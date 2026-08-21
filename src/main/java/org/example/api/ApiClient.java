package org.example.api;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.utils.ConfigManager;

/**
 * Central HTTP client for all API calls — the Playwright equivalent of the
 * old RestAssured-based RestClient. Playwright ships APIRequestContext
 * natively, so this framework needs no separate HTTP library: UI and API
 * tests both run through the same Playwright process, and API calls made
 * through a browser context's request object even share cookies with the
 * UI session if you ever need a hybrid test.
 *
 * Never call APIRequestContext directly in test classes — always go
 * through ApiClient, same rule as before.
 */
public class ApiClient {

    private static final Logger log = LogManager.getLogger(ApiClient.class);

    private static Playwright playwright;
    private static APIRequestContext requestContext;

    private ApiClient() {}

    // ─── Initialisation ─────────────────────────────────────────────

    /** Called once per suite in BaseApiTest. */
    public static synchronized void init() {
        String apiUrl = ConfigManager.getApiBaseUrl();
        int timeoutSeconds = ConfigManager.getApiTimeout();

        log.info("Initializing ApiClient -> base URL: [{}]", apiUrl);

        playwright = Playwright.create();
        requestContext = playwright.request().newContext(
                new com.microsoft.playwright.APIRequest.NewContextOptions()
                        .setBaseURL(apiUrl)
                        .setTimeout(timeoutSeconds * 1000.0)
                        .setExtraHTTPHeaders(java.util.Map.of(
                                "Content-Type", "application/json",
                                "Accept", "application/json")));

        log.info("ApiClient initialized -> timeout: {}s", timeoutSeconds);
    }

    public static synchronized void close() {
        if (requestContext != null) {
            requestContext.dispose();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    // ─── HTTP Methods ───────────────────────────────────────────────

    public static ApiResponse post(String endpoint, Object body) {
        log.info("POST -> {}", endpoint);
        APIResponse response = requestContext.post(endpoint,
                RequestOptions.create().setData(body));
        logSummary("POST", endpoint, response);
        return new ApiResponse(response);
    }

    public static ApiResponse post(String endpoint, Object body, String token) {
        log.info("POST (auth) -> {}", endpoint);
        APIResponse response = requestContext.post(endpoint,
                RequestOptions.create()
                        .setData(body)
                        .setHeader("Authorization", "Bearer " + token));
        logSummary("POST", endpoint, response);
        return new ApiResponse(response);
    }

    public static ApiResponse get(String endpoint) {
        log.info("GET -> {}", endpoint);
        APIResponse response = requestContext.get(endpoint);
        logSummary("GET", endpoint, response);
        return new ApiResponse(response);
    }

    public static ApiResponse get(String endpoint, String token) {
        log.info("GET (auth) -> {}", endpoint);
        APIResponse response = requestContext.get(endpoint,
                RequestOptions.create().setHeader("Authorization", "Bearer " + token));
        logSummary("GET", endpoint, response);
        return new ApiResponse(response);
    }

    public static ApiResponse put(String endpoint, Object body, String token) {
        log.info("PUT (auth) -> {}", endpoint);
        APIResponse response = requestContext.put(endpoint,
                RequestOptions.create()
                        .setData(body)
                        .setHeader("Authorization", "Bearer " + token));
        logSummary("PUT", endpoint, response);
        return new ApiResponse(response);
    }

    public static ApiResponse delete(String endpoint, String token) {
        log.info("DELETE (auth) -> {}", endpoint);
        APIResponse response = requestContext.delete(endpoint,
                RequestOptions.create().setHeader("Authorization", "Bearer " + token));
        logSummary("DELETE", endpoint, response);
        return new ApiResponse(response);
    }

    public static ApiResponse post(String endpoint, Object body, java.util.Map<String, String> headers) {
        log.info("POST (custom headers) -> {}", endpoint);
        RequestOptions options = RequestOptions.create().setData(body);
        headers.forEach(options::setHeader);
        APIResponse response = requestContext.post(endpoint, options);
        logSummary("POST", endpoint, response);
        return new ApiResponse(response);
    }

    // ─── Private helper ─────────────────────────────────────────────

    private static void logSummary(String method, String endpoint, APIResponse response) {
        log.info("  {} {} -> {}", method, endpoint, response.status());
    }
}
