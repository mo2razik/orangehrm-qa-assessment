package com.orangehrm.listeners;

import com.orangehrm.config.ConfigReader;
import com.orangehrm.driver.DriverManager;
import com.orangehrm.utils.ScreenshotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * - Captures a screenshot (and page source) as soon as a test method fails,
 *   while the browser is still open, and attaches it to the Allure report.
 * - Logs test start / result / duration.
 * - Writes Allure environment information.
 */
public class TestListener implements ITestListener, IInvokedMethodListener, ISuiteListener {

    private static final Logger LOG = LoggerFactory.getLogger(TestListener.class);

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult result) {
        if (method.isTestMethod() && result.getStatus() == ITestResult.FAILURE && DriverManager.hasDriver()) {
            ScreenshotUtils.failure(DriverManager.getDriver(), testName(result));
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        LOG.info("STARTED  {}", testName(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LOG.info("PASSED   {} ({} ms)", testName(result), duration(result));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        LOG.error("FAILED   {} ({} ms): {}", testName(result), duration(result),
                result.getThrowable() == null ? "" : result.getThrowable().getMessage());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LOG.warn("SKIPPED  {}", testName(result));
    }

    @Override
    public void onStart(ISuite suite) {
        writeAllureEnvironment();
    }

    private static String testName(ITestResult result) {
        return result.getTestClass().getRealClass().getSimpleName() + "." + result.getMethod().getMethodName();
    }

    private static long duration(ITestResult result) {
        return result.getEndMillis() - result.getStartMillis();
    }

    private static void writeAllureEnvironment() {
        try {
            Path dir = Paths.get(System.getProperty("allure.results.directory", "target/allure-results"));
            Files.createDirectories(dir);
            Properties env = new Properties();
            env.setProperty("Base URL", ConfigReader.baseUrl());
            env.setProperty("Browser", ConfigReader.browser());
            env.setProperty("Headless", String.valueOf(ConfigReader.headless()));
            env.setProperty("Java", System.getProperty("java.version"));
            env.setProperty("OS", System.getProperty("os.name"));
            try (OutputStream out = Files.newOutputStream(dir.resolve("environment.properties"))) {
                env.store(out, null);
            }
        } catch (IOException e) {
            LOG.warn("Could not write Allure environment: {}", e.getMessage());
        }
    }
}
