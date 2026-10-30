package com.shiwani.placementManagementSystem;

import com.shiwani.placementManagementSystem.exception.GlobalExceptionHandler;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {


    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void shouldReturn500ForUnexpectedException() {

        Exception exception = new RuntimeException("Internal error");

        ResponseEntity<Map<String, Object>> response =
                handler.handleGenericException(exception);

        assertEquals(500, response.getStatusCode().value());

        assertEquals(
                "An unexpected error occurred",
                response.getBody().get("message")
        );
    }

    @Test
    void shouldReturn400ForInvalidRequestBody() {

        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException(
                        "Invalid JSON",
                        (HttpInputMessage) null
                );

        ResponseEntity<Map<String, Object>> response =
                handler.handleInvalidRequestBody(exception);

        assertEquals(400, response.getStatusCode().value());

        assertEquals(
                "Invalid or missing request body",
                response.getBody().get("message")
        );
    }

    @Test
    void shouldReturnCorrectStatusForResponseStatusException() {

        ResponseStatusException exception =
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Resource not found"
                );

        ResponseEntity<Map<String, Object>> response =
                handler.handleResponseStatusException(exception);

        assertEquals(404, response.getStatusCode().value());

        assertEquals(
                "Resource not found",
                response.getBody().get("message")
        );

        assertNotNull(response.getBody().get("timestamp"));
    }


}
