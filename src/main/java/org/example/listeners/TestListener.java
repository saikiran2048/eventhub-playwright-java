package org.example.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Pure logging listener. Unlike the old Selenium framework's TestListener,
 * this one does NOT push data into the report — Allure's own AllureTestNg
 * listener (registered alongside this one in testng.xml) does that
 * automatically by hooking the same TestNG events. Keeping this listener
 * separate means log4j output and report data can't drift out of sync with
 * each other.
 */
public class TestListener implements ITestListener {

    private static final Logger log = LogManager.getLogger(TestListener.class);

    @Override
    public void onStart(ITestContext context) {
        log.info("========================================");
        log.info("Suite Started : {}", context.getName());
        log.info("========================================");
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("========================================");
        log.info("Suite Finished: {}", context.getName());
        log.info("  Passed  : {}", context.getPassedTests().size());
        log.info("  Failed  : {}", context.getFailedTests().size());
        log.info("  Skipped : {}", context.getSkippedTests().size());
        log.info("========================================");
    }

    @Override
    public void onTestStart(ITestResult result) {
        log.info(">>> Starting: [{}.{}]",
                result.getTestClass().getName(),
                result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("PASSED : [{}] in {}s",
                result.getMethod().getMethodName(), getDurationSeconds(result));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String failReason = result.getThrowable() != null
                ? result.getThrowable().getMessage()
                : "Unknown failure";
        int retryCount = result.getMethod().getCurrentInvocationCount();

        if (retryCount > 1) {
            log.warn("RETRY #{} FAILED: [{}] | Reason: {}",
                    retryCount - 1, result.getMethod().getMethodName(), failReason);
        } else {
            log.error("FAILED : [{}] in {}s | Reason: {}",
                    result.getMethod().getMethodName(), getDurationSeconds(result), failReason);
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String reason = result.getThrowable() != null
                ? result.getThrowable().getMessage()
                : "No reason provided";
        log.warn("SKIPPED: [{}] | Reason: {}", result.getMethod().getMethodName(), reason);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        log.info("PASSED ON RETRY: [{}] after {} attempts",
                result.getMethod().getMethodName(),
                result.getMethod().getCurrentInvocationCount());
    }

    private long getDurationSeconds(ITestResult result) {
        return (result.getEndMillis() - result.getStartMillis()) / 1000;
    }
}
