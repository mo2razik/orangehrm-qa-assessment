package com.orangehrm.pages;

import com.orangehrm.models.EmployeeRow;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PIM > Employee List. Rows are always located by their content
 * (employee id / name), never by a fixed row index.
 */
public class EmployeeListPage extends BasePage {

    public static final String EMPLOYEE_NAME = "Employee Name";
    public static final String EMPLOYEE_ID = "Employee Id";
    public static final String EMPLOYMENT_STATUS = "Employment Status";
    public static final String JOB_TITLE = "Job Title";
    public static final String SUB_UNIT = "Sub Unit";

    private static final By FILTER_TITLE = By.xpath("//h5[normalize-space()='Employee Information']");
    private static final By SEARCH = buttonByText("Search");
    private static final By RESET = buttonByText("Reset");
    private static final By RECORDS_FOUND = By.xpath(
            "//div[contains(@class,'orangehrm-horizontal-padding')]//span[contains(normalize-space(),'Found')]");
    private static final By CONFIRM_DELETE = buttonByText("Yes, Delete");
    private static final Pattern COUNT = Pattern.compile("\\((\\d+)\\)");

    public EmployeeListPage(WebDriver driver) {
        super(driver);
    }

    public EmployeeListPage waitUntilLoaded() {
        waitForUrlContains("/pim/viewEmployeeList");
        visible(FILTER_TITLE);
        waitForLoadersToDisappear();
        visible(RECORDS_FOUND);
        return this;
    }

    @Step("Open Employee List")
    public EmployeeListPage open() {
        openPath("/pim/viewEmployeeList");
        return waitUntilLoaded();
    }

    @Step("Open Add Employee page")
    public AddEmployeePage goToAddEmployee() {
        openTopBarItem("Add Employee");
        return new AddEmployeePage(driver).waitUntilLoaded();
    }

    // ------------------------------------------------------------- filters

    /**
     * Types a name into the Employee Name autocomplete. When a suggestion containing
     * every given part is offered it is selected (exact match), otherwise the typed text
     * is used as a partial-name filter.
     */
    @Step("Enter employee name \"{name}\"")
    public EmployeeListPage enterEmployeeName(String name, String... suggestionMustContain) {
        String[] parts = suggestionMustContain.length > 0 ? suggestionMustContain : name.split("\\s+");
        typeAndPickSuggestion(inputByLabel(EMPLOYEE_NAME), name, parts);
        return this;
    }

    /** Types a partial name without choosing a suggestion (free-text "contains" filter). */
    @Step("Enter partial employee name \"{text}\"")
    public EmployeeListPage enterPartialEmployeeName(String text) {
        type(inputByLabel(EMPLOYEE_NAME), text);
        visible(FILTER_TITLE).click(); // close the suggestion list
        return this;
    }

    @Step("Enter employee id \"{employeeId}\"")
    public EmployeeListPage enterEmployeeId(String employeeId) {
        type(inputByLabel(EMPLOYEE_ID), employeeId);
        return this;
    }

    @Step("Select employment status \"{status}\"")
    public EmployeeListPage selectEmploymentStatus(String status) {
        chooseOption(selectByLabel(EMPLOYMENT_STATUS), status);
        return this;
    }

    @Step("Select job title \"{jobTitle}\"")
    public EmployeeListPage selectJobTitle(String jobTitle) {
        chooseOption(selectByLabel(JOB_TITLE), jobTitle);
        return this;
    }

    public String getEmployeeNameFilterValue() {
        return value(inputByLabel(EMPLOYEE_NAME));
    }

    public String getEmployeeIdFilterValue() {
        return value(inputByLabel(EMPLOYEE_ID));
    }

    public String getSelectedJobTitle() {
        return selectedText(selectByLabel(JOB_TITLE));
    }

    public String getSelectedEmploymentStatus() {
        return selectedText(selectByLabel(EMPLOYMENT_STATUS));
    }

    @Step("Click Search")
    public EmployeeListPage search() {
        click(SEARCH);
        waitForResults();
        return this;
    }

    @Step("Click Reset")
    public EmployeeListPage reset() {
        click(RESET);
        waitForResults();
        return this;
    }

    private void waitForResults() {
        waitForLoadersToDisappear();
        visible(RECORDS_FOUND);
    }

    @Step("Search employee by id \"{employeeId}\"")
    public EmployeeListPage searchById(String employeeId) {
        return enterEmployeeId(employeeId).search();
    }

    // -------------------------------------------------------------- results

    /** Value of "(N) Records Found", 0 for "No Records Found". */
    public int getRecordCount() {
        String text = text(RECORDS_FOUND);
        Matcher m = COUNT.matcher(text);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }

    public String getRecordsFoundText() {
        return text(RECORDS_FOUND);
    }

    /** Reads every row on the current page into {@link EmployeeRow}s using the header names. */
    public List<EmployeeRow> getRows() {
        return readTable().stream()
                .map(r -> new EmployeeRow(
                        r.getOrDefault("Id", ""),
                        r.getOrDefault("First (& Middle) Name", ""),
                        r.getOrDefault("Last Name", ""),
                        r.getOrDefault("Job Title", ""),
                        r.getOrDefault("Employment Status", ""),
                        r.getOrDefault("Sub Unit", ""),
                        r.getOrDefault("Supervisor", "")))
                .toList();
    }

    public Optional<EmployeeRow> findRow(Predicate<EmployeeRow> condition) {
        return getRows().stream().filter(condition).findFirst();
    }

    public Optional<EmployeeRow> findRowById(String employeeId) {
        return findRow(r -> r.id().equals(employeeId));
    }

    private WebElement rowCardWithId(String employeeId) {
        By card = By.xpath("//div[contains(@class,'oxd-table-body')]//div[contains(@class,'oxd-table-card')]"
                + "[.//div[contains(@class,'oxd-table-cell')]/div[normalize-space()='" + employeeId + "']]");
        return wait.until(ExpectedConditions.visibilityOfElementLocated(card));
    }

    @Step("Open employee with id \"{employeeId}\" from the result list")
    public PersonalDetailsPage openEmployee(String employeeId) {
        rowCardWithId(employeeId).findElement(By.cssSelector(".oxd-table-row")).click();
        return new PersonalDetailsPage(driver).waitUntilLoaded();
    }

    @Step("Delete employee with id \"{employeeId}\"")
    public String deleteEmployee(String employeeId) {
        WebElement card = rowCardWithId(employeeId);
        card.findElement(By.cssSelector("i.bi-trash")).click();
        click(CONFIRM_DELETE);
        String message = waitForToastMessage();
        waitForLoadersToDisappear();
        return message;
    }
}
