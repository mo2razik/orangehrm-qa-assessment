package com.orangehrm.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * PIM > Employee > Personal Details.
 */
public class PersonalDetailsPage extends BasePage {

    private static final By EMPLOYEE_NAME_HEADER = By.cssSelector(".orangehrm-edit-employee-name h6");
    private static final By SECTION_TITLE = By.xpath("//h6[normalize-space()='Personal Details']");
    private static final By FIRST_NAME = By.name("firstName");
    private static final By MIDDLE_NAME = By.name("middleName");
    private static final By LAST_NAME = By.name("lastName");
    private static final By EMPLOYEE_ID = inputByLabel("Employee Id");
    private static final By OTHER_ID = inputByLabel("Other Id");
    private static final By SAVE_PERSONAL_DETAILS = By.xpath(
            "//h6[normalize-space()='Personal Details']/following::form[1]//button[@type='submit']");
    private static final By FIRST_NAME_ERROR = By.xpath("//input[@name='firstName']"
            + "/following::span[contains(@class,'oxd-input-field-error-message')][1]");

    public PersonalDetailsPage(WebDriver driver) {
        super(driver);
    }

    /** Waits for the page and for the form to be populated from the API. */
    @Step("Wait for Personal Details page")
    public PersonalDetailsPage waitUntilLoaded() {
        waitForUrlContains("/pim/viewPersonalDetails/empNumber/");
        visible(SECTION_TITLE);
        waitForLoadersToDisappear();
        wait.until(d -> !value(FIRST_NAME).isEmpty());
        return this;
    }

    /** Opens the logged-in user's own record (My Info). */
    @Step("Open My Info")
    public PersonalDetailsPage openMyInfo() {
        openPath("/pim/viewMyDetails");
        return waitUntilLoaded();
    }

    public String getEmpNumberFromUrl() {
        String url = currentUrl();
        return url.substring(url.lastIndexOf('/') + 1);
    }

    public String getHeaderName() {
        wait.until(d -> !d.findElement(EMPLOYEE_NAME_HEADER).getText().isBlank());
        return text(EMPLOYEE_NAME_HEADER);
    }

    public String getFirstName() {
        return value(FIRST_NAME);
    }

    public String getMiddleName() {
        return value(MIDDLE_NAME);
    }

    public String getLastName() {
        return value(LAST_NAME);
    }

    public String getEmployeeId() {
        return value(EMPLOYEE_ID);
    }

    public String getOtherId() {
        return value(OTHER_ID);
    }

    @Step("Change first name to \"{firstName}\"")
    public PersonalDetailsPage setFirstName(String firstName) {
        type(FIRST_NAME, firstName);
        return this;
    }

    @Step("Change last name to \"{lastName}\"")
    public PersonalDetailsPage setLastName(String lastName) {
        type(LAST_NAME, lastName);
        return this;
    }

    @Step("Change Other Id to \"{otherId}\"")
    public PersonalDetailsPage setOtherId(String otherId) {
        type(OTHER_ID, otherId);
        return this;
    }

    @Step("Save personal details")
    public String save() {
        click(SAVE_PERSONAL_DETAILS);
        return waitForToastMessage();
    }

    @Step("Save personal details expecting a validation error")
    public PersonalDetailsPage saveExpectingError() {
        click(SAVE_PERSONAL_DETAILS);
        visible(FIRST_NAME_ERROR);
        return this;
    }

    public String getFirstNameError() {
        return isVisible(FIRST_NAME_ERROR, 5) ? text(FIRST_NAME_ERROR) : "";
    }

    @Step("Reload Personal Details page")
    public PersonalDetailsPage reload() {
        refresh();
        return waitUntilLoaded();
    }
}
