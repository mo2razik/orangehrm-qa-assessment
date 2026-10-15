package com.orangehrm.pages;

import com.orangehrm.models.Employee;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class AddEmployeePage extends BasePage {

    private static final By TITLE = By.xpath("//h6[normalize-space()='Add Employee']");
    private static final By FIRST_NAME = By.name("firstName");
    private static final By MIDDLE_NAME = By.name("middleName");
    private static final By LAST_NAME = By.name("lastName");
    private static final By EMPLOYEE_ID = inputByLabel("Employee Id");
    private static final By SAVE = By.cssSelector("button[type='submit']");
    private static final By FIELD_ERRORS = By.cssSelector(".oxd-input-field-error-message");

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

    /** Clicks Save and waits for the redirect to the new employee's personal details. */
    @Step("Save employee")
    public PersonalDetailsPage save() {
        click(SAVE);
        return new PersonalDetailsPage(driver);
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
