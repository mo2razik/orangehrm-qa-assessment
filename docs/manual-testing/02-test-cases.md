# Employee Leave Request – Test Design

## 1. Approach

| Item | Description |
|---|---|
| Scope | Apply leave, day calculation, submission, My Leave list, cancellation, validation, manager decision (as far as it affects the employee), balance. |
| Out of scope | Leave configuration screens (types, periods, holidays) except as test-data set-up; payroll export. |
| Techniques | Equivalence partitioning and boundary value analysis (dates, balance, comment length), decision table (validation), role-based testing, error guessing. |
| Levels | UI (functional), API checks for authorisation and stored values, cross-browser smoke. |
| Priorities | **P1** – blocks release / money-related, **P2** – important functionality, **P3** – minor / cosmetic. |

### Test data

| Data | Values |
|---|---|
| Users | `EMP_A` (has supervisor `MGR_A`), `EMP_B` (no supervisor), `MGR_A`, `ADMIN` |
| Leave types | Annual (entitlement 10 days), Sick (entitlement 5), Unpaid (entitlement 0), one inactive type |
| Calendar | Work week Mon–Fri; public holiday on a Wednesday inside the test month |
| Leave period | 1 Jan – 31 Dec |
| Date patterns | single weekday · Mon–Wed · Thu–Mon (spans weekend) · Sat–Sun only · range containing the holiday · last working day of the period → first of the next |

## 2. Scenario overview (traceability)

| Scenario | AC | Test cases |
|---|---|---|
| S1 Open Leave Request page | AC1 | TC-01 – TC-03 |
| S2 Select leave type and see balance | AC2 | TC-04 – TC-07 |
| S3 Select dates | AC3 | TC-08 – TC-12 |
| S4 Calculate leave days | AC4 | TC-13 – TC-20 |
| S5 Comment | AC5 | TC-21 – TC-24 |
| S6 Submit request | AC6, AC7 | TC-25 – TC-30 |
| S7 View submitted requests | AC8 | TC-31 – TC-34 |
| S8 Cancel pending request | AC9 | TC-35 – TC-39 |
| S9 Invalid requests | AC10 | TC-40 – TC-49 |
| S10 Security / roles | AC1, AC8, AC9 | TC-50 – TC-53 |
| S11 Non-functional / compatibility | – | TC-54 – TC-58 |

## 3. Test cases

### S1 – Open Leave Request page

| ID | Title | Preconditions | Steps | Expected result | Priority |
|---|---|---|---|---|---|
| TC-01 | Employee opens Leave Request page from the menu | `EMP_A` logged in | 1. Click Leave → Apply | Page *Apply Leave* is displayed with Leave Type, From Date, To Date, Comment and Apply button | P1 |
| TC-02 | Page when employee has no entitlement | `EMP_C` with no entitlements | Open Leave → Apply | Informative message (e.g. "No Leave Types with Leave Balance"), no form, no error | P2 |
| TC-03 | Direct URL access when logged out | Logged out | Open the Apply Leave URL | Redirected to login; after login user lands on the requested page or dashboard | P2 |

### S2 – Leave type and balance

| ID | Title | Preconditions | Steps | Expected result | Priority |
|---|---|---|---|---|---|
| TC-04 | Leave type list content | `EMP_A` | Open the Leave Type dropdown | Only active leave types the employee is entitled to are listed; inactive type is not listed | P2 |
| TC-05 | Balance displayed for the selected type | Annual balance 10 | Select Annual | Balance "10.00 Day(s)" is displayed | P1 |
| TC-06 | Balance details popup | – | Click the balance details link | Entitlement, taken, scheduled, pending and balance values add up | P2 |
| TC-07 | Changing the type updates the balance | – | Select Annual, then Sick | Balance changes to the Sick balance | P2 |

### S3 – Dates

| ID | Title | Preconditions | Steps | Expected result | Priority |
|---|---|---|---|---|---|
| TC-08 | Select dates with the date picker | – | Pick From and To dates in the calendar | Dates are filled in the configured format | P1 |
| TC-09 | Type dates manually | – | Type valid dates in the configured format | Dates are accepted | P2 |
| TC-10 | To Date defaults to From Date | – | Select only From Date | To Date is set to the same date; duration "Full Day" is offered | P3 |
| TC-11 | Single-day request (From = To) | – | Select the same weekday for both | Accepted; 1 day calculated | P1 |
| TC-12 | Range across two leave periods | Period ends 31 Dec | From 30 Dec to 2 Jan | Behaviour as specified (rejected, or split per period); balance deducted from the correct period | P2 |

### S4 – Leave day calculation

| ID | Title | Data | Expected result | Priority |
|---|---|---|---|---|
| TC-13 | Consecutive weekdays | Mon–Wed | 3.00 days displayed, 3.00 deducted from balance | P1 |
| TC-14 | Range spanning a weekend | Thu–Mon | 3.00 days (Sat, Sun excluded) | P1 |
| TC-15 | Only weekend days | Sat–Sun | 0 days → request is blocked with a message | P1 |
| TC-16 | Range containing a public holiday | Mon–Fri with holiday on Wed | 4.00 days | P1 |
| TC-17 | Half day | Single day, Half Day – Morning | 0.50 days | P2 |
| TC-18 | Partial days on multi-day request | Mon–Wed, *Start Day Only* half day | 2.50 days | P2 |
| TC-19 | Specific time | Single day, 09:00–13:00 | 0.50 days (4 h of an 8 h day) | P3 |
| TC-20 | Recalculation on date change | Change To Date after calculation | Number of days updates immediately; the value saved equals the value displayed | P1 |

> For every TC in S4 the check is three-fold: **displayed days = days stored on the request = balance change**.

### S5 – Comment

| ID | Title | Data | Expected result | Priority |
|---|---|---|---|---|
| TC-21 | Submit without comment | empty | Request submitted (comment is optional) | P1 |
| TC-22 | Comment at maximum length | 250 characters (or the defined limit) | Accepted and fully displayed | P2 |
| TC-23 | Comment over maximum length | limit + 1 | Validation message, request not submitted | P2 |
| TC-24 | Special characters / script | `<script>alert(1)</script>`, Arabic text, emoji | Stored and displayed as plain text, no script executed | P2 |

### S6 – Submit request

| ID | Title | Preconditions | Steps | Expected result | Priority |
|---|---|---|---|---|---|
| TC-25 | Submit a valid request | Annual balance 10 | Select Annual, Mon–Wed, comment, click Apply | Success message; request created with status *Pending Approval (3.00)* | P1 |
| TC-26 | Balance after submission | as TC-25 | Re-open Apply, select Annual | Available balance reduced by the pending 3 days (or as defined by the balance rules) | P1 |
| TC-27 | Supervisor sees the request | `MGR_A` | Log in as `MGR_A`, open Leave List | Request of `EMP_A` is listed as Pending Approval | P1 |
| TC-28 | Notification sent | Email configured | Submit | Supervisor (and employee) receive the configured notification | P2 |
| TC-29 | Double submission | – | Double-click Apply / press Enter twice | Only one request is created | P2 |
| TC-30 | Employee without supervisor | `EMP_B` | Submit a valid request | Behaviour as specified (admin approval, warning); request is not lost | P2 |

### S7 – View submitted requests

| ID | Title | Steps | Expected result | Priority |
|---|---|---|---|---|
| TC-31 | Request appears in My Leave | Open Leave → My Leave | Row with correct dates, type, days, status, comment | P1 |
| TC-32 | Filter by date range and status | Filter Pending only, then Cancelled only | Only matching rows; record count matches | P2 |
| TC-33 | Multi-day request details | Open the details of a Mon–Wed request | One line per day with individual status | P3 |
| TC-34 | Many requests | Employee with > 50 requests | Pagination / sorting work, no missing rows | P3 |

### S8 – Cancel a pending request

| ID | Title | Steps | Expected result | Priority |
|---|---|---|---|---|
| TC-35 | Cancel a pending request | My Leave → Cancel on a pending row | Success message; status *Cancelled* | P1 |
| TC-36 | Balance restored after cancel | Check balance after TC-35 | Balance equals the value before the request | P1 |
| TC-37 | Cancel not offered for rejected request | Request rejected by manager | No Cancel action for the employee | P2 |
| TC-38 | Cancel approved future leave | Request approved | Behaviour as specified (allowed / blocked / needs approval) | P2 |
| TC-39 | Manager acts while employee cancels | Two sessions: manager approves, employee cancels at the same time | Final state is consistent; balance correct; no error page | P2 |

### S9 – Invalid requests (decision table)

| Rule | Leave type | Dates | Balance | Overlap | Expected |
|---|---|---|---|---|---|
| R1 | missing | valid | ok | no | "Required" on Leave Type |
| R2 | ok | From missing | ok | no | "Required" on From Date |
| R3 | ok | To < From | ok | no | "To date should be after from date" |
| R4 | ok | only weekend / holidays | ok | no | Blocked – 0 working days |
| R5 | ok | valid | insufficient | no | Blocked or warning as specified |
| R6 | ok | valid | ok | yes | Blocked – overlapping request shown |
| R7 | ok | invalid date (31 Feb, wrong format) | ok | no | "Should be a valid date in yyyy-dd-mm format" |
| R8 | ok | outside leave period | ok | no | Blocked – leave period not defined |

| ID | Title | Rule | Priority |
|---|---|---|---|
| TC-40 | Submit without leave type | R1 | P1 |
| TC-41 | Submit without From Date | R2 | P1 |
| TC-42 | To Date before From Date | R3 | P1 |
| TC-43 | Request containing only weekend days | R4 | P1 |
| TC-44 | Request exceeding balance (balance 2, request 3 days) | R5 | P1 |
| TC-45 | Request exactly equal to balance (boundary) | R5 boundary | P1 |
| TC-46 | Request overlapping an existing pending request | R6 | P1 |
| TC-47 | Request overlapping an approved request (one day overlap) | R6 | P2 |
| TC-48 | Invalid / mistyped date | R7 | P2 |
| TC-49 | Date outside the leave period | R8 | P2 |

For each invalid case also verify: no success message, nothing new in My Leave, balance unchanged.

### S10 – Security and roles

| ID | Title | Expected result | Priority |
|---|---|---|---|
| TC-50 | Employee sees only own requests | Another employee's requests are not listed | P1 |
| TC-51 | Cancel another employee's request through the API / URL | Request rejected with 401/403; data unchanged | P1 |
| TC-52 | Employee cannot approve own request through the API | Request rejected | P1 |
| TC-53 | Session timeout during submission | User is asked to log in again; no partial request created | P3 |

### S11 – Non-functional / compatibility

| ID | Title | Expected result | Priority |
|---|---|---|---|
| TC-54 | Different date formats configured (dd-mm-yyyy, mm-dd-yyyy, yyyy-dd-mm) | Dates parsed and displayed correctly; no day / month swap | P1 |
| TC-55 | Browser / server in different time zones (e.g. UTC-8 browser, UTC+3 server) | Stored dates equal selected dates; no off-by-one | P1 |
| TC-56 | Cross-browser smoke (Chrome, Firefox, Edge, Safari) | Apply / view / cancel work | P2 |
| TC-57 | Mobile viewport | Form usable, date picker usable | P3 |
| TC-58 | Keyboard-only and screen reader | Fields have labels, errors are announced | P3 |
