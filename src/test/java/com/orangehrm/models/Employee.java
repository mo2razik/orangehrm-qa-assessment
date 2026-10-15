package com.orangehrm.models;

/**
 * Test-side representation of an employee created or edited by the tests.
 */
public class Employee {

    private String firstName;
    private String middleName;
    private String lastName;
    private String employeeId;

    public Employee(String firstName, String middleName, String lastName) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    /** "First Middle" as shown in the "First (&amp; Middle) Name" column of the employee list. */
    public String getFirstAndMiddleName() {
        return middleName == null || middleName.isBlank() ? firstName : firstName + " " + middleName;
    }

    /** "First Last" as shown in the employee details header. */
    public String getFirstAndLastName() {
        return firstName + " " + lastName;
    }

    public String getFullName() {
        return getFirstAndMiddleName() + " " + lastName;
    }

    @Override
    public String toString() {
        return "Employee{" + getFullName() + ", id=" + employeeId + "}";
    }
}
