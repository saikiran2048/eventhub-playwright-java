package org.example.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class DashBoardPage {

    private final Page page;

    private final Locator emailDisplay;
    private final Locator homeNav;
    private final Locator eventsNav;
    private final Locator bookingsNav;
    private final Locator adminButton;
    private final Locator adminEventsLink;
    private final Locator adminBookingsLink;
    private final Locator logoutButton;

    public DashBoardPage(Page page) {
        this.page = page;
        this.emailDisplay = page.locator("#user-email-display");
        this.homeNav = page.locator("#nav-home");
        this.eventsNav = page.locator("#nav-events");
        this.bookingsNav = page.locator("#nav-bookings");
        this.adminButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Admin"));
        this.adminEventsLink = page.locator("a[href='/admin/events']");
        this.adminBookingsLink = page.locator("a[href='/admin/bookings']");
        this.logoutButton = page.locator("#logout-btn");
    }

    public boolean validateEmailDisplay(String expectedEmail) {
        emailDisplay.waitFor();
        String actualEmail = emailDisplay.textContent();
        if (!actualEmail.equals(expectedEmail)) {
            throw new AssertionError("Expected email: " + expectedEmail + ", but got: " + actualEmail);
        }
        return true;
    }
}
