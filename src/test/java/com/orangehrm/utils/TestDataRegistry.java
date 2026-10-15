package com.orangehrm.utils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Keeps track of records created during the run so they can be removed afterwards.
 */
public final class TestDataRegistry {

    private static final Set<String> CREATED_EMPLOYEE_IDS = Collections.synchronizedSet(new LinkedHashSet<>());

    private TestDataRegistry() {
    }

    public static void registerEmployee(String employeeId) {
        if (employeeId != null && !employeeId.isBlank()) {
            CREATED_EMPLOYEE_IDS.add(employeeId);
        }
    }

    public static void unregisterEmployee(String employeeId) {
        CREATED_EMPLOYEE_IDS.remove(employeeId);
    }

    public static Set<String> createdEmployeeIds() {
        synchronized (CREATED_EMPLOYEE_IDS) {
            return new LinkedHashSet<>(CREATED_EMPLOYEE_IDS);
        }
    }
}
