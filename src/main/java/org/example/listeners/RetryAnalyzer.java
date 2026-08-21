package org.example.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log = LogManager.getLogger(RetryAnalyzer.class);

    // How many times to retry a failed test.
    // MAX_RETRY = 2 means total 3 attempts (1 original + 2 retries).
    private static final int MAX_RETRY = 2;

    // ThreadLocal — critical for parallel execution. Each thread tracks its
    // OWN retry count independently, otherwise Thread-2 could reset
    // Thread-1's counter.
    private final ThreadLocal<Integer> retryCount = ThreadLocal.withInitial(() -> 0);

    @Override
    public boolean retry(ITestResult result) {
        int currentCount = retryCount.get();

        if (currentCount < MAX_RETRY) {
            retryCount.set(currentCount + 1);

            log.warn("Retrying test: [{}] | Attempt: {}/{}",
                    result.getMethod().getMethodName(),
                    retryCount.get(),
                    MAX_RETRY);

            if (result.getThrowable() != null) {
                log.warn("    Failure reason: {}", result.getThrowable().getMessage());
            }
            return true;
        }

        log.error("Test FAILED after {} attempts: [{}]",
                MAX_RETRY + 1, result.getMethod().getMethodName());

        retryCount.remove();
        return false;
    }
}
