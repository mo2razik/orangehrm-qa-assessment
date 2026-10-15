package com.orangehrm.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DashboardPage extends BasePage {

    private static final By WIDGETS = By.cssSelector(".orangehrm-dashboard-widget");
    private static final By USER_AVATAR = By.cssSelector(".oxd-userdropdown-img");

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    @Step("Wait for dashboard")
    public DashboardPage waitUntilLoaded() {
        waitForUrlContains("/dashboard");
        visible(PAGE_HEADER);
        waitForLoadersToDisappear();
        return this;
    }

    public boolean isDashboardDisplayed() {
        return currentUrl().contains("/dashboard/index") && "Dashboard".equals(getPageHeader());
    }

    public int widgetCount() {
        visible(WIDGETS);
        return driver.findElements(WIDGETS).size();
    }

    public boolean isUserAvatarDisplayed() {
        return visible(USER_AVATAR).isDisplayed();
    }

    @Step("Navigate to PIM > Employee List")
    public EmployeeListPage goToEmployeeList() {
        openMainMenu("PIM");
        return new EmployeeListPage(driver).waitUntilLoaded();
    }
}
