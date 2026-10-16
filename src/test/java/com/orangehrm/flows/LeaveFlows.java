package com.orangehrm.flows;

import com.orangehrm.config.ConfigReader;
import com.orangehrm.pages.AddEntitlementPage;
import com.orangehrm.pages.ApplyLeavePage;
import com.orangehrm.pages.PersonalDetailsPage;
import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;
import org.testng.SkipException;

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

        if (apply.hasLeaveTypesWithBalance()) {
            List<String> types = apply.getAvailableLeaveTypes();
            for (String type : types) {
                apply.selectLeaveType(type);
                if (apply.getLeaveBalance() >= 1) {
                    return type;
                }
            }
            // types exist but none has enough balance -> top up the first one
            return topUpAndSelect(driver, types.get(0));
        }

        List<String> definedTypes = new AddEntitlementPage(driver).open().getLeaveTypes();
        if (definedTypes.isEmpty()) {
            // the shared demo is edited by many people; without any leave type no request is possible
            throw new SkipException("Precondition not met: no leave types are defined in the demo environment");
        }
        return topUpAndSelect(driver, definedTypes.get(0));
    }

    private static String topUpAndSelect(WebDriver driver, String leaveType) {
        // the name in the top bar is not always the stored employee name (demo users rename it),
        // so the logged-in employee's name is read from My Info
        PersonalDetailsPage myInfo = new PersonalDetailsPage(driver).openMyInfo();
        String firstName = myInfo.getFirstName();
        String lastName = myInfo.getLastName();

        new AddEntitlementPage(driver).open()
                .addEntitlement(firstName, lastName, leaveType, ConfigReader.getInt("leave.entitlement.days"));
        ApplyLeavePage apply = new ApplyLeavePage(driver).open();
        if (!apply.hasLeaveTypesWithBalance()) {
            throw new IllegalStateException("Leave balance still unavailable after adding an entitlement for "
                    + firstName + " " + lastName + " / " + leaveType);
        }
        apply.selectLeaveType(leaveType);
        return leaveType;
    }
}
