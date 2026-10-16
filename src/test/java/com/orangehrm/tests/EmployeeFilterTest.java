package com.orangehrm.tests;

import com.orangehrm.base.BaseTest;
import com.orangehrm.models.EmployeeRow;
import com.orangehrm.pages.EmployeeListPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.SkipException;
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
    private static final int MAX_JOB_TITLES_TO_TRY = 15;

    @Test(groups = "regression", description = "Filter by Job Title + Employment Status, then reset")
    @Severity(SeverityLevel.NORMAL)
    @Description("Every returned row must have the selected job title and employment status")
    public void filterByJobTitleAndEmploymentStatus() {
        EmployeeListPage list = dashboard().goToEmployeeList();
        int unfilteredCount = list.getRecordCount();

        // Pick a job title that currently has an employee with an employment status.
        // Values come from the dropdown, not from the first page of rows, because the
        // shared demo data changes constantly.
        EmployeeRow reference = null;
        for (String jobTitle : list.getJobTitleOptions().stream().limit(MAX_JOB_TITLES_TO_TRY).toList()) {
            list.selectJobTitle(jobTitle).search();
            reference = list.findRow(r -> !r.employmentStatus().isBlank()).orElse(null);
            if (reference != null) {
                break;
            }
        }
        if (reference == null) {
            throw new SkipException("No employee with both a job title and an employment status in the demo data");
        }
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
        String referenceId = reference.id();
        assertTrue(results.stream().anyMatch(r -> r.id().equals(referenceId)),
                "Reference employee should be part of the filtered result");

        list.reset();
        assertEquals(list.getSelectedJobTitle(), SELECT_PLACEHOLDER, "Job title filter cleared");
        assertEquals(list.getSelectedEmploymentStatus(), SELECT_PLACEHOLDER, "Employment status filter cleared");
        assertResetRestoresFullList(list);
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

        assertTrue(list.getRecordCount() < unfilteredCount, "Filtered set should be smaller than the full set");

        list.reset();
        assertEquals(list.getEmployeeNameFilterValue(), "", "Employee name filter cleared");
        assertEquals(list.getEmployeeIdFilterValue(), "", "Employee id filter cleared");
        assertResetRestoresFullList(list);
    }

    /**
     * Compares the result after Reset with a freshly loaded, unfiltered list. Comparing with the
     * count read at the start of the test is unreliable because other users of the shared demo
     * add and delete employees while the test runs.
     */
    private void assertResetRestoresFullList(EmployeeListPage list) {
        int afterReset = list.getRecordCount();
        int freshUnfiltered = new EmployeeListPage(driver()).open().getRecordCount();
        assertEquals(afterReset, freshUnfiltered, "Reset restores the full (unfiltered) result set");
    }
}
