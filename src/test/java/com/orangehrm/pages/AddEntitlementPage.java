package com.orangehrm.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Leave > Entitlements > Add Entitlements.
 * Used as test-data setup when the logged-in user has no leave balance.
 */
public class AddEntitlementPage extends BasePage {

    private static final By TITLE = By.xpath("//label[normalize-space()='Entitlement']");
    private static final By EMPLOYEE_NAME = inputByLabel("Employee Name");
    private static final By LEAVE_TYPE = selectByLabel("Leave Type");
    private static final By ENTITLEMENT = inputByLabel("Entitlement");
    private static final By SAVE = By.cssSelector("button[type='submit']");
    private static final By CONFIRM = buttonByText("Confirm");

    public AddEntitlementPage(WebDriver driver) {
        super(driver);
    }

    @Step("Open Leave > Entitlements > Add Entitlements")
    public AddEntitlementPage open() {
        openPath("/leave/addLeaveEntitlement");
        visible(TITLE);
        waitForLoadersToDisappear();
        return this;
    }

    public java.util.List<String> getLeaveTypes() {
        return listOptions(LEAVE_TYPE);
    }

    /**
     * Adds {@code days} of entitlement for the given employee and leave type.
     *
     * @return toast message displayed after confirming
     */
    @Step("Add {days} day(s) of \"{leaveType}\" entitlement for \"{firstName} {lastName}\"")
    public String addEntitlement(String firstName, String lastName, String leaveType, int days) {
        // type the first name only and pick the suggestion that also contains the last name,
        // so middle names or extra spaces in the stored name do not break the search
        typeAndPickSuggestion(EMPLOYEE_NAME, firstName, firstName, lastName)
                .orElseThrow(() -> new IllegalStateException("No employee suggestion for " + firstName + " " + lastName));
        chooseOption(LEAVE_TYPE, leaveType);
        type(ENTITLEMENT, String.valueOf(days));
        click(SAVE);
        // "Updating Entitlement" confirmation; the form loader stays visible behind the dialog
        if (isVisible(CONFIRM, 10)) {
            clickable(CONFIRM).click();
        }
        String toast = waitForToastMessage();
        // OrangeHRM then redirects to the entitlement list; wait for it so the redirect
        // does not override the next navigation of the test
        waitForUrlContains("/leave/viewLeaveEntitlements");
        waitForLoadersToDisappear();
        return toast;
    }
}
