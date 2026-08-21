package org.example.api;

import org.example.base.BaseApiTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * API tests for the login endpoint.
 * No browser. No Page. Pure HTTP via Playwright's APIRequestContext.
 * Fast — each test runs in under 1 second.
 */
public class LoginApiTest extends BaseApiTest {

    private static final String VALID_EMAIL = "test4545@youmail.com";
    private static final String VALID_PASSWORD = "q9sgCu@u8LddRZP";

    private Map<String, String> buildLoginBody(String email, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        return body;
    }

    @Test(description = "Valid credentials should return 200 and auth token", priority = 0)
    public void testValidLoginReturns200() {
        logStep("Sending POST to: " + ApiEndpoints.LOGIN);
        logStep("Email: " + VALID_EMAIL);

        ApiResponse response = ApiClient.post(
                ApiEndpoints.LOGIN, buildLoginBody(VALID_EMAIL, VALID_PASSWORD));

        logStep("Asserting status code is 200");
        Assert.assertEquals(response.statusCode(), 200,
                "Expected 200 but got: " + response.statusCode() + " | Body: " + response.asString());
        logPass("Status code 200");

        logStep("Asserting auth token is present");
        String token = response.jsonPath("token"); // adjust to your API's JSON key
        Assert.assertNotNull(token, "Auth token is null in response: " + response.asString());
        Assert.assertFalse(token.isEmpty(), "Auth token is empty");
        logPass("Auth token received: " + token.substring(0, Math.min(10, token.length())) + "...");
    }

    @Test(description = "Invalid credentials should return 400", priority = 1)
    public void testInvalidLoginReturns400() {
        logStep("Sending POST with invalid credentials");

        ApiResponse response = ApiClient.post(
                ApiEndpoints.LOGIN, buildLoginBody("wrong@email.com", "wrongpassword"));

        Assert.assertEquals(response.statusCode(), 400,
                "Expected 400 but got: " + response.statusCode());
        logPass("Status 400 returned for invalid credentials");

        String error = response.jsonPath("error");
        Assert.assertNotNull(error, "Error message missing in 400 response");
        logPass("Error message present: " + error);
    }

    @Test(description = "Empty credentials should return 400", priority = 2)
    public void testEmptyCredentialsReturns400() {
        logStep("Sending POST with empty credentials");

        ApiResponse response = ApiClient.post(ApiEndpoints.LOGIN, buildLoginBody("", ""));

        Assert.assertEquals(response.statusCode(), 400,
                "Expected 400 but got: " + response.statusCode());
        logPass("Status 400 returned for empty credentials");
    }

    @Test(description = "Invalid email format should return 400", priority = 3)
    public void testInvalidEmailFormatReturns400() {
        logStep("Sending POST with malformed email");

        ApiResponse response = ApiClient.post(
                ApiEndpoints.LOGIN, buildLoginBody("notanemail", "validpassword66"));

        Assert.assertEquals(response.statusCode(), 400,
                "Expected 400 but got: " + response.statusCode());
        logPass("Status 400 returned for invalid email format");
    }
}
