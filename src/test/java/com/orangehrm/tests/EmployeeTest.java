package com.orangehrm.tests;

import com.orangehrm.base.BaseTest;
import com.orangehrm.flows.EmployeeFlows;
import com.orangehrm.models.Employee;
import com.orangehrm.models.EmployeeRow;
import com.orangehrm.pages.EmployeeListPage;
import com.orangehrm.pages.PersonalDetailsPage;
import com.orangehrm.utils.TestDataFactory;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

/**
 * Employee lifecycle: create -> validate persistence -> edit -> verify after refresh/navigation.
 * Each step runs in its own browser session, so every check reads the data back from the server.
 */
@Epic("OrangeHRM")
@Feature("PIM - Employee Lifecycle")
public class EmployeeTest extends BaseTest {

    private final Employee employee = TestDataFactory.newEmployee();
    private String otherId;

    @Test(groups = {"smoke", "regression"}, description = "Create a new employee with generated data")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Step 3 - add employee with a unique generated last name")
    public void createEmployee() {
        String toast = EmployeeFlows.create(driver(), employee);
        PersonalDetailsPage details = new PersonalDetailsPage(driver());

        assertEquals(toast, "Successfully Saved", "Success notification");
        assertFalse(employee.getEmployeeId().isBlank(), "Employee ID should be generated");
        assertEquals(details.getEmployeeId(), employee.getEmployeeId(), "Generated Employee ID is displayed");
        assertEquals(details.getHeaderName(), employee.getFirstAndLastName(), "Created employee header");
        checkpoint("02_employee_created");

        // the employee can subsequently be found in the employee list
        EmployeeListPage list = new EmployeeListPage(driver()).open().searchById(employee.getEmployeeId());
        assertTrue(list.findRowById(employee.getEmployeeId()).isPresent(),
                "New employee should be listed in the Employee List");
    }

    @Test(groups = "regression", dependsOnMethods = "createEmployee",
            description = "Created employee is persisted and can be opened from the list")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Step 4 - search by name in a new session, open the record and compare every value")
    public void validateCreatedEmployee() {
        EmployeeListPage list = dashboard().goToEmployeeList()
                .enterEmployeeName(employee.getFullName(), employee.getFirstName(), employee.getLastName())
                .search();

        EmployeeRow row = list.findRowById(employee.getEmployeeId())
                .orElseThrow(() -> new AssertionError("Created employee not found by name: " + employee));
        assertEquals(row.firstAndMiddleName(), employee.getFirstAndMiddleName(), "First (& Middle) name in list");
        assertEquals(row.lastName(), employee.getLastName(), "Last name in list");

        PersonalDetailsPage details = list.openEmployee(employee.getEmployeeId());
        assertEquals(details.getHeaderName(), employee.getFirstAndLastName(), "Employee header");
        assertEquals(details.getFirstName(), employee.getFirstName(), "First name");
        assertEquals(details.getMiddleName(), employee.getMiddleName(), "Middle name");
        assertEquals(details.getLastName(), employee.getLastName(), "Last name");
        assertEquals(details.getEmployeeId(), employee.getEmployeeId(), "Employee Id is populated");
        assertTrue(details.getEmpNumberFromUrl().matches("\\d+"), "Employee details are accessible by emp number");
    }

    @Test(groups = "regression", dependsOnMethods = "validateCreatedEmployee",
            description = "Edit the created employee")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Step 5 - change last name and Other Id, save and verify")
    public void editEmployee() {
        String newLastName = TestDataFactory.uniqueLastName();
        String newOtherId = "OID" + TestDataFactory.randomLetters(5).toUpperCase();
        PersonalDetailsPage details = EmployeeFlows.open(driver(), employee.getEmployeeId());
        assertNotEquals(details.getLastName(), newLastName, "New value must differ from the original");

        String toast = details.setLastName(newLastName).setOtherId(newOtherId).save();
        assertEquals(toast, "Successfully Updated", "Update notification");
        employee.setLastName(newLastName);

        assertEquals(details.getLastName(), newLastName, "Updated last name is visible");
        assertEquals(details.getOtherId(), newOtherId, "Updated Other Id is visible");
        // Note: the profile header keeps the old name until the page is reloaded (application behaviour),
        // the header is therefore verified after refresh/navigation in the persistence test.
        checkpoint("04_employee_updated");
        otherId = newOtherId;
    }

    @Test(groups = "regression", dependsOnMethods = "editEmployee",
            description = "Updated values survive a refresh and navigation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Step 6 - refresh the details page, then leave and come back")
    public void updatedValuesPersistAfterRefreshAndNavigation() {
        PersonalDetailsPage details = EmployeeFlows.open(driver(), employee.getEmployeeId());

        details.reload();
        assertEquals(details.getLastName(), employee.getLastName(), "Last name after refresh");
        assertEquals(details.getOtherId(), otherId, "Other Id after refresh");

        // navigate away and come back through the employee list
        details.openMainMenu("Dashboard");
        dashboard().waitUntilLoaded();
        EmployeeListPage list = dashboard().goToEmployeeList().searchById(employee.getEmployeeId());
        EmployeeRow row = list.findRowById(employee.getEmployeeId())
                .orElseThrow(() -> new AssertionError("Employee not found after navigation"));
        assertEquals(row.lastName(), employee.getLastName(), "Last name in list after navigation");

        details = list.openEmployee(employee.getEmployeeId());
        assertEquals(details.getLastName(), employee.getLastName(), "Last name after navigation");
        assertEquals(details.getOtherId(), otherId, "Other Id after navigation");
        assertEquals(details.getHeaderName(), employee.getFirstAndLastName(), "Header after navigation");
    }
}
