package org.example.base;

import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.api.ApiClient;
import org.example.utils.ConfigManager;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * Base class for all API test classes.
 * Does NOT extend BaseTest — no browser needed.
 * Separate hierarchy:
 *
 *   BaseTest    -> UI tests  (Playwright Page)
 *   BaseApiTest -> API tests (Playwright APIRequestContext only)
 */
public class BaseApiTest {

    protected static final Logger log = LogManager.getLogger(BaseApiTest.class);

    @BeforeSuite(alwaysRun = true)
    public void initApiSuite() {
        ConfigManager.loadConfig();
        ApiClient.init();

        log.info("API Suite initialized -> env: [{}] | api.url: [{}]",
                ConfigManager.getEnvironment(),
                ConfigManager.getApiBaseUrl());
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownApiSuite() {
        ApiClient.close();
    }

    // ─── Shared helpers for all API test classes ─────────────────────

    protected void logStep(String message) {
        log.info(message);
        Allure.step(message);
    }

    protected void logPass(String message) {
        log.info("PASS: {}", message);
        Allure.step("PASS: " + message);
    }

    protected void logFail(String message) {
        log.error("FAIL: {}", message);
        Allure.step("FAIL: " + message);
    }
}
