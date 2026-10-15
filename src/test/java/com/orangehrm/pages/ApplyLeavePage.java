package com.orangehrm.pages;

import com.orangehrm.utils.DateUtils;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Leave > Apply.
 */
public class ApplyLeavePage extends BasePage {

    private static final By TITLE = By.xpath("//h6[normalize-space()='Apply Leave']");
    private static final By NO_BALANCE = By.xpath("//*[contains(normalize-space(),'No Leave Types with Leave Balance')]");
    private static final By LEAVE_TYPE = selectByLabel("Leave Type");
    private static final By LEAVE_BALANCE = By.xpath("//label[normalize-space()='Leave Balance']"
            + "/ancestor::div[contains(concat(' ',normalize-space(@class),' '),' oxd-input-group ')][1]//p");
    private static final By FROM_DATE = inputByLabel("From Date");
    private static final By TO_DATE = inputByLabel("To Date");
    private static final By COMMENTS = By.cssSelector("textarea");
    private static final By APPLY = buttonByText("Apply");
    private static final By OVERLAP = By.xpath("//*[contains(normalize-space(),'Overlapping Leave Request')]");
    private static final Pattern NUMBER = Pattern.compile("(-?\\d+(?:\\.\\d+)?)");

    public ApplyLeavePage(WebDriver driver) {
        super(driver);
    }

    @Step("Open Leave > Apply")
    public ApplyLeavePage open() {
        openPath("/leave/applyLeave");
        return waitUntilLoaded();
    }

    public ApplyLeavePage waitUntilLoaded() {
        waitForUrlContains("/leave/applyLeave");
        waitForLoadersToDisappear();
        wait.until(d -> !d.findElements(TITLE).isEmpty() || !d.findElements(NO_BALANCE).isEmpty());
        return this;
    }

    /**
     * The form is rendered first and replaced by the "No Leave Types with Leave Balance"
     * message once the leave types are loaded, so give the message a chance to appear.
     */
    public boolean hasLeaveTypesWithBalance() {
        if (isVisible(NO_BALANCE, 5)) {
            return false;
        }
        return isVisible(LEAVE_TYPE, 5);
    }

    public List<String> getAvailableLeaveTypes() {
        return listOptions(LEAVE_TYPE);
    }

    @Step("Select leave type \"{leaveType}\"")
    public ApplyLeavePage selectLeaveType(String leaveType) {
        chooseOption(LEAVE_TYPE, leaveType);
        waitForLoadersToDisappear();
        return this;
    }

    /** Balance shown after selecting a leave type, e.g. "10.00 Day(s)" -> 10.0. */
    public double getLeaveBalance() {
        wait.until(d -> NUMBER.matcher(d.findElement(LEAVE_BALANCE).getText()).find());
        Matcher m = NUMBER.matcher(text(LEAVE_BALANCE));
        return m.find() ? Double.parseDouble(m.group(1)) : 0;
    }

    /** The date format configured in OrangeHRM, read from the input placeholder (e.g. "yyyy-dd-mm"). */
    public String getDatePlaceholder() {
        return visible(FROM_DATE).getAttribute("placeholder");
    }

    @Step("Set From Date {date}")
    public ApplyLeavePage setFromDate(LocalDate date) {
        setDate(FROM_DATE, date);
        return this;
    }

    @Step("Set To Date {date}")
    public ApplyLeavePage setToDate(LocalDate date) {
        setDate(TO_DATE, date);
        return this;
    }

    private void setDate(By input, LocalDate date) {
        String formatted = DateUtils.format(date, getDatePlaceholder());
        type(input, formatted);
        WebElement field = visible(input);
        field.sendKeys(Keys.TAB);
        closeCalendar();
        wait.until(d -> formatted.equals(d.findElement(input).getAttribute("value")));
    }

    private void closeCalendar() {
        // clicking on the page title closes the date picker popup
        visible(TITLE).click();
    }

    public String getFromDateValue() {
        return value(FROM_DATE);
    }

    public String getToDateValue() {
        return value(TO_DATE);
    }

    @Step("Enter comment \"{comment}\"")
    public ApplyLeavePage setComment(String comment) {
        type(COMMENTS, comment);
        return this;
    }

    @Step("Click Apply")
    public ApplyLeavePage apply() {
        click(APPLY);
        return this;
    }

    public boolean isOverlapWarningDisplayed() {
        return isVisible(OVERLAP, 3);
    }

    public String getToDateError() {
        return isVisible(errorByLabel("To Date"), 5) ? text(errorByLabel("To Date")) : "";
    }

    public String getLeaveTypeError() {
        return isVisible(errorByLabel("Leave Type"), 5) ? text(errorByLabel("Leave Type")) : "";
    }

    public String getFromDateError() {
        return isVisible(errorByLabel("From Date"), 5) ? text(errorByLabel("From Date")) : "";
    }

    public boolean isOnApplyPage() {
        return currentUrl().contains("/leave/applyLeave") && isVisible(TITLE, 5);
    }
}
