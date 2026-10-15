package com.orangehrm.base;

import com.orangehrm.config.ConfigReader;
import com.orangehrm.driver.DriverManager;
import com.orangehrm.listeners.TestListener;
import com.orangehrm.pages.DashboardPage;
import com.orangehrm.pages.EmployeeListPage;
import com.orangehrm.pages.LoginPage;
import com.orangehrm.utils.ScreenshotUtils;
import com.orangehrm.utils.TestDataRegistry;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import java.util.Set;

/**
 * Starts a fresh browser for every test method (and logs in unless the test opts out),
 * closes it afterwards and removes test data created during the run.
 */
@Listeners(TestListener.class)
public abstract class BaseTest {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /** Override and return false for tests that exercise the login page themselves. */
    protected boolean loginBeforeEachTest() {
        return true;
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        DriverManager.startDriver();
        if (loginBeforeEachTest()) {
            new LoginPage(driver()).open().loginWithConfiguredUser();
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverManager.quitDriver();
    }

    protected WebDriver driver() {
        return DriverManager.getDriver();
    }

    protected DashboardPage dashboard() {
        return new DashboardPage(driver());
    }

    protected void checkpoint(String name) {
        ScreenshotUtils.checkpoint(driver(), name);
    }

    /**
     * Deletes every employee registered in {@link TestDataRegistry} so the shared
     * demo environment is not polluted. Runs once, after the whole suite.
     */
    @AfterSuite(alwaysRun = true)
    public void cleanUpTestData() {
        Set<String> ids = TestDataRegistry.createdEmployeeIds();
        if (!ConfigReader.getBoolean("cleanup.enabled") || ids.isEmpty()) {
            return;
        }
        log.info("Cleaning up {} employee(s) created during the run: {}", ids.size(), ids);
        try {
            DriverManager.startDriver();
            new LoginPage(driver()).open().loginWithConfiguredUser();
            EmployeeListPage list = new EmployeeListPage(driver());
            for (String id : ids) {
                try {
                    list.open().searchById(id);
                    if (list.findRowById(id).isPresent()) {
                        list.deleteEmployee(id);
                        log.info("Deleted employee {}", id);
                    }
                    TestDataRegistry.unregisterEmployee(id);
                } catch (RuntimeException e) {
                    log.warn("Could not delete employee {}: {}", id, e.getMessage());
                }
            }
        } catch (RuntimeException e) {
            log.warn("Test data cleanup failed: {}", e.getMessage());
        } finally {
            DriverManager.quitDriver();
        }
    }
}
