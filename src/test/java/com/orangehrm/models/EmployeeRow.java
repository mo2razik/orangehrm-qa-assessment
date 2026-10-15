package com.orangehrm.models;

/**
 * One row of the PIM Employee List table, read from the UI.
 */
public record EmployeeRow(String id,
                          String firstAndMiddleName,
                          String lastName,
                          String jobTitle,
                          String employmentStatus,
                          String subUnit,
                          String supervisor) {

    public String fullName() {
        return (firstAndMiddleName + " " + lastName).trim().replaceAll("\\s+", " ");
    }
}
