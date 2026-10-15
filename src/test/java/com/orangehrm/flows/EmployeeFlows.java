package com.orangehrm.flows;

import com.orangehrm.models.Employee;
import com.orangehrm.pages.AddEmployeePage;
import com.orangehrm.pages.EmployeeListPage;
import com.orangehrm.pages.PersonalDetailsPage;
import com.orangehrm.utils.TestDataRegistry;
import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;

/**
 * Reusable employee flows shared by several test classes.
 */
public final class EmployeeFlows {

    private EmployeeFlows() {
    }

    /**
     * Creates the employee through PIM > Add Employee, stores the generated
     * employee id on the model and registers it for cleanup.
     *
     * @return the toast message shown after saving
     */
    @Step("Create employee {employee}")
    public static String create(WebDriver driver, Employee employee) {
        AddEmployeePage addPage = new EmployeeListPage(driver).open().goToAddEmployee();
        addPage.fill(employee);
        employee.setEmployeeId(addPage.getEmployeeId());
        PersonalDetailsPage details = addPage.save();
        String toast = details.waitForToastMessage();
        details.waitUntilLoaded();
        TestDataRegistry.registerEmployee(employee.getEmployeeId());
        return toast;
    }

    /** Searches the employee list by employee id and opens the matching record. */
    @Step("Open employee {employeeId} from the Employee List")
    public static PersonalDetailsPage open(WebDriver driver, String employeeId) {
        return new EmployeeListPage(driver).open()
                .searchById(employeeId)
                .openEmployee(employeeId);
    }
}
