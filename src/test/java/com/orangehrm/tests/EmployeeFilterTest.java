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

/**
 * Step 7 - filter values are taken from the live data so the tests do not depend
 * on a specific job title / status existing in the shared demo environment.
 */
@Epic("OrangeHRM")
@Feature("PIM - Employee List Filtering")
public class EmployeeFilterTest extends BaseTest {

    private static final String SELECT_PLACEHOLDER = "-- Select --";

    @Test(groups = "regression", description = "Filter by Job Title + Employment Status, then reset")
    @Severity(SeverityLevel.NORMAL)
    @Description("Every returned row must have the selected job title and employment status")
    public void filterByJobTitleAndEmploymentStatus() {
        EmployeeListPage list = dashboard().goToEmployeeList();
        int unfilteredCount = list.getRecordCount();

        EmployeeRow reference = list.findRow(r -> !r.jobTitle().isBlank() && !r.employmentStatus().isBlank())
                .orElseThrow(() -> new AssertionError("No employee with job title and employment status found"));
        log.info("Filtering with job title '{}' and status '{}'", reference.jobTitle(), reference.employmentStatus());

        list.selectJobTitle(reference.jobTitle())
                .selectEmploymentStatus(reference.employmentStatus())
                .search();
        checkpoint("07_filter_job_title_status");

        List<EmployeeRow> results = list.getRows();
        assertFalse(results.isEmpty(), "Filter should return the reference employee at least");
        assertTrue(list.getRecordCount() <= unfilteredCount, "Filtered set should not be larger than the full set");
        for (EmployeeRow row : results) {
            assertEquals(row.jobTitle(), reference.jobTitle(), "Job title of " + row);
            assertEquals(row.employmentStatus(), reference.employmentStatus(), "Employment status of " + row);
        }
        assertTrue(results.stream().anyMatch(r -> r.id().equals(reference.id()) || reference.id().isBlank()),
                "Reference employee should be part of the filtered result");

        list.reset();
        assertEquals(list.getSelectedJobTitle(), SELECT_PLACEHOLDER, "Job title filter cleared");
        assertEquals(list.getSelectedEmploymentStatus(), SELECT_PLACEHOLDER, "Employment status filter cleared");
        assertEquals(list.getRecordCount(), unfilteredCount, "Reset restores the full result set");
    }

    @Test(groups = "regression", description = "Filter by Employee Name + Employee Id, then reset")
    @Severity(SeverityLevel.NORMAL)
    @Description("Name and id filters combined return exactly the matching employee")
    public void filterByEmployeeNameAndId() {
        EmployeeListPage list = dashboard().goToEmployeeList();
        int unfilteredCount = list.getRecordCount();

        EmployeeRow reference = list.findRow(r -> !r.id().isBlank() && r.lastName().matches("[A-Za-z]+"))
                .orElseThrow(() -> new AssertionError("No employee with id and last name found"));
        log.info("Filtering with last name '{}' and id '{}'", reference.lastName(), reference.id());

        // partial name (last name only, no suggestion picked) + exact id
        list.enterPartialEmployeeName(reference.lastName())
                .enterEmployeeId(reference.id())
                .search();

        List<EmployeeRow> results = list.getRows();
        assertFalse(results.isEmpty(), "Filter should return the reference employee");
        for (EmployeeRow row : results) {
            assertEquals(row.id(), reference.id(), "Employee id of " + row);
            assertTrue(row.fullName().toLowerCase().contains(reference.lastName().toLowerCase()),
                    "Name of " + row + " should contain " + reference.lastName());
        }

        list.reset();
        assertEquals(list.getEmployeeNameFilterValue(), "", "Employee name filter cleared");
        assertEquals(list.getEmployeeIdFilterValue(), "", "Employee id filter cleared");
        assertEquals(list.getRecordCount(), unfilteredCount, "Reset restores the full result set");
    }
}
