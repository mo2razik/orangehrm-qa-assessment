# OrangeHRM UI Automation – Senior QA Assessment

UI test automation framework for the [OrangeHRM open-source demo](https://opensource-demo.orangehrmlive.com/),
plus the manual testing deliverables for the *Employee Leave Request* feature.

| Deliverable | Location |
|---|---|
| Automation framework (Java / Maven) | `pom.xml`, `src/test/java`, `testng.xml` |
| Automated tests (steps 1–10) | `src/test/java/com/orangehrm/tests` |
| Screenshots | [`docs/screenshots`](docs/screenshots) |
| Example execution report (Allure, single file) | [`docs/report/index.html`](docs/report/index.html) |
| Manual testing – requirement review & risks | [`docs/manual-testing/01-requirements-review.md`](docs/manual-testing/01-requirements-review.md) |
| Manual testing – test scenarios & test cases | [`docs/manual-testing/02-test-cases.md`](docs/manual-testing/02-test-cases.md) |
| Manual testing – bug report | [`docs/manual-testing/03-bug-report.md`](docs/manual-testing/03-bug-report.md) |

---

## 1. Prerequisites

| Tool | Version used | Notes |
|---|---|---|
| Java (JDK) | **17** (tested with 17.0.10, also compiles on 21) | `JAVA_HOME` must point to the JDK |
| Maven | **3.9.x** (tested with 3.9.6) | `mvn -v` |
| Browser | Google Chrome (latest) | Firefox / Edge also supported |
| Internet access | – | Maven Central + the OrangeHRM demo site |

No driver binaries need to be installed: **Selenium Manager** downloads the matching driver automatically.

## 2. Technologies

| Purpose | Library |
|---|---|
| Browser automation | Selenium WebDriver 4.25 |
| Test runner / grouping / suites | TestNG 7.10 |
| Reporting | Allure 2 (`allure-testng`, `allure-maven`) |
| Step logging in report | Allure `@Step` via AspectJ weaver |
| Logging | SLF4J (simple logger) |
| Build | Maven, Surefire 3.5 |

## 3. Framework architecture

```
tests  (LoginTest, EmployeeSearchTest, EmployeeTest, EmployeeFilterTest, LeaveTest, NegativeValidationTest)
  │
  ├── extend  BaseTest ──────────── browser per test, login, @AfterSuite data cleanup
  │              ├── DriverManager / DriverFactory   (ThreadLocal WebDriver, Selenium Manager)
  │              ├── TestListener                     (failure screenshot + page source, logs, Allure env)
  │              └── ConfigReader                     (properties → env vars → -D overrides)
  │
  ├── use     flows ─────────────── reusable business flows (create/open employee, ensure leave balance)
  │              │
  └── use     pages ─────────────── Page Objects: BasePage + one class per screen
                                    (explicit waits, selects, autocompletes, toasts, tables)

utils: TestDataFactory (unique data, dates) · DateUtils · ScreenshotUtils · TestDataRegistry (cleanup)
models: Employee · EmployeeRow · LeaveRow
```

Key design decisions:

* **Page Object Model** – every screen is a class in `pages`; tests contain no locators.
  `BasePage` wraps explicit waits, OrangeHRM's custom selects / autocompletes, toasts,
  field validation messages and tables.
* **Rows are located by content, never by index.** Tables are read into objects keyed by
  column header (`EmployeeRow`, `LeaveRow`), and rows are found by employee id / name / comment.
* **No `Thread.sleep`, no implicit waits** – only `WebDriverWait` conditions.
* **Thread-safe driver** (`ThreadLocal`) – a fresh browser per test method, so each test
  re-reads data from the server and can run in parallel.
* **Configuration outside the code** – `config.properties`, overridable by `-D` system properties
  or environment variables.
* **Generated test data** – unique names / ids / comments and runtime-calculated dates.
* **Automatic evidence** – a screenshot (and the page source) is attached to the report the moment
  a test fails, while the browser is still open.

## 4. Project structure

```
.
├── pom.xml
├── testng.xml                     # full regression suite
├── testng-smoke.xml               # smoke group suite
├── docs/
│   ├── report/index.html          # example Allure report (single file)
│   ├── screenshots/               # key checkpoints + failure example
│   └── manual-testing/            # manual QA deliverables
└── src/test/
    ├── resources/
    │   ├── config/config.properties
    │   ├── allure.properties
    │   └── simplelogger.properties
    └── java/com/orangehrm/
        ├── base/BaseTest.java
        ├── config/ConfigReader.java
        ├── driver/DriverFactory.java, DriverManager.java
        ├── listeners/TestListener.java
        ├── flows/EmployeeFlows.java, LeaveFlows.java
        ├── models/Employee.java, EmployeeRow.java, LeaveRow.java
        ├── pages/BasePage.java, LoginPage.java, DashboardPage.java,
        │         EmployeeListPage.java, AddEmployeePage.java, PersonalDetailsPage.java,
        │         ApplyLeavePage.java, MyLeaveListPage.java, AddEntitlementPage.java
        ├── utils/TestDataFactory.java, DateUtils.java, ScreenshotUtils.java, TestDataRegistry.java
        └── tests/LoginTest.java, EmployeeSearchTest.java, EmployeeTest.java,
                  EmployeeFilterTest.java, LeaveTest.java, NegativeValidationTest.java
```

## 5. Test coverage

| Step | Test class / method | Groups |
|---|---|---|
| 1 – Login | `LoginTest.loginPageDisplaysAllControls`, `LoginTest.validLoginOpensDashboard` | smoke, regression |
| 2 – Employee search | `EmployeeSearchTest.searchExistingEmployeeByName` | smoke, regression |
| 3 – Add employee | `EmployeeTest.createEmployee` | smoke, regression |
| 4 – Validate created employee | `EmployeeTest.validateCreatedEmployee` | regression |
| 5 – Edit employee | `EmployeeTest.editEmployee` | regression |
| 6 – Persistence after refresh / navigation | `EmployeeTest.updatedValuesPersistAfterRefreshAndNavigation` | regression |
| 7 – Employee list filtering | `EmployeeFilterTest.filterByJobTitleAndEmploymentStatus`, `EmployeeFilterTest.filterByEmployeeNameAndId` | regression |
| 8 – Leave request | `LeaveTest.submitLeaveRequest` | regression |
| 9 – Negative validation | `NegativeValidationTest` (5 tests) + `LoginTest.invalidPasswordIsRejected` | negative, regression |
| 10 – Test data cleanup | `BaseTest.cleanUpTestData` (`@AfterSuite`), leave cancellation in `LeaveTest` | – |

Negative scenarios implemented:

1. Add employee without first / last name → `Required`, employee not created (searched by the proposed id).
2. Add employee with an Employee Id longer than 10 characters → `Should not exceed 10 characters`, not created.
3. Update an employee with an empty first name → `Required`, value unchanged after reload.
4. Leave request with *To Date* before *From Date* → `To date should be after from date`, not submitted.
5. Leave request without leave type → `Required`, not submitted.
6. Login with an invalid password → `Invalid credentials`.

## 6. How to execute

> **PowerShell users:** wrap every `-D` argument in quotes, e.g. `mvn test "-Dgroups=smoke"`,
> otherwise PowerShell splits the argument at the dot.

```bash
# all tests (every *Test class)
mvn clean test

# individual test class
mvn test -Dtest=EmployeeTest

# individual test method
mvn test -Dtest=LoginTest#validLoginOpensDashboard

# TestNG suite
mvn clean test -DsuiteXmlFile=testng.xml
mvn clean test -DsuiteXmlFile=testng-smoke.xml

# test group
mvn clean test -Dgroups=smoke
mvn clean test -Dgroups=regression
mvn clean test -Dgroups=negative

# headless / other browser
mvn clean test -Dheadless=true
mvn clean test -Dbrowser=firefox
```

`-DsuiteXmlFile` activates the `suite` Maven profile which passes the file to Surefire;
without it Surefire runs the test classes directly, which is what makes `-Dtest` and `-Dgroups` work.

## 7. Reports

Allure results are written to `target/allure-results` on every run.

```bash
# generate a static report in target/site/allure-maven-plugin/index.html
mvn allure:report

# or generate and open it in the browser
mvn allure:serve
```

The report shows every test with its status, duration, steps (`@Step`), failure message and
stack trace, plus attachments: checkpoint screenshots and, for failures, an automatic
screenshot and the page source. The *Environment* widget lists URL, browser and Java version.

The example in [`docs/report/index.html`](docs/report/index.html) is a single self-contained file
(16 tests, all passed) produced with:

```bash
.allure/allure-2.30.0/bin/allure generate --single-file target/allure-results -o docs/report --clean
```

(`.allure/` is downloaded by `mvn allure:report` on first use.)

Surefire / TestNG reports are also available in `target/surefire-reports`.

## 8. Screenshots

Checkpoint screenshots are saved to `target/screenshots/checkpoints` and attached to the report;
failure screenshots go to `target/screenshots/failures`. A selection is committed in
[`docs/screenshots`](docs/screenshots):

| File | Scenario |
|---|---|
| `01_login_dashboard.png` | Successful login / dashboard |
| `02_employee_created.png` | Employee created – generated name and Employee Id |
| `03_employee_search_result.png` | Employee search result |
| `04_employee_updated.png` | Updated employee |
| `05_negative_required_fields.png` | Negative – required field validation |
| `05_negative_leave_invalid_dates.png` | Negative – invalid leave dates |
| `06_leave_request_in_my_leave.png` | Leave request found in My Leave (Pending Approval) |
| `07_filter_job_title_status.png` | Filtering by Job Title + Employment Status |
| `08_failure_auto_screenshot.png` | Screenshot captured automatically on a (deliberate) failure |

## 9. Configuration

`src/test/resources/config/config.properties`:

| Key | Default | Description |
|---|---|---|
| `base.url` | demo URL | Application base URL |
| `app.username` / `app.password` | demo credentials | Credentials published on the demo login page |
| `browser` | `chrome` | `chrome`, `firefox`, `edge` |
| `headless` | `false` | Run without UI |
| `timeout.explicit` | `20` | Explicit wait (s) |
| `leave.min.days.ahead` / `leave.max.days.ahead` | `7` / `45` | Window used to generate leave dates |
| `leave.entitlement.days` | `10` | Days granted when the user has no leave balance |
| `cleanup.enabled` | `true` | Delete created employees after the suite |

Resolution order: `-Dkey=value` → environment variable (`KEY_NAME`, e.g. `APP_PASSWORD`) → properties file.
Credentials can therefore be supplied by CI secrets without touching the code:

```bash
ORANGEHRM_USERNAME=Admin ORANGEHRM_PASSWORD=*** mvn test
```

## 10. Test data strategy

* **Unique generated data** – employees are created as `Automation Test Qa<8 random letters>`;
  the Employee Id generated by OrangeHRM is captured and used as the stable key afterwards.
  Edited values (last name, Other Id) are also generated.
* **No reliance on pre-existing records** – search and filter tests pick their target employee,
  job title and employment status from the live list at runtime, because the shared demo
  data changes constantly.
* **Runtime dates** – leave dates are a random working day 7–45 days ahead (kept within the
  current leave year). If the date overlaps an existing request, another date is tried.
  The date format is read from the application's date-field placeholder (the demo uses `yyyy-dd-mm`).
* **Leave balance set-up** – if the logged-in user has no leave type with balance, the test
  adds an entitlement for that user through *Leave → Entitlements* before applying.

### Cleanup

* Every employee created by a test is registered in `TestDataRegistry`; `BaseTest.cleanUpTestData`
  (`@AfterSuite(alwaysRun = true)`) logs in once and deletes them through PIM, even when tests fail.
  It can be disabled with `-Dcleanup.enabled=false` when the data is needed for debugging.
* The submitted leave request is **cancelled** at the end of `LeaveTest` (in a `finally` block).
  OrangeHRM does not allow deleting leave requests through the UI, so cancellation is the
  cleanest option; it also restores the leave balance.
* **Not cleaned up:** a leave entitlement added as set-up (only when the user has no balance) is
  intentionally left in place – it only grants balance to the demo admin, it is needed by later
  runs, and keeping it avoids re-creating it on every execution.
* The demo environment is also reset periodically by OrangeHRM.

## 11. Notes and observations

* After saving *Personal Details*, the employee name in the profile header is **not refreshed until
  the page is reloaded** (the form fields are). The tests therefore verify the header after a
  refresh / navigation. This would be raised as a minor UI defect.
* The demo is a shared, public environment: other users change data at any time, so the tests
  avoid assumptions about existing records and assert on data they created or read at runtime.
* Tests run sequentially by default. The driver is thread-local, so classes can be run in
  parallel by adding `parallel="classes" thread-count="2"` to the suite file.

### Troubleshooting

* **`PKIX path building failed` while Maven downloads dependencies** – a proxy or antivirus
  intercepts HTTPS. Tell Java to use the Windows certificate store:
  `set MAVEN_OPTS=-Djavax.net.ssl.trustStoreType=Windows-ROOT` (PowerShell: `$env:MAVEN_OPTS="..."`).
* **`Unable to establish loopback connection` when the browser starts (Windows, JDK 17)** – the JDK
  cannot create its internal socket in the temp folder. Point it to a short local folder:
  `set JAVA_TOOL_OPTIONS=-Djdk.net.unixdomain.tmpdir=C:\Temp` (folder must exist).
