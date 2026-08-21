package org.example.tests;

import io.qameta.allure.Allure;
import org.example.base.BaseTest;
import org.example.pages.DashBoardPage;
import org.example.pages.LoginPage;
import org.example.utils.ConfigManager;
import org.example.utils.ExcelUtil;
import org.example.utils.JsonUtil;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

public class DataDrivenLoginTest extends BaseTest {

    private static final String EXCEL_PATH =
            System.getProperty("user.dir") + "/src/test/resources/testdata/loginData.xlsx";

    private static final String JSON_PATH =
            System.getProperty("user.dir") + "/src/test/resources/testdata/loginData.json";

    private static final String SHEET_NAME = "LoginData";

    // ─── DataProviders ──────────────────────────────────────────────

    @DataProvider(name = "excelLoginData", parallel = true)
    public Object[][] excelLoginData() {
        return ExcelUtil.toDataProviderFormat(EXCEL_PATH, SHEET_NAME);
    }

    @DataProvider(name = "jsonLoginData", parallel = true)
    public Object[][] jsonLoginData() {
        return JsonUtil.toDataProviderFormat(JSON_PATH);
    }

    // ─── Test using Excel ───────────────────────────────────────────

    @SuppressWarnings("unchecked")
    @Test(dataProvider = "excelLoginData",
            description = "Data driven login tests from Excel",
            enabled = true)
    public void testLoginFromExcel(Map<String, String> row) {
        String testCase = row.get("TestCaseName");
        String email = row.get("Email");
        String password = row.get("Password");
        String expectedOutcome = row.get("ExpectedOutcome");
        String expectedLoginError = row.get("ExpectedLoginError");
        String expectedEmailError = row.get("ExpectedEmailError");
        String expectedPasswordError = row.get("ExpectedPasswordError");

        logToReport(testCase, email, expectedOutcome);
        log.info("[Excel] TestCase: {} | Email: {} | Expected: {}", testCase, email, expectedOutcome);

        runLoginAndAssert(email, password, expectedOutcome,
                expectedLoginError, expectedEmailError, expectedPasswordError, testCase);
    }

    // ─── Test using JSON ────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    @Test(dataProvider = "jsonLoginData",
            description = "Data driven login tests from JSON",
            enabled = true)
    public void testLoginFromJson(Map<String, String> row) {
        String testCase = row.get("testCaseName");
        String email = row.get("email");
        String password = row.get("password");
        String expectedOutcome = row.get("expectedOutcome");
        String expectedLoginError = row.get("expectedLoginError");
        String expectedEmailError = row.get("expectedEmailError");
        String expectedPasswordError = row.get("expectedPasswordError");

        logToReport(testCase, email, expectedOutcome);
        log.info("[JSON] TestCase: {} | Email: {} | Expected: {}", testCase, email, expectedOutcome);

        runLoginAndAssert(email, password, expectedOutcome,
                expectedLoginError, expectedEmailError, expectedPasswordError, testCase);
    }

    // ─── Core execution + assertion ─────────────────────────────────

    private void runLoginAndAssert(
            String email, String password, String expectedOutcome,
            String expectedLoginError, String expectedEmailError,
            String expectedPasswordError, String testCase) {

        getPage().navigate(ConfigManager.getBaseUrl());

        LoginPage loginPage = new LoginPage(getPage());
        loginPage.login(email, password);

        if ("success".equalsIgnoreCase(expectedOutcome)) {
            assertSuccess(email, testCase);
        } else if ("error".equalsIgnoreCase(expectedOutcome)) {
            assertErrors(loginPage, expectedLoginError, expectedEmailError, expectedPasswordError, testCase);
        } else {
            throw new RuntimeException(
                    "Unknown ExpectedOutcome: [" + expectedOutcome + "] for test [" + testCase
                            + "] — valid values: success | error");
        }
    }

    private void assertSuccess(String email, String testCase) {
        log.info("[{}] Asserting successful login for: {}", testCase, email);

        DashBoardPage dashBoardPage = new DashBoardPage(getPage());
        boolean emailDisplayed = dashBoardPage.validateEmailDisplay(email);

        Allure.step("Validating email on dashboard: " + email);

        Assert.assertTrue(emailDisplayed,
                "[" + testCase + "] Login succeeded but email [" + email + "] not displayed on dashboard");

        log.info("[{}] PASSED -> email displayed on dashboard", testCase);
    }

    private void assertErrors(
            LoginPage loginPage, String expectedLoginError, String expectedEmailError,
            String expectedPasswordError, String testCase) {

        if (isNotEmpty(expectedLoginError)) {
            log.info("[{}] Asserting login error: {}", testCase, expectedLoginError);
            String actual = loginPage.getErrorMessageLogin();
            Allure.step("Login error -> Expected: [" + expectedLoginError + "] | Actual: [" + actual + "]");
            Assert.assertEquals(actual, expectedLoginError, "[" + testCase + "] Login error message mismatch");
            log.info("[{}] PASSED -> Login error: {}", testCase, actual);
        }

        if (isNotEmpty(expectedEmailError)) {
            log.info("[{}] Asserting email error: {}", testCase, expectedEmailError);
            String actual = loginPage.getErrorMessageEmail();
            Allure.step("Email error -> Expected: [" + expectedEmailError + "] | Actual: [" + actual + "]");
            Assert.assertEquals(actual, expectedEmailError, "[" + testCase + "] Email error message mismatch");
            log.info("[{}] PASSED -> Email error: {}", testCase, actual);
        }

        if (isNotEmpty(expectedPasswordError)) {
            log.info("[{}] Asserting password error: {}", testCase, expectedPasswordError);
            String actual = loginPage.getErrorMessagePassword();
            Allure.step("Password error -> Expected: [" + expectedPasswordError + "] | Actual: [" + actual + "]");
            Assert.assertEquals(actual, expectedPasswordError, "[" + testCase + "] Password error message mismatch");
            log.info("[{}] PASSED -> Password error: {}", testCase, actual);
        }
    }

    private void logToReport(String testCase, String email, String expectedOutcome) {
        Allure.step("Test Case: " + testCase + " | Email: " + email + " | Expected Outcome: " + expectedOutcome);
    }

    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
