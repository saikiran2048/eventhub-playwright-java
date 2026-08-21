package org.example.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Note the big structural difference from the Selenium version: no @FindBy,
 * no PageFactory, no WebDriverWait. Locator is lazy — creating it doesn't
 * search the DOM yet, and every action on it (click/fill/etc.) auto-waits
 * for the element to be actionable first. That removes an entire category
 * of flaky-wait bugs the old framework had to hand-manage.
 */
public class LoginPage {

    private final Page page;
    private static final Logger log = LogManager.getLogger(LoginPage.class);

    private final Locator emailField;
    private final Locator passwordField;
    private final Locator loginButton;
    private final Locator registerLink;
    private final Locator errorMessageLogin;
    private final Locator errorMessageEmail;
    private final Locator errorMessagePassword;

    public LoginPage(Page page) {
        this.page = page;
        this.emailField = page.locator("#email");
        this.passwordField = page.locator("#password");
        this.loginButton = page.locator("#login-btn");
        this.registerLink = page.getByText("Register");
        this.errorMessageLogin = page.locator("p", new Page.LocatorOptions().setHasText("Invalid email or password"));
        this.errorMessageEmail = page.locator("p", new Page.LocatorOptions().setHasText("Enter a valid email"));
        this.errorMessagePassword = page.locator("p", new Page.LocatorOptions().setHasText("Password must be at least 6 characters"));
    }

    public void enterEmail(String email) {
        log.info("Entering email: {}", email);
        emailField.fill(email);
    }

    public void enterPassword(String password) {
        log.info("Entering password: {}", password);
        passwordField.fill(password);
    }

    public void clickLoginButton() {
        log.info("Clicking login button");
        loginButton.click();
    }

    public void clickRegisterLink() {
        log.info("Clicking register link");
        registerLink.click();
    }

    public String getErrorMessageLogin() {
        errorMessageLogin.waitFor();
        String text = errorMessageLogin.textContent();
        log.info("Error message for login: {}", text);
        return text;
    }

    public String getErrorMessageEmail() {
        errorMessageEmail.waitFor();
        return errorMessageEmail.textContent();
    }

    public String getErrorMessagePassword() {
        errorMessagePassword.waitFor();
        return errorMessagePassword.textContent();
    }

    public void login(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickLoginButton();
    }
}
