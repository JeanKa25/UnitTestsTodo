package com.unittest.todo;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Locale;

import static io.restassured.RestAssured.given;

@Tag("performance")
class TodosPerformanceTest extends TestAbstract {

    private static final int[] SIZES = new int[]{
            1, 5, 10, 50, 75, 100,
            200, 300, 400, 500, 600, 700, 800, 900, 1000
    };

    private static final int ITERATIONS = Integer.getInteger("todo.perf.iterations", 200);
    private static final Random RAND = new Random(42);
    private final List<String> createdIdsLog = new ArrayList<>();
	private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm:ss a", Locale.US);

    @Test
    void runTodosExperiment() throws Exception {
        File outDir = new File("target/perf");
        if (!outDir.exists()) outDir.mkdirs();
        File csv = new File(outDir, "todos-experiment.csv");
        int startingSize = getAllTodoIds().size();

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(csv, false), StandardCharsets.UTF_8))) {
            // Header
			String header = "timestamp_start_ms,start_time_local,timestamp_end_ms,size,iterations,create_avg_ms,update_avg_ms,delete_avg_ms,total_ms";
            System.out.println(header);
            pw.println(header);

            for (int size : SIZES) {
                createdIdsLog.clear();
                List<String> idsSnapshot = getAllTodoIds();

				long timestampStartMs = System.currentTimeMillis();
				String startLocal = TIME_FMT.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampStartMs), ZoneId.systemDefault()));
				long tAll0 = System.nanoTime();
                double createAvg = measureCreateAverage(size);
                List<String> updateCandidates = createdIdsLog.isEmpty() ? idsSnapshot : new ArrayList<>(createdIdsLog);
                double updateAvg = measureUpdateAverage(updateCandidates);
                double deleteAvg = measureDeleteAverage(updateCandidates.isEmpty() ? idsSnapshot : updateCandidates, size);
                long totalMsAll = java.time.Duration.ofNanos(System.nanoTime() - tAll0).toMillis();
				long timestampEndMs = System.currentTimeMillis();

				String line = timestampStartMs + "," + startLocal + "," + timestampEndMs + "," + size + "," + ITERATIONS + "," +
                        round2(createAvg) + "," + round2(updateAvg) + "," + round2(deleteAvg) + "," + totalMsAll;
                System.out.println(line);
                pw.println(line);
                pw.flush();

                // Clear IDs we tracked during this iteration
                createdIdsLog.clear();
            }
        }
    }

    private double measureCreateAverage(int count) {
        long totalMs = 0L;
        int attempts = 0;
        int maxAttempts = Math.max(count * 5, count);
        while (createdIdsLog.size() < count && attempts < maxAttempts) {
            attempts++;
            String body = "{\"title\":\"" + randomTitle("create") + "\"}";
            long t0 = System.nanoTime();
            Response r = given()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when()
                    .post("/todos");
            int status = r.then().extract().statusCode();
            long dtMs = Duration.ofNanos(System.nanoTime() - t0).toMillis();
            if (status >= 200 && status < 300) {
                totalMs += dtMs;
                // Track created id for cleanup
                try {
                    Object id = r.jsonPath().get("id");
                    if (id == null) {
                        String loc = r.getHeader("Location");
                        if (loc != null) {
                            int idx = loc.lastIndexOf('/');
                            if (idx >= 0 && idx < loc.length() - 1) {
                                String newId = loc.substring(idx + 1);
                                createdIdsLog.add(newId);
                            }
                        }
                    } else {
                        String newId = String.valueOf(id);
                        createdIdsLog.add(newId);
                    }
                } catch (Exception ignored) {}
            }
        }
        int successfulCreates = createdIdsLog.size();
        if (successfulCreates == 0) return Double.NaN;
        return (double) totalMs / successfulCreates;
    }

    private double measureUpdateAverage(List<String> idsPool) {
        if (idsPool.isEmpty()) return Double.NaN;
        long totalMs = 0L;
        int successCount = 0;
        int loops = Math.max(1, idsPool.size());
        for (int i = 0; i < loops; i++) {
            String id = idsPool.get(i % idsPool.size());
            String body = "{\"title\":\"" + randomTitle("updated") + "\"}";
            long t0 = System.nanoTime();
            Response r = given()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when()
                    .put("/todos/" + id);
            int status = r.then().extract().statusCode();
            long dtMs = Duration.ofNanos(System.nanoTime() - t0).toMillis();
            if (status >= 200 && status < 300) {
                totalMs += dtMs;
                successCount++;
            }
        }
        if (successCount == 0) return Double.NaN;
        return (double) totalMs / successCount;
    }

    private double measureDeleteAverage(List<String> idsPool, int count) {
        if (idsPool.isEmpty()) return Double.NaN;
        // Pick up to ITERATIONS distinct ids; if not enough, cycle
        int loops = Math.max(1, idsPool.size());
        List<String> toDelete = new ArrayList<>(loops);
        Set<String> chosen = new HashSet<>();
        int idx = 0;
        while (toDelete.size() < loops) {
            String id = idsPool.get(idx % idsPool.size());
            if (chosen.add(id)) {
                toDelete.add(id);
            }
            idx++;
            if (chosen.size() == idsPool.size()) {
                // No more unique ids available; continue cycling allowing duplicates
                break;
            }
        }
        while (toDelete.size() < loops) {
            toDelete.add(idsPool.get(toDelete.size() % idsPool.size()));
        }

        long totalMs = 0L;
        int successCount = 0;
        List<String> deleted = new ArrayList<>(toDelete.size());
        for (String id : toDelete) {
            long t0 = System.nanoTime();
            Response r = given().when().delete("/todos/" + id);
            int status = r.then().extract().statusCode();
            long dtMs = Duration.ofNanos(System.nanoTime() - t0).toMillis();
            if (status >= 200 && status < 300) {
                totalMs += dtMs;
                successCount++;
                deleted.add(id);
            }
        }
        if (successCount == 0) return Double.NaN;
        return (double) totalMs / successCount;
    }



    private static String randomTitle(String prefix) {
        // Short random suffix for readability
        long x = Math.abs(RAND.nextLong());
        return prefix + "-" + Long.toString(x, 36);
    }

    @SuppressWarnings("unchecked")
    private static List<String> getAllTodoIds() {
        try {
            Response r = given().when().get("/todos");
            List<Map<String, Object>> list = r.jsonPath().getList("todos");
            if (list == null) return Collections.emptyList();
            List<String> ids = new ArrayList<>(list.size());
            for (Map<String, Object> m : list) {
                Object id = m.get("id");
                if (id != null) ids.add(String.valueOf(id));
            }
            return ids;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private static String round2(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "";
        }
        return String.format(java.util.Locale.ROOT, "%.4f", value);
    }
}


