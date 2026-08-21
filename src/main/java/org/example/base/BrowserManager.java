package org.example.base;

import com.microsoft.playwright.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.utils.ConfigManager;

import java.nio.file.Paths;

/**
 * Playwright equivalent of the old Selenium DriverManager.
 *
 * Selenium had ONE object to juggle per thread (WebDriver). Playwright has
 * FOUR, each a layer inside the last:
 *
 *   Playwright        -> the driver process itself (one per thread is simplest/safest)
 *     -> Browser       -> the actual browser binary (chromium/firefox/webkit)
 *       -> BrowserContext -> an isolated "incognito" session (cookies/storage)
 *         -> Page       -> a single tab — this is what your Page Objects talk to
 *
 * Everything is ThreadLocal for the same reason as before: parallel TestNG
 * threads must never share a Page/Context, or they'll collide on navigation
 * and end up asserting against each other's state.
 */
public class BrowserManager {

    private static final Logger log = LogManager.getLogger(BrowserManager.class);

    private static final ThreadLocal<Playwright> playwrightThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browserThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> contextThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();

    private BrowserManager() {
        throw new IllegalStateException(
                "BrowserManager is a static utility class — do not instantiate");
    }

    // ─── Public API ────────────────────────────────────────────────

    /** Called by BaseTest @BeforeMethod — once per test, using config default browser. */
    public static void initBrowser() {
        initBrowser(System.getProperty("browser", ConfigManager.getBrowser()));
    }

    /**
     * Overload that accepts an explicit browser value — used when browser comes
     * from TestNG's @Parameter (per <test> block in testng.xml), exactly like the
     * old DriverManager.initDriver(String) did for the chrome/firefox/edge matrix.
     * Playwright's matrix is chromium/firefox/webkit.
     */
    public static void initBrowser(String browserParam) {
        String browser = browserParam;
        long threadId = Thread.currentThread().getId();
        boolean headless = ConfigManager.isHeadless();

        log.info("[Thread-{}] Launching {} | headless={}", threadId, browser, headless);

        Playwright playwright = Playwright.create();
        playwrightThreadLocal.set(playwright);

        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(headless);

        Browser browserInstance = switch (browser.toLowerCase()) {
            case "chromium", "chrome" -> playwright.chromium().launch(launchOptions);
            case "firefox" -> playwright.firefox().launch(launchOptions);
            case "webkit", "safari" -> playwright.webkit().launch(launchOptions);
            default -> throw new RuntimeException(
                    "Browser not supported: [" + browser + "] "
                            + "| Supported values: chromium, firefox, webkit");
        };
        browserThreadLocal.set(browserInstance);

        BrowserContext context = browserInstance.newContext(
                new Browser.NewContextOptions().setViewportSize(1920, 1080));
        context.setDefaultTimeout(ConfigManager.getDefaultTimeout());
        context.setDefaultNavigationTimeout(ConfigManager.getNavigationTimeout());

        // Start tracing — captured per-test, saved only on failure in BaseTest.tearDown.
        // This is Playwright's built-in equivalent of the old failure screenshot,
        // but far richer: a full timeline you can replay in Trace Viewer.
        context.tracing().start(new com.microsoft.playwright.Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));

        contextThreadLocal.set(context);

        Page page = context.newPage();
        pageThreadLocal.set(page);

        log.info("[Thread-{}] Browser ready", threadId);
    }

    /** Returns the Page for the calling thread — this is what Page Objects use. */
    public static Page getPage() {
        Page page = pageThreadLocal.get();
        if (page == null) {
            throw new IllegalStateException(
                    "[Thread-" + Thread.currentThread().getId() + "] "
                            + "Page not initialized. "
                            + "Ensure initBrowser() was called in @BeforeMethod");
        }
        return page;
    }

    public static BrowserContext getContext() {
        return contextThreadLocal.get();
    }

    /**
     * Stops tracing and saves the trace zip — call from BaseTest.tearDown on failure only,
     * traces are large and you don't want one for every green test.
     */
    public static void saveTrace(String traceName) {
        BrowserContext context = contextThreadLocal.get();
        if (context != null) {
            context.tracing().stop(new com.microsoft.playwright.Tracing.StopOptions()
                    .setPath(Paths.get("traces/" + traceName + ".zip")));
            log.info("Trace saved: traces/{}.zip", traceName);
        }
    }

    /** Tears down all four layers for the calling thread, in reverse order. */
    public static void quitBrowser() {
        long threadId = Thread.currentThread().getId();
        log.info("[Thread-{}] Closing browser", threadId);

        Page page = pageThreadLocal.get();
        if (page != null) {
            page.close();
            pageThreadLocal.remove();
        }
        BrowserContext context = contextThreadLocal.get();
        if (context != null) {
            context.close();
            contextThreadLocal.remove();
        }
        Browser browser = browserThreadLocal.get();
        if (browser != null) {
            browser.close();
            browserThreadLocal.remove();
        }
        Playwright playwright = playwrightThreadLocal.get();
        if (playwright != null) {
            playwright.close();
            playwrightThreadLocal.remove(); // MUST remove — prevents leaks across CI runs
        }
    }
}
