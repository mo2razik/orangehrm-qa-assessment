# Bug Report

| Field | Value |
|---|---|
| **ID** | LEAVE-001 |
| **Title** | Leave balance is reduced by 4 days for a 3-day leave request (10–12 Aug) – displayed duration and deducted days do not match |
| **Component** | Leave → Apply Leave / Leave balance |
| **Environment** | QA – OrangeHRM OS 5.9 (build / commit: *to be filled*), Chrome 129 / Windows 11 |
| **Reported by / date** | QA – *date of finding* |
| **Severity** | **Major** – incorrect leave balance (affects employee entitlements and potentially payroll) |
| **Priority** | **High** – must be fixed before release |
| **Reproducibility** | *To be confirmed* (e.g. 3/3 attempts with the data below) |
| **Status** | New |

## Preconditions

* Employee `EMP_A` logged in as ESS user, supervisor `MGR_A`.
* Leave type **Annual Leave**, entitlement 10.00 days for the current leave period, no other requests → balance **10.00**.
* Work week Monday–Friday; no public holiday between 10 and 12 August.
* 10 Aug 2026 = Monday, 12 Aug 2026 = Wednesday (if the year is different, record the weekdays).

## Steps to reproduce

1. Log in as `EMP_A`.
2. Go to **Leave → Apply**.
3. Select Leave Type **Annual Leave** – note the displayed balance (**10.00 Day(s)**).
4. Set **From Date = 10 Aug 2026**, **To Date = 12 Aug 2026**, Partial Days = None (full days).
5. Note the calculated duration shown on the form.
6. Click **Apply**.
7. Go to **Leave → My Leave** and open the request.
8. Go back to **Leave → Apply**, select **Annual Leave** and check the balance (or open *Leave → Entitlements → My Leave Entitlements and Usage Report*).

## Expected result

* Step 5 / 7: the request shows **3.00** days (Mon, Tue, Wed).
* Step 8: the balance is **7.00** days (10.00 − 3.00).
* Displayed days, days stored on the request and days deducted are identical.

## Actual result

* Step 5 / 7: the request shows **3.00** days.
* Step 8: the balance is **6.00** days – **4.00** days were deducted.

## Evidence (attach)

* Screenshot of the Apply form with the 3-day calculation.
* Screenshot of My Leave (request row and the per-day details).
* Screenshot of the balance before and after the request.
* Network capture (HAR) of the *apply leave* request and of the balance / entitlement responses.
* If available: database rows for the request (`ohrm_leave_request`, `ohrm_leave` – number of day rows and their `length_days`, `ohrm_leave_entitlement.days_used`).
* Browser time zone and server time zone.

## Impact

* Employees lose leave days they did not take; the error accumulates with every request.
* The balance shown to the employee and the request duration contradict each other, which damages trust and generates HR tickets.
* If the balance feeds payroll or leave encashment, it has a financial impact.
