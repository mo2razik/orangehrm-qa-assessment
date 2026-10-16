package com.orangehrm.tests;

import com.orangehrm.base.BaseTest;
import com.orangehrm.flows.EmployeeFlows;
import com.orangehrm.flows.LeaveFlows;
import com.orangehrm.models.Employee;
import com.orangehrm.pages.AddEmployeePage;
import com.orangehrm.pages.ApplyLeavePage;
import com.orangehrm.pages.EmployeeListPage;
import com.orangehrm.pages.PersonalDetailsPage;
import com.orangehrm.utils.TestDataFactory;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import java.time.LocalDate;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Step 9 - negative scenarios. Every test verifies both the validation message
 * and that nothing was actually created / changed.
 */
@Epic("OrangeHRM")
@Feature("Negative Validation")
public class NegativeValidationTest extends BaseTest {

    private static final String REQUIRED = "Required";

    @Test(groups = {"negative", "regression"}, description = "Employee cannot be created without first and last name")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Example A - required field validation on Add Employee")
    public void createEmployeeWithoutRequiredFields() {
        // unique middle name + employee id act as markers to prove nothing was saved
        // (the proposed id cannot be used: other demo users may take it in the meantime)
        Employee incomplete = new Employee("", "Neg" + TestDataFactory.randomLetters(8), "");
        incomplete.setEmployeeId(TestDataFactory.uniqueEmployeeId());

        AddEmployeePage addPage = new AddEmployeePage(driver()).open().fill(incomplete).saveExpectingErrors();
        checkpoint("05_negative_required_fields");

        assertEquals(addPage.getFirstNameError(), REQUIRED, "First name validation");
        assertEquals(addPage.getLastNameError(), REQUIRED, "Last name validation");
        assertTrue(addPage.isStillOnAddEmployeePage(), "User should stay on Add Employee");
        assertFalse(addPage.isToastDisplayed(2), "No success notification expected");

        EmployeeListPage list = new EmployeeListPage(driver()).open().searchById(incomplete.getEmployeeId());
        assertEquals(list.getRecordCount(), 0, "Employee must not be created (searched by its unique id)");
        list.reset().enterPartialEmployeeName(incomplete.getMiddleName()).search();
        assertEquals(list.getRecordCount(), 0, "Employee must not be created (searched by its unique middle name)");
    }

    @Test(groups = {"negative", "regression"}, description = "Employee Id longer than 10 characters is rejected")
    @Severity(SeverityLevel.NORMAL)
    @Description("Example B - invalid employee data")
    public void createEmployeeWithTooLongEmployeeId() {
        Employee employee = TestDataFactory.newEmployee();
        employee.setEmployeeId("ID" + TestDataFactory.uniqueEmployeeId()); // 11 characters

        AddEmployeePage addPage = new AddEmployeePage(driver()).open().fill(employee).saveExpectingErrors();

        assertEquals(addPage.getEmployeeIdError(), "Should not exceed 10 characters", "Employee Id validation");
        assertTrue(addPage.isStillOnAddEmployeePage(), "User should stay on Add Employee");

        EmployeeListPage list = new EmployeeListPage(driver()).open()
                .enterPartialEmployeeName(employee.getLastName()).search();
        assertEquals(list.getRecordCount(), 0, "Employee with invalid id must not be created");
    }

    @Test(groups = {"negative", "regression"}, description = "Existing employee cannot be saved with an empty first name")
    @Severity(SeverityLevel.NORMAL)
    @Description("Example A (update) - required field validation on Personal Details; data stays unchanged")
    public void updateEmployeeWithEmptyFirstName() {
        Employee employee = TestDataFactory.newEmployee();
        EmployeeFlows.create(driver(), employee);

        PersonalDetailsPage details = EmployeeFlows.open(driver(), employee.getEmployeeId());
        details.setFirstName("").saveExpectingError();
        assertEquals(details.getFirstNameError(), REQUIRED, "First name validation");

        details.reload();
        assertEquals(details.getFirstName(), employee.getFirstName(), "First name must not be updated");
        assertEquals(details.getHeaderName(), employee.getFirstAndLastName(), "Header unchanged");
    }

    @Test(groups = {"negative", "regression"}, description = "Leave request with To Date before From Date is rejected")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Example C - invalid leave dates")
    public void leaveRequestWithToDateBeforeFromDate() {
        String leaveType = LeaveFlows.prepareLeaveTypeWithBalance(driver());
        LocalDate from = TestDataFactory.futureWorkingDay();
        LocalDate to = from.minusDays(2);

        ApplyLeavePage apply = new ApplyLeavePage(driver())
                .selectLeaveType(leaveType)
                .setFromDate(from)
                .setToDate(to)
                .apply();
        checkpoint("05_negative_leave_invalid_dates");

        assertEquals(apply.getToDateError(), "To date should be after from date", "To Date validation");
        assertFalse(apply.isToastDisplayed(2), "Leave request must not be submitted");
        assertTrue(apply.isOnApplyPage(), "User should stay on Apply Leave");
    }

    @Test(groups = {"negative", "regression"}, description = "Leave request without a leave type is rejected")
    @Severity(SeverityLevel.NORMAL)
    @Description("Example C - missing required leave information")
    public void leaveRequestWithoutLeaveType() {
        LeaveFlows.prepareLeaveTypeWithBalance(driver());
        LocalDate date = TestDataFactory.futureWorkingDay();

        ApplyLeavePage apply = new ApplyLeavePage(driver()).open()
                .setFromDate(date)
                .setToDate(date)
                .apply();

        assertEquals(apply.getLeaveTypeError(), REQUIRED, "Leave Type validation");
        assertFalse(apply.isToastDisplayed(2), "Leave request must not be submitted");
        assertTrue(apply.isOnApplyPage(), "User should stay on Apply Leave");
    }
}
