package com.unittest.todo;

import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceAvailabilityTest extends TestAbstract {

    @Test
    void rootReturns200() {
        ValidatableResponse res = given().when().get("/").then();
        int status = res.extract().statusCode();
        assertEquals(200, status);
    }
}


