package org.example.tests;

import org.example.base.BaseTest;
import org.example.pages.DashBoardPage;
import org.example.pages.LoginPage;
import org.example.utils.ConfigManager;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    @Test(description = "Verify that a user can log in with valid credentials and is redirected to the dashboard", priority = 0)
    public void testValidLogin() {
        log.info("Navigating to login page");
        getPage().navigate(ConfigManager.getBaseUrl());
        LoginPage loginPage = new LoginPage(getPage());
        loginPage.login("test4545@youmail.com", "q9sgCu@u8LddRZP");

        DashBoardPage dashBoardPage = new DashBoardPage(getPage());
        log.info("Validating that user email is displayed on dashboard");
        Assert.assertTrue(dashBoardPage.validateEmailDisplay("test4545@youmail.com"));
    }

    @Test(description = "Verify that an error message is displayed when a user attempts to log in with invalid credentials", priority = 1)
    public void testInvalidLogin() {
        log.info("Navigating to login page");
        getPage().navigate(ConfigManager.getBaseUrl());
        LoginPage loginPage = new LoginPage(getPage());
        loginPage.login("invalidemail@test.com", "Psswrd123");

        Assert.assertEquals(loginPage.getErrorMessageLogin(), "Invalid email or password");
    }

    @Test(description = "Verify that appropriate error messages are displayed when a user attempts to log in with empty fields", priority = 2)
    public void testEmptyFieldsLogin() {
        log.info("Navigating to login page");
        getPage().navigate(ConfigManager.getBaseUrl());
        LoginPage loginPage = new LoginPage(getPage());
        loginPage.login("", "");

        Assert.assertEquals(loginPage.getErrorMessageEmail(), "Enter a valid email");
        Assert.assertEquals(loginPage.getErrorMessagePassword(), "Password must be at least 6 characters");
    }

    @Test(description = "Verify that an appropriate error message is displayed when a user attempts to log in with an invalid email format", priority = 3)
    public void testInvalidEmailFormat() {
        log.info("Navigating to login page");
        getPage().navigate(ConfigManager.getBaseUrl());
        LoginPage loginPage = new LoginPage(getPage());
        loginPage.login("invalidemail", "validpassword66");

        Assert.assertEquals(loginPage.getErrorMessageEmail(), "Enter a valid email");
    }

    @Test(description = "Verify that an appropriate error message is displayed when a user attempts to log in with a password that is too short", priority = 4)
    public void testShortPassword() {
        log.info("Navigating to login page");
        getPage().navigate(ConfigManager.getBaseUrl());
        LoginPage loginPage = new LoginPage(getPage());
        loginPage.login(" test2334@test.com", "short");

        Assert.assertEquals(loginPage.getErrorMessagePassword(), "Password must be at least 6 characters");
    }
}
