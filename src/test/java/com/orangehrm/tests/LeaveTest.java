package com.orangehrm.tests;

import com.orangehrm.base.BaseTest;
import com.orangehrm.flows.LeaveFlows;
import com.orangehrm.models.LeaveRow;
import com.orangehrm.pages.ApplyLeavePage;
import com.orangehrm.pages.MyLeaveListPage;
import com.orangehrm.utils.TestDataFactory;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import java.time.LocalDate;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@Epic("OrangeHRM")
@Feature("Leave")
public class LeaveTest extends BaseTest {

    private static final int MAX_DATE_ATTEMPTS = 3;

    @Test(groups = "regression", description = "Submit a leave request and find it in My Leave")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Step 8 - leave type is taken from the user's available types, dates are calculated at runtime")
    public void submitLeaveRequest() {
        String leaveType = LeaveFlows.prepareLeaveTypeWithBalance(driver());
        String comment = TestDataFactory.uniqueComment();
        ApplyLeavePage apply = new ApplyLeavePage(driver());

        LocalDate leaveDate = null;
        String toast = null;
        for (int attempt = 1; attempt <= MAX_DATE_ATTEMPTS && toast == null; attempt++) {
            LocalDate candidate = TestDataFactory.futureWorkingDay();
            log.info("Applying {} leave on {} (attempt {})", leaveType, candidate, attempt);
            apply.selectLeaveType(leaveType)
                    .setFromDate(candidate)
                    .setToDate(candidate)
                    .setComment(comment)
                    .apply();
            if (apply.isToastDisplayed(10)) {
                toast = apply.waitForToastMessage();
                leaveDate = candidate;
            } else if (apply.isOverlapWarningDisplayed()) {
                // another run already booked that day on the shared environment - pick another one
                apply.open();
            } else {
                fail("Leave request was neither saved nor reported as overlapping");
            }
        }

        assertEquals(toast, "Successfully Saved", "Leave submission notification");
        checkpoint("06_leave_request_submitted");

        MyLeaveListPage myLeave = new MyLeaveListPage(driver()).open().filterByDate(leaveDate);
        boolean cancelled = false;
        try {
            LeaveRow row = myLeave.findRequest(leaveDate, leaveType, comment)
                    .orElseThrow(() -> new AssertionError("Submitted leave request not found in My Leave"));
            assertTrue(row.status().startsWith("Pending Approval"),
                    "Status should be Pending Approval but was " + row.status());
            assertEquals(row.numberOfDays(), "1.00", "Number of days for a single full day");
            checkpoint("06_leave_request_in_my_leave");

            // clean up: cancel the pending request so the balance is restored
            assertEquals(myLeave.cancelRequest(comment), "Successfully Updated", "Leave cancellation notification");
            cancelled = true;
        } finally {
            if (!cancelled) {
                cancelQuietly(myLeave, comment);
            }
        }
    }

    private void cancelQuietly(MyLeaveListPage myLeave, String comment) {
        try {
            myLeave.cancelRequest(comment);
        } catch (RuntimeException e) {
            log.warn("Could not cancel leave request '{}': {}", comment, e.getMessage());
        }
    }
}
