package com.unittest.todo;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;

@Tag("expected")
class ProjectsApiExpectedTest extends TestAbstract {

    @Test
    void getAllProjects() {
        given().when().get("/projects").then().statusCode(200);
    }

    @Test
    void optionsProjects() {
        given().when().options("/projects").then().statusCode(200);
    }

    @Test
    void headProjects() {
        given().when().head("/projects").then().statusCode(200);
    }

    @Test
    void putProjectsNotAllowed() {
        given().contentType(ContentType.JSON).body("{}").when().put("/projects").then().statusCode(405);
    }

    @Test
    void deleteProjectsNotAllowed() {
        given().when().delete("/projects").then().statusCode(405);
    }

    @Test
    void patchProjectsNotAllowed() {
        given().contentType(ContentType.JSON).body("{}").when().patch("/projects").then().statusCode(405);
    }

    @Test
    void postProjectJson() {
        int before = getCount("/projects");
        String body = "{\"title\":\"unit project\"}";
        given().contentType(ContentType.JSON).body(body).when().post("/projects").then().statusCode(anyOf(is(201), is(200)));
        int after = getCount("/projects");
        if (before >= 0 && after >= 0) {
            assertTrue(after == before || after == before + 1);
        }
    }

    @Test
    void postProjectMalformedJson() {
        String malformed = "{\"title\":\"bad";
        given().contentType(ContentType.JSON).body(malformed).when().post("/projects").then().statusCode(anyOf(is(400), is(415)));
    }

    @Test
    void postProjectXmlVariants() {
        String xml = "<project><title>xml project</title></project>";
        given().contentType(ContentType.XML).body(xml).when().post("/projects").then().statusCode(anyOf(is(201), is(200), is(400)));

        String badXml = "<project><title>bad";
        given().contentType(ContentType.XML).body(badXml).when().post("/projects").then().statusCode(anyOf(is(400), is(415)));
    }

    @Test
    void getProjectsAcceptJsonXml() {
        given().accept(ContentType.JSON).when().get("/projects").then().statusCode(200).contentType(containsString("json"));
        given().accept(ContentType.XML).when().get("/projects").then().statusCode(200);
    }

    @Test
    void projectByIdCrudAndDeleteTwice() {
        String projectId = createProjectReturningId("byid project");
        assertTrue(projectId != null && !projectId.isEmpty());

        assertEquals(200, requestStatus("GET", "/projects/" + projectId));
        assertEquals(200, requestStatus("PUT", "/projects/" + projectId, "{}", ContentType.JSON));
        assertEquals(200, requestStatus("POST", "/projects/" + projectId, "{}", ContentType.JSON));
        assertEquals(200, requestStatus("OPTIONS", "/projects/" + projectId));
        int headCode = requestStatus("HEAD", "/projects/" + projectId);
        assertTrue(headCode == 200 || headCode == 404);
        assertEquals(405, requestStatus("PATCH", "/projects/" + projectId, "{}", ContentType.JSON));
        assertEquals(200, requestStatus("DELETE", "/projects/" + projectId));
        assertEquals(404, requestStatus("DELETE", "/projects/" + projectId));
    }

    @Test
    void projectTasks() {
        String projectId = createProjectReturningId("tasks project");
        assertTrue(projectId != null && !projectId.isEmpty());

        assertEquals(200, requestStatus("GET", "/projects/" + projectId + "/tasks"));
        assertEquals(200, requestStatus("OPTIONS", "/projects/" + projectId + "/tasks"));
        assertEquals(200, requestStatus("HEAD", "/projects/" + projectId + "/tasks"));
        assertEquals(405, requestStatus("PUT", "/projects/" + projectId + "/tasks", "{}", ContentType.JSON));
        assertEquals(405, requestStatus("DELETE", "/projects/" + projectId + "/tasks"));
        assertEquals(405, requestStatus("PATCH", "/projects/" + projectId + "/tasks", "{}", ContentType.JSON));

        int postCode = requestStatus("POST", "/projects/" + projectId + "/tasks", "{\"id\":\"999999\"}", ContentType.JSON);
        assertTrue(postCode == 201 || postCode == 400);
    }

    @Test
    void projectTasksDetail() {
        String projectId = createProjectReturningId("tasks project 2");
        assertTrue(projectId != null && !projectId.isEmpty());

        String todoId = "999999";
        int del = requestStatus("DELETE", "/projects/" + projectId + "/tasks/" + todoId);
        assertTrue(del == 200 || del == 400 || del == 404);

        assertEquals(405, requestStatus("GET", "/projects/" + projectId + "/tasks/" + todoId));
        assertEquals(405, requestStatus("PUT", "/projects/" + projectId + "/tasks/" + todoId, "{}", ContentType.JSON));
        assertEquals(405, requestStatus("POST", "/projects/" + projectId + "/tasks/" + todoId, "{}", ContentType.JSON));
        assertEquals(200, requestStatus("OPTIONS", "/projects/" + projectId + "/tasks/" + todoId));
        assertEquals(405, requestStatus("HEAD", "/projects/" + projectId + "/tasks/" + todoId));
        assertEquals(405, requestStatus("PATCH", "/projects/" + projectId + "/tasks/" + todoId, "{}", ContentType.JSON));
    }

    @Test
    void projectCategories() {
        String projectId = createProjectReturningId("cat project");
        assertTrue(projectId != null && !projectId.isEmpty());

        assertEquals(200, requestStatus("GET", "/projects/" + projectId + "/categories"));
        assertEquals(200, requestStatus("OPTIONS", "/projects/" + projectId + "/categories"));
        assertEquals(200, requestStatus("HEAD", "/projects/" + projectId + "/categories"));
        assertEquals(405, requestStatus("PUT", "/projects/" + projectId + "/categories", "{}", ContentType.JSON));
        assertEquals(405, requestStatus("DELETE", "/projects/" + projectId + "/categories"));
        assertEquals(405, requestStatus("PATCH", "/projects/" + projectId + "/categories", "{}", ContentType.JSON));

        int postCode = requestStatus("POST", "/projects/" + projectId + "/categories", "{\"id\":\"999999\"}", ContentType.JSON);
        assertTrue(postCode == 201 || postCode == 400);
    }

    @Test
    void projectCategoriesDetail() {
        String projectId = createProjectReturningId("cat project 2");
        assertTrue(projectId != null && !projectId.isEmpty());

        String categoryId = "999999";
        int del = requestStatus("DELETE", "/projects/" + projectId + "/categories/" + categoryId);
        assertTrue(del == 200 || del == 400 || del == 404);

        assertEquals(405, requestStatus("GET", "/projects/" + projectId + "/categories/" + categoryId));
        assertEquals(405, requestStatus("PUT", "/projects/" + projectId + "/categories/" + categoryId, "{}", ContentType.JSON));
        assertEquals(405, requestStatus("POST", "/projects/" + projectId + "/categories/" + categoryId, "{}", ContentType.JSON));
        assertEquals(200, requestStatus("OPTIONS", "/projects/" + projectId + "/categories/" + categoryId));
        assertEquals(405, requestStatus("HEAD", "/projects/" + projectId + "/categories/" + categoryId));
        assertEquals(405, requestStatus("PATCH", "/projects/" + projectId + "/categories/" + categoryId, "{}", ContentType.JSON));
    }
}


