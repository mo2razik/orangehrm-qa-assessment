# OrangeHRM QA Assessment

Automated UI tests for the [OrangeHRM demo](https://opensource-demo.orangehrmlive.com/) and the manual testing work for the *Employee Leave Request* feature.

## Prerequisites

- Java 17
- Maven 3.9+
- Google Chrome (Firefox and Edge also work)

Browser drivers are downloaded automatically by Selenium.

## Technologies

Java · Maven · Selenium WebDriver 4 · TestNG · Allure Reports

## Project structure

```
src/test/java/com/orangehrm/
├── base/        BaseTest – opens the browser, logs in, cleans up test data
├── config/      Reads config.properties
├── driver/      Creates and holds the WebDriver
├── pages/       Page Objects (one class per screen)
├── flows/       Reusable steps (create employee, prepare leave balance)
├── tests/       Test classes
├── listeners/   Screenshot on failure, logging
├── models/      Employee and table-row objects
└── utils/       Test data generator, dates, screenshots
src/test/resources/config/config.properties   – URL, credentials, browser, timeouts
testng.xml / testng-smoke.xml                  – TestNG suites
docs/                                          – screenshots, example reports, manual testing
```

The framework uses the **Page Object Model**: tests call page methods, and all locators and waits live in the page classes.

## Tests

| Test class | What it covers |
|---|---|
| `LoginTest` | Login page, valid login, invalid password |
| `EmployeeSearchTest` | Search an existing employee by name |
| `EmployeeTest` | Create → validate → edit → check after refresh and navigation |
| `EmployeeFilterTest` | Filter by two criteria and reset |
| `LeaveTest` | Apply for leave and find it in My Leave |
| `NegativeValidationTest` | Required fields, invalid Employee Id, invalid leave dates, missing leave type |

Groups: `smoke`, `regression`, `negative`.

## How to run

```bash
mvn clean test                              # all tests
mvn test -Dtest=EmployeeTest                # one test class
mvn clean test -DsuiteXmlFile=testng.xml    # TestNG suite
mvn clean test -Dgroups=smoke               # one group
mvn clean test -Dheadless=true              # without opening the browser
```

On PowerShell, put quotes around `-D` options: `mvn test "-Dgroups=smoke"`.

## Reports

```bash
mvn allure:serve      # opens the report in the browser
mvn allure:report     # creates target/site/allure-maven-plugin/index.html
```

The report shows each test's name, status, duration, steps and errors. When a test fails, a screenshot and the page source are attached automatically.

Example reports (download and open in a browser):

- `docs/report/index.html` – full run, all 16 tests passed
- `docs/report-with-failure/index.html` – same run plus one failed test with its screenshot

Screenshots of the main steps are in `docs/screenshots`.

## Configuration

Settings are in `src/test/resources/config/config.properties` (URL, credentials, browser, headless, timeouts). Any value can be overridden from the command line, for example `-Dbrowser=firefox`. Credentials can also come from the environment variables `ORANGEHRM_USERNAME` and `ORANGEHRM_PASSWORD`.

## Test data

- New employees get a unique generated name (`Automation Test Qa<random>`), and their Employee Id is captured after saving.
- Search and filter tests pick their data from the live employee list, so nothing depends on a fixed record or row number.
- Leave dates are calculated at runtime (a working day 7–45 days ahead).
- **Cleanup:** employees created by the tests are deleted at the end of the run, and the leave request is cancelled after it is checked. Set `-Dcleanup.enabled=false` to keep the data for debugging.

## Manual testing

In `docs/manual-testing`:

1. `01-requirements-review.md` – questions, missing requirements, assumptions and risks
2. `02-test-cases.md` – test approach and 64 test cases
3. `03-bug-report.md` – bug report for the leave balance issue

## Troubleshooting

- **`PKIX path building failed`** while Maven downloads: set `MAVEN_OPTS=-Djavax.net.ssl.trustStoreType=Windows-ROOT`.
- **`Unable to establish loopback connection`** when the browser starts (Windows, JDK 17): set `JAVA_TOOL_OPTIONS=-Djdk.net.unixdomain.tmpdir=C:\Temp` (the folder must exist).
