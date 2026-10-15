package com.orangehrm.pages;

import com.orangehrm.config.ConfigReader;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    private static final By TITLE = By.cssSelector("h5.orangehrm-login-title");
    private static final By USERNAME = By.name("username");
    private static final By PASSWORD = By.name("password");
    private static final By LOGIN_BUTTON = By.cssSelector("button.orangehrm-login-button");
    private static final By ERROR_ALERT = By.cssSelector(".oxd-alert-content-text");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    @Step("Open login page")
    public LoginPage open() {
        driver.get(ConfigReader.baseUrl() + "/auth/login");
        visible(TITLE);
        return this;
    }

    public boolean isLoginPageDisplayed() {
        return isVisible(TITLE, 10) && text(TITLE).equals("Login");
    }

    public boolean isUsernameFieldAvailable() {
        return visible(USERNAME).isEnabled();
    }

    public boolean isPasswordFieldAvailable() {
        return visible(PASSWORD).isEnabled() && "password".equals(visible(PASSWORD).getAttribute("type"));
    }

    public boolean isLoginButtonAvailable() {
        return clickable(LOGIN_BUTTON).isDisplayed();
    }

    @Step("Log in as \"{username}\"")
    public DashboardPage loginAs(String username, String password) {
        submitCredentials(username, password);
        return new DashboardPage(driver).waitUntilLoaded();
    }

    @Step("Log in with configured credentials")
    public DashboardPage loginWithConfiguredUser() {
        return loginAs(ConfigReader.username(), ConfigReader.password());
    }

    @Step("Submit credentials for \"{username}\"")
    public LoginPage submitCredentials(String username, String password) {
        type(USERNAME, username);
        type(PASSWORD, password);
        click(LOGIN_BUTTON);
        return this;
    }

    public String getErrorMessage() {
        return text(ERROR_ALERT);
    }
}
