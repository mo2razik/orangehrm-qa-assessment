package com.orangehrm.models;

/**
 * One row of the "My Leave List" table, read from the UI.
 */
public record LeaveRow(String date,
                       String leaveType,
                       String numberOfDays,
                       String status,
                       String comments) {
}
