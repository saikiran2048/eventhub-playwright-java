package org.example.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIResponse;

/**
 * Wraps Playwright's APIResponse the same way the old RestAssured-based
 * ApiResponse wrapped a RestAssured Response — gives test code a small,
 * stable surface instead of coupling every test to the underlying HTTP
 * client's API.
 */
public class ApiResponse {

    private final APIResponse response;
    private final ObjectMapper mapper = new ObjectMapper();
    private String cachedBody;

    public ApiResponse(APIResponse response) {
        this.response = response;
    }

    public int statusCode() {
        return response.status();
    }

    public boolean isOk() {
        return response.ok();
    }

    public String asString() {
        if (cachedBody == null) {
            cachedBody = response.text();
        }
        return cachedBody;
    }

    public JsonNode asJson() {
        try {
            return mapper.readTree(asString());
        } catch (Exception e) {
            throw new RuntimeException("Response body is not valid JSON: " + asString(), e);
        }
    }

    public String jsonPath(String fieldName) {
        JsonNode node = asJson().get(fieldName);
        return node == null ? null : node.asText();
    }

    public String header(String name) {
        return response.headers().get(name.toLowerCase());
    }

    public APIResponse raw() {
        return response;
    }
}
