package com.unittest.todo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public abstract class TestAbstract {
    protected static final String BASE_URL = System.getProperty("todo.baseUrl", "http://localhost:4567");

    static {
        RestAssured.baseURI = BASE_URL;
    }

    protected ValidatableResponse createTodoJson(String title) {
        String body = "{\"title\":\"" + title + "\"}";
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/todos")
                .then();
    }

    protected ValidatableResponse createProjectJson(String title) {
        String body = "{\"title\":\"" + title + "\"}";
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/projects")
                .then();
    }

    protected int requestStatus(String method, String path) {
        return requestStatus(method, path, null, null);
    }

    protected int requestStatus(String method, String path, String body, ContentType contentType) {
        switch (method.toUpperCase()) {
            case "GET":
                return given().when().get(path).then().extract().statusCode();
            case "PUT":
                return given()
                        .contentType(contentType == null ? ContentType.JSON : contentType)
                        .body(body == null ? "{}" : body)
                        .when().put(path)
                        .then().extract().statusCode();
            case "POST":
                return given()
                        .contentType(contentType == null ? ContentType.JSON : contentType)
                        .body(body == null ? "{}" : body)
                        .when().post(path)
                        .then().extract().statusCode();
            case "DELETE":
                return given().when().delete(path).then().extract().statusCode();
            case "OPTIONS":
                return given().when().options(path).then().extract().statusCode();
            case "HEAD":
                return given().when().head(path).then().extract().statusCode();
            case "PATCH":
                return given()
                        .contentType(contentType == null ? ContentType.JSON : contentType)
                        .body(body == null ? "{}" : body)
                        .when().patch(path)
                        .then().extract().statusCode();
            default:
                throw new IllegalArgumentException("Unsupported method: " + method);
        }
    }

    protected String createTodoReturningId(String title) {
        ValidatableResponse vr = createTodoJson(title);
        return extractId(vr, "/todos");
    }

    protected String createProjectReturningId(String title) {
        ValidatableResponse vr = createProjectJson(title);
        return extractId(vr, "/projects");
    }

    protected String extractId(ValidatableResponse vr, String resourcePath) {
        Response r = vr.extract().response();
        String location = r.getHeader("Location");
        if (location != null && !location.isEmpty()) {
            int idx = location.lastIndexOf('/');
            if (idx >= 0 && idx < location.length() - 1) {
                return location.substring(idx + 1);
            }
        }
        try {
            Object id = r.jsonPath().get("id");
            if (id != null) {
                return String.valueOf(id);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    protected int getCount(String collectionPath) {
        try {
            Response r = given().when().get(collectionPath);
            String keyFromPath = collectionPath.startsWith("/") ? collectionPath.substring(1) : collectionPath;
            int slash = keyFromPath.indexOf('/');
            if (slash > 0) keyFromPath = keyFromPath.substring(0, slash);
            try {
                java.util.List<?> list = r.jsonPath().getList(keyFromPath);
                if (list != null) return list.size();
            } catch (Exception ignored) {}
            try {
                java.util.List<?> list = r.jsonPath().getList("$");
                if (list != null) return list.size();
            } catch (Exception ignored) {}
        } catch (Exception ignoredOuter) {}
        return -1;
    }

}


