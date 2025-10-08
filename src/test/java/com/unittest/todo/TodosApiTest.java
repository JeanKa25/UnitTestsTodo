package com.unittest.todo;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;

@Tag("expected")
class TodosApiExpectedTest extends TestAbstract {

    @Test
    void getAllTodos() {
        given().when().get("/todos").then().statusCode(200);
    }

    @Test
    void optionsTodos() {
        given()
                .when()
                .options("/todos")
                .then()
                .statusCode(200);
    }

    @Test
    void headTodos() {
        given()
                .when()
                .head("/todos")
                .then()
                .statusCode(200);
    }

    @Test
    void putTodosNotAllowed() {
        given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .put("/todos")
                .then()
                .statusCode(405);
    }

    @Test
    void deleteTodosNotAllowed() {
        given()
                .when()
                .delete("/todos")
                .then()
                .statusCode(405);
    }

    @Test
    void patchTodosNotAllowed() {
        given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .patch("/todos")
                .then()
                .statusCode(405);
    }

    @Test
    void postTodoJson() {
        int before = getCount("/todos");
        ValidatableResponse create = createTodoJson("unit test todo");
        create.statusCode(anyOf(is(201), is(200))); // Some implementations return 200
        given().when().get("/todos").then().statusCode(200);
        int after = getCount("/todos");
        if (before >= 0 && after >= 0) {
            assertTrue(after == before || after == before + 1);
        }
    }

    @Test
    void postTodoMalformedJson() {
        String malformed = "{\"title\":\"bad"; // missing closing quotes/braces
        given()
                .contentType(ContentType.JSON)
                .body(malformed)
                .when()
                .post("/todos")
                .then()
                .statusCode(anyOf(is(400), is(415)));
    }

    @Test
    void postTodoXmlVariants() {
        String xml = "<todo><title>xml todo</title></todo>";
        given()
                .contentType(ContentType.XML)
                .body(xml)
                .when()
                .post("/todos")
                .then()
                .statusCode(anyOf(is(201), is(200), is(400)));

        String badXml = "<todo><title>bad"; // malformed xml
        given()
                .contentType(ContentType.XML)
                .body(badXml)
                .when()
                .post("/todos")
                .then()
                .statusCode(anyOf(is(400), is(415)));
    }

    @Test
    void getTodosAcceptJsonXml() {
        given().accept(ContentType.JSON).when().get("/todos").then().statusCode(200).contentType(containsString("json"));
        given().accept(ContentType.XML).when().get("/todos").then().statusCode(200);
    }

    @Test
    void todoByIdAndDeleteTwice() {
        String todoId = createTodoReturningId("byid todo");
        assertTrue(todoId != null && !todoId.isEmpty());

        assertEquals(200, requestStatus("GET", "/todos/" + todoId));
        assertEquals(200, requestStatus("PUT", "/todos/" + todoId, "{}", ContentType.JSON));
        assertEquals(200, requestStatus("POST", "/todos/" + todoId, "{}", ContentType.JSON));
        assertEquals(200, requestStatus("OPTIONS", "/todos/" + todoId));
        int headCode = requestStatus("HEAD", "/todos/" + todoId);
        assertTrue(headCode == 200 || headCode == 404);
        assertEquals(405, requestStatus("PATCH", "/todos/" + todoId, "{}", ContentType.JSON));
        assertEquals(200, requestStatus("DELETE", "/todos/" + todoId));
        assertEquals(404, requestStatus("DELETE", "/todos/" + todoId));
    }

    @Test
    void todoCategories() {
        String todoId = createTodoReturningId("cat todo");
        assertTrue(todoId != null && !todoId.isEmpty());

        assertEquals(200, requestStatus("GET", "/todos/" + todoId + "/categories"));
        assertEquals(200, requestStatus("OPTIONS", "/todos/" + todoId + "/categories"));
        assertEquals(200, requestStatus("HEAD", "/todos/" + todoId + "/categories"));
        assertEquals(405, requestStatus("PUT", "/todos/" + todoId + "/categories", "{}", ContentType.JSON));
        assertEquals(405, requestStatus("DELETE", "/todos/" + todoId + "/categories"));
        assertEquals(405, requestStatus("PATCH", "/todos/" + todoId + "/categories", "{}", ContentType.JSON));

        int postCode = requestStatus("POST", "/todos/" + todoId + "/categories", "{\"id\":\"999999\"}", ContentType.JSON);
        assertTrue(postCode == 201 || postCode == 400);
    }

    @Test
    void todoCategoriesDetail() {
        String todoId = createTodoReturningId("cat todo 2");
        assertTrue(todoId != null && !todoId.isEmpty());

        String categoryId = "999999";
        int del = requestStatus("DELETE", "/todos/" + todoId + "/categories/" + categoryId);
        assertTrue(del == 200 || del == 400 || del == 404);

        assertEquals(405, requestStatus("GET", "/todos/" + todoId + "/categories/" + categoryId));
        assertEquals(405, requestStatus("PUT", "/todos/" + todoId + "/categories/" + categoryId, "{}", ContentType.JSON));
        assertEquals(405, requestStatus("POST", "/todos/" + todoId + "/categories/" + categoryId, "{}", ContentType.JSON));
        assertEquals(200, requestStatus("OPTIONS", "/todos/" + todoId + "/categories/" + categoryId));
        assertEquals(405, requestStatus("HEAD", "/todos/" + todoId + "/categories/" + categoryId));
        assertEquals(405, requestStatus("PATCH", "/todos/" + todoId + "/categories/" + categoryId, "{}", ContentType.JSON));
    }

    @Test
    void todoTasksof() {
        String todoId = createTodoReturningId("tasksof todo");
        assertTrue(todoId != null && !todoId.isEmpty());

        assertEquals(200, requestStatus("GET", "/todos/" + todoId + "/tasksof"));
        assertEquals(200, requestStatus("OPTIONS", "/todos/" + todoId + "/tasksof"));
        assertEquals(200, requestStatus("HEAD", "/todos/" + todoId + "/tasksof"));
        assertEquals(405, requestStatus("PUT", "/todos/" + todoId + "/tasksof", "{}", ContentType.JSON));
        assertEquals(405, requestStatus("DELETE", "/todos/" + todoId + "/tasksof"));
        assertEquals(405, requestStatus("PATCH", "/todos/" + todoId + "/tasksof", "{}", ContentType.JSON));

        int postCode = requestStatus("POST", "/todos/" + todoId + "/tasksof", "{\"id\":\"999999\"}", ContentType.JSON);
        assertTrue(postCode == 201 || postCode == 400);
    }

    @Test
    void todoTasksofDetail() {
        String todoId = createTodoReturningId("tasksof todo 2");
        assertTrue(todoId != null && !todoId.isEmpty());

        String projectId = "999999";
        int del = requestStatus("DELETE", "/todos/" + todoId + "/tasksof/" + projectId);
        assertTrue(del == 200 || del == 400 || del == 404);

        assertEquals(405, requestStatus("GET", "/todos/" + todoId + "/tasksof/" + projectId));
        assertEquals(405, requestStatus("PUT", "/todos/" + todoId + "/tasksof/" + projectId, "{}", ContentType.JSON));
        assertEquals(405, requestStatus("POST", "/todos/" + todoId + "/tasksof/" + projectId, "{}", ContentType.JSON));
        assertEquals(200, requestStatus("OPTIONS", "/todos/" + todoId + "/tasksof/" + projectId));
        assertEquals(405, requestStatus("HEAD", "/todos/" + todoId + "/tasksof/" + projectId));
        assertEquals(405, requestStatus("PATCH", "/todos/" + todoId + "/tasksof/" + projectId, "{}", ContentType.JSON));
    }
}


