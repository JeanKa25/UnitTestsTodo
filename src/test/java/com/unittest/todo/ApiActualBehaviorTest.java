package com.unittest.todo;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiActualBehaviorTest extends TestAbstract {


    @Test
    void todoPutEmptyBodyReturns400() {
        String todoId = createTodoReturningId("put should be 400");
        assertTrue(todoId != null && !todoId.isEmpty());

        int putCode = requestStatus("PUT", "/todos/" + todoId, "{}", ContentType.JSON);
        assertEquals(400, putCode);

        requestStatus("DELETE", "/todos/" + todoId);
    }

    @Test
    void todoCategoriesPostReturns404() {
        String todoId = createTodoReturningId("link category returns 404");
        assertTrue(todoId != null && !todoId.isEmpty());

        int postCode = requestStatus(
                "POST",
                "/todos/" + todoId + "/categories",
                "{\"id\":\"999999\"}",
                ContentType.JSON
        );
        assertEquals(404, postCode);

        requestStatus("DELETE", "/todos/" + todoId);
    }

    @Test
    void todoCategoriesDetailGetReturns404() {
        String todoId = createTodoReturningId("categories detail get 404");
        assertTrue(todoId != null && !todoId.isEmpty());

        String categoryId = "999999";
        int code = requestStatus("GET", "/todos/" + todoId + "/categories/" + categoryId);
        assertEquals(404, code);

        requestStatus("DELETE", "/todos/" + todoId);
    }

    @Test
    void todoTasksofPostReturns404() {
        String todoId = createTodoReturningId("link tasksof returns 404");
        assertTrue(todoId != null && !todoId.isEmpty());

        int postCode = requestStatus(
                "POST",
                "/todos/" + todoId + "/tasksof",
                "{\"id\":\"999999\"}",
                ContentType.JSON
        );
        assertEquals(404, postCode);

        requestStatus("DELETE", "/todos/" + todoId);
    }

    @Test
    void todoTasksofDetailGetReturns404() {
        String todoId = createTodoReturningId("tasksof detail get 404");
        assertTrue(todoId != null && !todoId.isEmpty());

        String projectId = "999999";
        int code = requestStatus("GET", "/todos/" + todoId + "/tasksof/" + projectId);
        assertEquals(404, code);

        requestStatus("DELETE", "/todos/" + todoId);
    }

    @Test
    void projectCategoriesPostReturns404() {
        String projectId = createProjectReturningId("link category returns 404");
        assertTrue(projectId != null && !projectId.isEmpty());

        int postCode = requestStatus(
                "POST",
                "/projects/" + projectId + "/categories",
                "{\"id\":\"999999\"}",
                ContentType.JSON
        );
        assertEquals(404, postCode);

        requestStatus("DELETE", "/projects/" + projectId);
    }

    @Test
    void projectCategoriesDetailGetReturns404() {
        String projectId = createProjectReturningId("categories detail get 404");
        assertTrue(projectId != null && !projectId.isEmpty());

        String categoryId = "999999";
        int code = requestStatus("GET", "/projects/" + projectId + "/categories/" + categoryId);
        assertEquals(404, code);

        requestStatus("DELETE", "/projects/" + projectId);
    }

    @Test
    void projectTasksPostReturns404() {
        String projectId = createProjectReturningId("link tasks returns 404");
        assertTrue(projectId != null && !projectId.isEmpty());

        int postCode = requestStatus(
                "POST",
                "/projects/" + projectId + "/tasks",
                "{\"id\":\"999999\"}",
                ContentType.JSON
        );
        assertEquals(404, postCode);

        requestStatus("DELETE", "/projects/" + projectId);
    }

    @Test
    void projectTasksDetailGetReturns404() {
        String projectId = createProjectReturningId("tasks detail get 404");
        assertTrue(projectId != null && !projectId.isEmpty());

        String todoId = "999999";
        int code = requestStatus("GET", "/projects/" + projectId + "/tasks/" + todoId);
        assertEquals(404, code);

        requestStatus("DELETE", "/projects/" + projectId);
    }

    @Test
    void todoCategoriesAggregatedGetHeadOptionsReturn200() {
        int getCode = requestStatus("GET", "/todos/categories");
        int headCode = requestStatus("HEAD", "/todos/categories");
        int optionsCode = requestStatus("OPTIONS", "/todos/categories");
        assertEquals(200, getCode);
        assertEquals(200, headCode);
        assertEquals(200, optionsCode);
    }

    @Test
    void todoTasksofAggregatedGetHeadOptionsReturn200() {
        int getCode = requestStatus("GET", "/todos/tasksof");
        int headCode = requestStatus("HEAD", "/todos/tasksof");
        int optionsCode = requestStatus("OPTIONS", "/todos/tasksof");
        assertEquals(200, getCode);
        assertEquals(200, headCode);
        assertEquals(200, optionsCode);
    }

    @Test
    void todoOptionsAnyTextReturns200EmptyBody() {
        String path = "/todos/not_a_real_endpoint_" + System.currentTimeMillis();
        Response r = given().when().options(path);
        int status = r.then().extract().statusCode();
        String body = r.getBody() == null ? null : r.getBody().asString();
        assertEquals(200, status);
        assertTrue(body == null || body.trim().isEmpty());
    }
}


