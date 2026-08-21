# EventHub Playwright Java Framework

A Playwright + Java automation framework for the [EventHub practice app](https://eventhub.rahulshettyacademy.com),
covering UI and API testing in a single Maven project.

## Architecture

- **`BrowserManager`** — manages the Playwright session per thread (Playwright
  instance → Browser → BrowserContext → Page), all backed by `ThreadLocal` so
  parallel TestNG threads never share state. Also owns tracing: every test's
  context starts a trace, and `BaseTest.tearDown` saves it to disk on failure.
- **`BaseTest`** — base class for UI tests. Loads config once per suite,
  launches a browser per test (`@Parameters("browser")` drives
  chromium/firefox/webkit), and on failure attaches a screenshot to the Allure
  report plus saves a full trace.
- **`BaseApiTest`** — separate base class for pure API tests (no browser at
  all — initializes `ApiClient` once per suite instead).
- **Page Objects (`pages/`)** — built on Playwright `Locator`, which is lazy
  (doesn't query the DOM until you act on it) and auto-waits on every action,
  so there's no explicit wait code anywhere in the page classes.
- **API layer (`api/`)** — `ApiClient` wraps Playwright's native
  `APIRequestContext`, so API calls run through the same Playwright process as
  UI tests with no extra HTTP library. `ApiResponse` gives tests a small,
  stable surface (`statusCode()`, `asJson()`, `jsonPath()`, etc.) instead of
  coupling test code directly to the Playwright response type.
- **Listeners (`listeners/`)** — `RetryAnalyzer`/`RetryListener` auto-retry
  failed tests (2 retries by default); `TestListener` logs suite/test
  lifecycle events via log4j2. Allure's own `AllureTestNg` listener is
  registered alongside these in `testng.xml` and handles report data
  independently.
- **Reporting** — [Allure](https://allurereport.org/) (`allure-testng`),
  chosen because its TestNG adapter hooks the test lifecycle automatically —
  no manual step-by-step report bookkeeping in test code — and its attachment
  model pairs naturally with Playwright's screenshot/trace output.
- **Config (`utils/ConfigManager`)** — env-driven properties
  (`config-dev/test/staging.properties`), selected via `-Denv=`.
- **Data-driven testing (`utils/JsonUtil`, `utils/ExcelUtil`)** — read test
  data from JSON or Excel into TestNG `@DataProvider` format, with a
  `runMode`/`RunMode` column to selectively skip rows.

## Project structure

```
src/main/java/org/example/
  base/       BrowserManager, BaseTest, BaseApiTest
  pages/      LoginPage, DashBoardPage
  api/        ApiEndpoints, ApiClient, ApiResponse
  listeners/  RetryAnalyzer, RetryListener, TestListener
  utils/      ConfigManager, JsonUtil, ExcelUtil
  resources/  config-{dev,test,staging}.properties, log4j2.xml

src/test/java/org/example/
  tests/      LoginTest, DataDrivenLoginTest
  api/        LoginApiTest
  resources/  testng.xml, allure.properties, testdata/(loginData.json, loginData.xlsx)

.github/workflows/run-tests.yml   CI: installs browsers, runs suite, publishes Allure report
```

## Running locally

```bash
# install dependencies + Playwright browser binaries
mvn install
mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps"

# run the full suite (defaults to env=test, headless=true)
mvn test

# run against a specific environment, headed, for local debugging
mvn test -Denv=dev -Dheadless=false
```

## Viewing the report

```bash
# after a test run, results land in target/allure-results
allure serve target/allure-results
```

(Install the Allure CLI once: `npm i -g allure-commandline` or via a package
manager — it's a report generator, not a test dependency, so it stays out of
`pom.xml`.)

## Debugging a failed test with Trace Viewer

On any test failure, `BaseTest.tearDown` saves a trace to `traces/<testName>.zip`
in addition to the Allure screenshot. Open it with:

```bash
npx playwright show-trace traces/testValidLogin.zip
```

(This uses Playwright's own CLI via `npx` — a one-time Node/npm dependency
just for viewing traces locally; the traces themselves are produced entirely
by the Java framework.)

## Browser matrix

`testng.xml` runs `LoginTest` and `DataDrivenLoginTest` across all three
Playwright engines in parallel (`chromium`, `firefox`, `webkit`), plus a
separate, unparallelized API test block for `LoginApiTest`.

## Notes

- `DashBoardPage`'s `#nav-*` and admin-menu selectors should be verified
  against the live app before relying on them — they were written from the
  page's known structure and may need adjusting if the UI has changed.
- `LoginApiTest`'s `token`/`error` JSON field names are placeholders — adjust
  the `ApiResponse.jsonPath(...)` calls to match EventHub's actual response
  shape once you've confirmed it against `/api/docs`.