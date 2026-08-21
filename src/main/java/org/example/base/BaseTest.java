package org.example.base;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.ScreenshotType;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.utils.ConfigManager;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.ByteArrayInputStream;

public class BaseTest {

    protected static final Logger log = LogManager.getLogger(BaseTest.class);

    // No protected Page field — same reasoning as the old "no protected WebDriver
    // field": if TestNG threads shared one class instance, they'd stomp on each
    // other's reference. BrowserManager's ThreadLocals solve this completely.

    @BeforeSuite
    public void initConfig() {
        ConfigManager.loadConfig();
        log.info("Environment: [{}] | URL: [{}]",
                ConfigManager.getEnvironment(),
                ConfigManager.getBaseUrl());
    }

    @Parameters("browser")
    @BeforeMethod
    public void setUp(@Optional("chromium") String browser) {
        BrowserManager.initBrowser(browser);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        String testName = result.getMethod().getMethodName();

        if (result.getStatus() == ITestResult.FAILURE) {
            log.error("[Thread-{}] Test FAILED: {}",
                    Thread.currentThread().getId(), testName);
            captureScreenshot(testName);
            BrowserManager.saveTrace(testName);
        }

        // BrowserManager tears down THIS thread's browser only
        BrowserManager.quitBrowser();
    }

    // Convenience method for all test classes — always fetches the correct
    // Page for the calling thread.
    protected Page getPage() {
        return BrowserManager.getPage();
    }

    private void captureScreenshot(String testName) {
        try {
            byte[] screenshot = getPage().screenshot(new Page.ScreenshotOptions()
                    .setType(ScreenshotType.PNG)
                    .setFullPage(true));

            Allure.addAttachment(
                    "Screenshot on failure — " + testName,
                    new ByteArrayInputStream(screenshot));

            log.info("Screenshot attached for: {}", testName);
        } catch (Exception e) {
            log.error("Screenshot capture failed: {}", e.getMessage());
        }
    }
}
