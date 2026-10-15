package com.orangehrm.flows;

import com.orangehrm.config.ConfigReader;
import com.orangehrm.pages.AddEntitlementPage;
import com.orangehrm.pages.ApplyLeavePage;
import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;

import java.util.List;

/**
 * Reusable leave set-up steps.
 */
public final class LeaveFlows {

    private LeaveFlows() {
    }

    /**
     * Makes sure the logged-in user has at least one leave type with a usable balance.
     * When none is available an entitlement is added (Leave > Entitlements) as data set-up.
     *
     * @return the leave type to use, with the Apply Leave page open and that type selected
     */
    @Step("Ensure the logged-in user has a leave type with balance")
    public static String prepareLeaveTypeWithBalance(WebDriver driver) {
        ApplyLeavePage apply = new ApplyLeavePage(driver).open();
        String userName = apply.getLoggedInUserName();

        if (apply.hasLeaveTypesWithBalance()) {
            List<String> types = apply.getAvailableLeaveTypes();
            for (String type : types) {
                apply.selectLeaveType(type);
                if (apply.getLeaveBalance() >= 1) {
                    return type;
                }
            }
            // types exist but none has enough balance -> top up the first one
            return topUpAndSelect(driver, userName, types.get(0));
        }

        AddEntitlementPage entitlementPage = new AddEntitlementPage(driver).open();
        String type = entitlementPage.getLeaveTypes().get(0);
        return topUpAndSelect(driver, userName, type);
    }

    private static String topUpAndSelect(WebDriver driver, String userName, String leaveType) {
        new AddEntitlementPage(driver).open()
                .addEntitlement(userName, leaveType, ConfigReader.getInt("leave.entitlement.days"));
        ApplyLeavePage apply = new ApplyLeavePage(driver).open();
        if (!apply.hasLeaveTypesWithBalance()) {
            throw new IllegalStateException("Leave balance still unavailable after adding an entitlement for "
                    + userName + " / " + leaveType);
        }
        apply.selectLeaveType(leaveType);
        return leaveType;
    }
}
