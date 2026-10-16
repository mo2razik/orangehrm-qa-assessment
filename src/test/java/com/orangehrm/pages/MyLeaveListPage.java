package com.orangehrm.pages;

import com.orangehrm.models.LeaveRow;
import com.orangehrm.utils.DateUtils;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Leave > My Leave.
 */
public class MyLeaveListPage extends BasePage {

    private static final By TITLE = By.xpath("//h5[normalize-space()='My Leave List']");
    private static final By FROM_DATE = inputByLabel("From Date");
    private static final By TO_DATE = inputByLabel("To Date");
    private static final By SEARCH = buttonByText("Search");
    private static final By RECORDS_FOUND = By.xpath("//span[contains(normalize-space(),'Found')]");

    public MyLeaveListPage(WebDriver driver) {
        super(driver);
    }

    @Step("Open Leave > My Leave")
    public MyLeaveListPage open() {
        openPath("/leave/viewMyLeaveList");
        visible(TITLE);
        waitForLoadersToDisappear();
        return this;
    }

    /** Narrows the list to a date range so the request is found regardless of paging. */
    @Step("Filter My Leave from {from} to {to}")
    public MyLeaveListPage filterByDateRange(LocalDate from, LocalDate to) {
        String placeholder = visible(FROM_DATE).getAttribute("placeholder");
        setDate(FROM_DATE, DateUtils.format(from, placeholder));
        setDate(TO_DATE, DateUtils.format(to, placeholder));
        click(SEARCH);
        waitForLoadersToDisappear();
        visible(RECORDS_FOUND);
        return this;
    }

    /** The request carrying the given unique comment, whatever its date. */
    public Optional<LeaveRow> findByComment(String comment) {
        return getRows().stream().filter(r -> r.comments().contains(comment)).findFirst();
    }

    private void setDate(By input, String formatted) {
        type(input, formatted);
        visible(input).sendKeys(Keys.TAB);
        visible(TITLE).click();
    }

    public String formatForList(LocalDate date) {
        return DateUtils.format(date, visible(FROM_DATE).getAttribute("placeholder"));
    }

    public List<LeaveRow> getRows() {
        return readTable().stream()
                .map(r -> new LeaveRow(
                        r.getOrDefault("Date", ""),
                        r.getOrDefault("Leave Type", ""),
                        r.getOrDefault("Number of Days", ""),
                        r.getOrDefault("Status", ""),
                        r.getOrDefault("Comments", "")))
                .toList();
    }

    public Optional<LeaveRow> findRequest(LocalDate date, String leaveType, String comment) {
        String formatted = formatForList(date);
        return getRows().stream()
                .filter(r -> r.date().contains(formatted))
                .filter(r -> r.leaveType().equals(leaveType))
                .filter(r -> comment == null || r.comments().contains(comment))
                .findFirst();
    }

    /** Cancels the pending request that carries the given unique comment. */
    @Step("Cancel leave request with comment \"{comment}\"")
    public String cancelRequest(String comment) {
        WebElement card = visible(By.xpath("//div[contains(@class,'oxd-table-body')]//div[contains(@class,'oxd-table-card')]"
                + "[.//div[contains(@class,'oxd-table-cell')][contains(normalize-space(),'" + comment + "')]]"));
        card.findElement(By.xpath(".//button[normalize-space()='Cancel']")).click();
        return waitForToastMessage();
    }
}
