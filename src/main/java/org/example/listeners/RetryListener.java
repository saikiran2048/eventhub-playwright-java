package org.example.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IAnnotationTransformer;
import org.testng.IRetryAnalyzer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class RetryListener implements IAnnotationTransformer {

    private static final Logger log = LogManager.getLogger(RetryListener.class);

    @Override
    public void transform(
            ITestAnnotation annotation,
            Class testClass,
            Constructor testConstructor,
            Method testMethod) {

        Class<? extends IRetryAnalyzer> existingAnalyzer = annotation.getRetryAnalyzerClass();

        if (existingAnalyzer == null) {
            annotation.setRetryAnalyzer(RetryAnalyzer.class);

            log.debug("RetryAnalyzer applied -> [{}.{}]",
                    testMethod.getDeclaringClass().getSimpleName(),
                    testMethod.getName());
        }
    }
}
