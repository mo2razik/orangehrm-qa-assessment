# Employee Leave Request – Requirements Review

**Feature:** Employee Leave Request
**Story:** *As an employee, I want to request leave through the HR system, so that I can submit my planned absence for manager approval.*

This document lists the questions, ambiguities, missing requirements, assumptions and risks
I would raise with the Product Owner / BA / developers **before** test execution starts.

---

## 1. Questions and ambiguities per acceptance criterion

| # | Acceptance criterion | Questions / ambiguities |
|---|---|---|
| AC1 | Employee can open the Leave Request page | Which roles can access it (ESS user only, Admin, Supervisor on behalf of others)? What is shown if the employee has **no entitlement** for any leave type? Is the page available on mobile? |
| AC2 | Employee can select a Leave Type | Are all leave types listed, or only those with an entitlement / positive balance? Is the **current balance** displayed after selection? Can a leave type be inactive / deleted while a request is open? |
| AC3 | Employee can select From Date and To Date | Are **past dates** allowed (e.g. sick leave reported afterwards)? How far in the future can leave be requested? Can a request span two **leave periods / years**? Is From = To (single day) allowed? Which date format and which calendar first day of week? Is To Date auto-filled from From Date? |
| AC4 | System calculates the number of leave days | Are **weekends** and **public holidays** excluded? Whose work week / holiday calendar is used (employee location, company)? Are **half days / hours / partial days** supported? Is the value recalculated when dates change? Is the calculation done on the client, the server or both (risk of mismatch)? Rounding rules? |
| AC5 | Employee can add an optional comment | Maximum length? Allowed characters / emojis / HTML? Is the comment visible to the manager? Can it be edited after submission? |
| AC6 | Employee can submit the request | What happens when the **balance is insufficient** – block, warn, or allow negative balance? Is the balance **deducted at submission** (pending) or only at approval? Is the manager **notified** (email / in-app)? What if the employee has **no supervisor**? Protection against double submission? |
| AC7 | Submitted request has *Pending Approval* status | Is the status per request or per day (multi-day requests may have mixed statuses after partial approval)? Can a manager auto-approve rules exist (e.g. short leave)? |
| AC8 | Employee can view submitted requests | Which statuses / date range are shown by default? Sorting, filtering, pagination? Which fields (dates, type, days, status, comments, approver)? Are past years visible? |
| AC9 | Employee can cancel a pending request | Only *Pending*, or also *Approved / Scheduled* future leave? Is a confirmation required? Is the balance **restored** immediately? Is the manager notified? Can part of a multi-day request be cancelled? |
| AC10 | System prevents invalid leave requests | "Invalid" is **not defined**. A list of validation rules and messages is needed (see section 2). |

## 2. Missing requirements

1. **Validation rules** (AC10) – at minimum:
   - mandatory fields (leave type, from date, to date);
   - To Date before From Date;
   - request with 0 working days (only weekend / holidays);
   - insufficient balance;
   - **overlapping** with an existing pending / approved request;
   - dates outside the defined leave period;
   - invalid / manually typed dates (e.g. 31 Feb), wrong format;
   - maximum length of the comment.
2. **Balance rules** – when the balance is reduced (pending vs approved), how pending days are shown
   ("balance" vs "available balance"), restoration on cancel / reject.
3. **Status model** – the business flow only shows *Pending → Approved / Rejected*. Missing states
   and transitions: **Cancelled**, **Taken** (approved leave in the past), Scheduled, partially approved
   multi-day requests, cancellation of approved leave, re-submission after rejection.
4. **Manager decision details** – rejection reason mandatory? Can the manager change dates? What
   happens when the manager is absent (delegation / escalation / SLA)?
5. **Notifications** – who is notified on submit / approve / reject / cancel and through which channel.
6. **Roles and permissions** – an employee must only see and cancel their own requests; can a
   manager approve their own leave; can an admin apply on behalf of an employee?
7. **Audit trail** – who changed the status and when.
8. **Non-functional** – localisation (date formats, time zones), accessibility, supported browsers /
   mobile, performance of the list with many requests.

## 3. Business flow review

| Observation | Impact |
|---|---|
| "Calculate Leave Days" happens before "Valid Request?" but the validation rules that depend on the calculation (0 days, insufficient balance) are not shown. | Unclear where and when the user sees calculation-related errors. |
| No branch for **Cancel** although AC9 requires it. | State model incomplete; Cancelled status / balance restore untested by design. |
| No branch for **insufficient balance** or **overlap**. | Most likely production defects live here. |
| Approved leave has no further state (*Taken*, cancellation of approved leave). | Reporting / balance at year end is unclear. |
| Manager decision has no timeout / escalation. | Requests may stay pending forever. |

## 4. Assumptions (to be confirmed)

| # | Assumption |
|---|---|
| A1 | Work week is Monday–Friday; weekends and configured public holidays are **not** counted. |
| A2 | Full-day leave is the default; partial days (half day / hours) are supported as in OrangeHRM. |
| A3 | Balance shown = entitlement − (approved + pending); pending days are reserved at submission. |
| A4 | Only requests in *Pending Approval* can be cancelled by the employee (as stated in AC9). |
| A5 | Overlapping requests are rejected. |
| A6 | Requests outside the current leave period are not allowed unless the next period is defined. |
| A7 | The manager is the employee's supervisor defined in PIM (*Report-to*). |
| A8 | Dates are stored as calendar dates (no time component), so time zones must not shift them. |

## 5. Risks

| # | Risk | Likelihood | Impact | Mitigation / test focus |
|---|---|---|---|---|
| R1 | Day calculation differs between UI and balance deduction (weekends, holidays, inclusive end date). | High | High – financial / payroll impact | Compare displayed days, stored days and balance change for every date pattern. |
| R2 | Time-zone conversion shifts dates (e.g. 12 Aug 00:00 local stored as 11 Aug UTC). | Medium | High | Run with employee / browser / server in different time zones. |
| R3 | Overlapping or duplicate requests (double click, two tabs). | Medium | Medium | Concurrency and idempotency tests. |
| R4 | Balance not restored after cancel / reject. | Medium | High | Check balance after each state transition. |
| R5 | Employee can see / cancel another employee's request by changing an id in the URL / API. | Low | High (security, privacy) | Authorisation tests on UI and API. |
| R6 | Leave spanning two leave periods / years deducted from the wrong period. | Medium | Medium | Boundary tests at period end. |
| R7 | Manager not notified → request never approved. | Medium | Medium | Verify notifications / pending list of the supervisor. |
| R8 | Locale-specific date formats cause wrong parsing (dd-mm vs mm-dd). | Medium | Medium | Test with several configured date formats. |

## 6. Entry criteria proposed

* Answers to the open questions above (at least validation rules, balance rules, status model).
* Test environment with: employees with and without supervisors, several leave types, entitlements,
  a holiday calendar and a defined work week.
* Test users for the roles Employee, Supervisor and Admin.
