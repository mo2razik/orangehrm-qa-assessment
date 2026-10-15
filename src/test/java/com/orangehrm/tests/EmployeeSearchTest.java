package com.orangehrm.tests;

import com.orangehrm.base.BaseTest;
import com.orangehrm.models.EmployeeRow;
import com.orangehrm.pages.EmployeeListPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Epic("OrangeHRM")
@Feature("PIM - Employee Search")
public class EmployeeSearchTest extends BaseTest {

    @Test(groups = {"smoke", "regression"}, description = "Search an existing employee by name and validate the result")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Step 2 - the target employee is taken from the live list (no hard-coded name or row number)")
    public void searchExistingEmployeeByName() {
        EmployeeListPage list = dashboard().goToEmployeeList();

        // pick an existing employee dynamically from the current data
        EmployeeRow target = list.findRow(r -> !r.id().isBlank()
                        && r.firstAndMiddleName().matches("[A-Za-z ]+")
                        && r.lastName().matches("[A-Za-z]+"))
                .orElseThrow(() -> new AssertionError("No suitable employee found in the employee list"));
        String firstName = target.firstAndMiddleName().split(" ")[0];
        log.info("Searching for existing employee {}", target);

        list.enterEmployeeName(target.fullName(), firstName, target.lastName()).search();
        checkpoint("03_employee_search_result");

        List<EmployeeRow> results = list.getRows();
        assertFalse(results.isEmpty(), "Search should return at least one employee");
        assertEquals(list.getRecordCount(), results.size(), "Record counter should match the rows displayed");
        for (EmployeeRow row : results) {
            assertTrue(row.firstAndMiddleName().toLowerCase().contains(firstName.toLowerCase())
                            && row.lastName().equalsIgnoreCase(target.lastName()),
                    "Every result should match the searched name, but found: " + row);
        }

        EmployeeRow found = list.findRowById(target.id())
                .orElseThrow(() -> new AssertionError("Searched employee " + target.id() + " not in results"));
        assertEquals(found.firstAndMiddleName(), target.firstAndMiddleName(), "First (& Middle) name");
        assertEquals(found.lastName(), target.lastName(), "Last name");
        assertEquals(found.jobTitle(), target.jobTitle(), "Job title");
        assertEquals(found.employmentStatus(), target.employmentStatus(), "Employment status");
        assertEquals(found.subUnit(), target.subUnit(), "Sub unit");
    }
}
