package com.orangehrm.tests;

import com.orangehrm.base.BaseTest;
import com.orangehrm.pages.DashboardPage;
import com.orangehrm.pages.LoginPage;
import com.orangehrm.utils.TestDataFactory;
import com.orangehrm.config.ConfigReader;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Epic("OrangeHRM")
@Feature("Authentication")
public class LoginTest extends BaseTest {

    @Override
    protected boolean loginBeforeEachTest() {
        return false;
    }

    @Test(groups = {"smoke", "regression"}, description = "Login page shows the username, password and login controls")
    @Severity(SeverityLevel.BLOCKER)
    public void loginPageDisplaysAllControls() {
        LoginPage loginPage = new LoginPage(driver()).open();

        SoftAssert soft = new SoftAssert();
        soft.assertTrue(loginPage.isLoginPageDisplayed(), "Login page is displayed");
        soft.assertTrue(loginPage.isUsernameFieldAvailable(), "Username field is available");
        soft.assertTrue(loginPage.isPasswordFieldAvailable(), "Password field is available and masked");
        soft.assertTrue(loginPage.isLoginButtonAvailable(), "Login button is available");
        soft.assertAll();
    }

    @Test(groups = {"smoke", "regression"}, description = "Valid credentials open the dashboard with the user information")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Step 1 - login with the configured demo credentials and verify the dashboard")
    public void validLoginOpensDashboard() {
        DashboardPage dashboard = new LoginPage(driver()).open().loginWithConfiguredUser();

        assertTrue(dashboard.isDashboardDisplayed(), "Dashboard should be displayed after login");
        assertEquals(dashboard.getPageHeader(), "Dashboard");
        assertFalse(dashboard.getLoggedInUserName().isBlank(), "Logged-in user name should be displayed");
        assertTrue(dashboard.isUserAvatarDisplayed(), "User avatar should be displayed");
        assertTrue(dashboard.widgetCount() > 0, "Dashboard widgets should be displayed");
        checkpoint("01_login_dashboard");
    }

    @Test(groups = {"negative", "regression"}, description = "Invalid password is rejected")
    @Severity(SeverityLevel.CRITICAL)
    public void invalidPasswordIsRejected() {
        LoginPage loginPage = new LoginPage(driver()).open()
                .submitCredentials(ConfigReader.username(), "wrong-" + TestDataFactory.randomLetters(6));

        assertEquals(loginPage.getErrorMessage(), "Invalid credentials");
        assertTrue(loginPage.isLoginPageDisplayed(), "User should stay on the login page");
        assertFalse(loginPage.currentUrl().contains("/dashboard"), "Dashboard must not be reachable");
    }
}
