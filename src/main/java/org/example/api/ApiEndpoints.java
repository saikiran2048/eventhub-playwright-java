package org.example.api;

/**
 * Single source of truth for all API endpoints.
 * Never hardcode endpoint strings in test files.
 *
 * Usage:
 *   ApiClient.post(ApiEndpoints.LOGIN, body)
 *   ApiClient.get(ApiEndpoints.USER_PROFILE)
 */
public class ApiEndpoints {

    private ApiEndpoints() {}

    // Auth
    public static final String LOGIN = "/api/auth/login";
    public static final String REGISTER = "/api/auth/register";
    public static final String REFRESH_TOKEN = "/api/auth/refresh";

    // User
    public static final String USER_PROFILE = "/api/user/me";
    public static final String USER_UPDATE = "/api/user/update";

    // Add your real endpoints below as you expand
    // public static final String ORDERS = "/api/orders";
    // public static final String PRODUCTS = "/api/products";
}
