package com.orangehrm.pages;

import com.orangehrm.config.ConfigReader;
import com.orangehrm.models.Employee;
import com.orangehrm.utils.TestDataFactory;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class AddEmployeePage extends BasePage {

    private static final By TITLE = By.xpath("//h6[normalize-space()='Add Employee']");
    private static final By FIRST_NAME = By.name("firstName");
    private static final By MIDDLE_NAME = By.name("middleName");
    private static final By LAST_NAME = By.name("lastName");
    private static final By EMPLOYEE_ID = inputByLabel("Employee Id");
    private static final By SAVE = By.cssSelector("button[type='submit']");
    private static final By FIELD_ERRORS = By.cssSelector(".oxd-input-field-error-message");
    private static final By EMPLOYEE_ID_ERROR = errorByLabel("Employee Id");
    private static final String DETAILS_URL = "/pim/viewPersonalDetails/empNumber/";
    private static final int MAX_SAVE_ATTEMPTS = 3;

    private String lastSaveMessage;

    public AddEmployeePage(WebDriver driver) {
        super(driver);
    }

    public AddEmployeePage waitUntilLoaded() {
        waitForUrlContains("/pim/addEmployee");
        visible(TITLE);
        waitForLoadersToDisappear();
        // the employee id is pre-filled asynchronously
        wait.until(d -> !value(EMPLOYEE_ID).isEmpty());
        return this;
    }

    @Step("Open Add Employee page directly")
    public AddEmployeePage open() {
        openPath("/pim/addEmployee");
        return waitUntilLoaded();
    }

    @Step("Fill employee: {employee}")
    public AddEmployeePage fill(Employee employee) {
        type(FIRST_NAME, employee.getFirstName());
        type(MIDDLE_NAME, employee.getMiddleName());
        type(LAST_NAME, employee.getLastName());
        if (employee.getEmployeeId() != null) {
            type(EMPLOYEE_ID, employee.getEmployeeId());
        }
        return this;
    }

    @Step("Set employee id \"{employeeId}\"")
    public AddEmployeePage setEmployeeId(String employeeId) {
        type(EMPLOYEE_ID, employeeId);
        return this;
    }

    public String getEmployeeId() {
        return value(EMPLOYEE_ID);
    }

    /** Notification shown by the last successful {@link #save()} (e.g. "Successfully Saved"). */
    public String getLastSaveMessage() {
        return lastSaveMessage;
    }

    /**
     * Clicks Save and waits for the redirect to the new employee's personal details.
     * On the shared demo another user can take the proposed Employee Id between page load
     * and save ("Employee Id already exists"); in that case a unique id is generated and
     * the save is retried.
     */
    @Step("Save employee")
    public PersonalDetailsPage save() {
        lastSaveMessage = null;
        for (int attempt = 1; attempt <= MAX_SAVE_ATTEMPTS; attempt++) {
            click(SAVE);
            // The success toast appears on this page right before the redirect and only for a
            // few seconds, so it is captured here while polling for the outcome every 100 ms.
            new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.explicitTimeout()), Duration.ofMillis(100))
                    .ignoring(StaleElementReferenceException.class)
                    .until(d -> {
                        String toast = currentToastText();
                        if (toast != null) {
                            lastSaveMessage = toast;
                        }
                        return d.getCurrentUrl().contains(DETAILS_URL) || !d.findElements(EMPLOYEE_ID_ERROR).isEmpty();
                    });
            if (currentUrl().contains(DETAILS_URL)) {
                PersonalDetailsPage details = new PersonalDetailsPage(driver);
                if (lastSaveMessage == null) {
                    lastSaveMessage = details.isToastDisplayed(5) ? details.waitForToastMessage() : "";
                }
                return details;
            }
            String error = text(EMPLOYEE_ID_ERROR);
            if (!error.contains("already exists")) {
                throw new IllegalStateException("Employee could not be saved: " + error);
            }
            setEmployeeId(TestDataFactory.uniqueEmployeeId());
        }
        throw new IllegalStateException("Employee could not be saved after " + MAX_SAVE_ATTEMPTS + " attempts");
    }

    /** Clicks Save when a validation error is expected (no redirect). */
    @Step("Save employee expecting validation errors")
    public AddEmployeePage saveExpectingErrors() {
        click(SAVE);
        visible(FIELD_ERRORS);
        return this;
    }

    public String getFirstNameError() {
        return errorUnderInput("firstName");
    }

    public String getLastNameError() {
        return errorUnderInput("lastName");
    }

    public String getEmployeeIdError() {
        return isVisible(errorByLabel("Employee Id"), 5) ? text(errorByLabel("Employee Id")) : "";
    }

    private String errorUnderInput(String inputName) {
        By error = By.xpath("//input[@name='" + inputName + "']"
                + "/following::span[contains(@class,'oxd-input-field-error-message')][1]");
        return isVisible(error, 5) ? text(error) : "";
    }

    public boolean isStillOnAddEmployeePage() {
        return currentUrl().contains("/pim/addEmployee") && isVisible(TITLE, 5);
    }
}
