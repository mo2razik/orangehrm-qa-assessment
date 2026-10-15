package com.orangehrm.pages;

import com.orangehrm.config.ConfigReader;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Common behaviour shared by every page object: explicit waits, typing into
 * OrangeHRM (Vue) controls, toasts, field validation messages and navigation.
 */
public abstract class BasePage {

    protected static final By LOADERS = By.cssSelector(".oxd-loading-spinner, .oxd-form-loader");
    protected static final By TOAST = By.cssSelector(".oxd-toast");
    protected static final By TOAST_MESSAGE = By.cssSelector(".oxd-toast .oxd-text--toast-message");
    protected static final By PAGE_HEADER = By.cssSelector(".oxd-topbar-header-breadcrumb h6");
    protected static final By USER_DROPDOWN_NAME = By.cssSelector(".oxd-userdropdown-name");
    private static final By SELECT_OPTIONS = By.cssSelector("div[role='listbox'] div[role='option']");
    private static final By AUTOCOMPLETE_OPTIONS = By.cssSelector(".oxd-autocomplete-dropdown div[role='option']");

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.explicitTimeout()));
    }

    // ---------------------------------------------------------------- waits

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement clickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected boolean isVisible(By locator, int seconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(seconds))
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void waitForLoadersToDisappear() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(LOADERS));
    }

    protected void waitForUrlContains(String fragment) {
        wait.until(ExpectedConditions.urlContains(fragment));
    }

    // -------------------------------------------------------------- actions

    protected void click(By locator) {
        waitForLoadersToDisappear();
        try {
            clickable(locator).click();
        } catch (StaleElementReferenceException e) {
            clickable(locator).click();
        } catch (org.openqa.selenium.ElementClickInterceptedException e) {
            waitForLoadersToDisappear();
            jsClick(visible(locator));
        }
    }

    protected void jsClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    /**
     * Clears and types into an input. OrangeHRM inputs are Vue-bound, so a plain
     * WebElement#clear() does not always update the model; select-all + delete does.
     */
    protected void type(By locator, String text) {
        WebElement input = visible(locator);
        clearInput(input);
        if (text != null && !text.isEmpty()) {
            input.sendKeys(text);
        }
    }

    protected void clearInput(WebElement input) {
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        String remaining = input.getAttribute("value");
        for (int i = 0; remaining != null && i < remaining.length(); i++) {
            input.sendKeys(Keys.BACK_SPACE);
        }
    }

    protected String text(By locator) {
        return visible(locator).getText().trim();
    }

    protected String value(By locator) {
        String v = visible(locator).getAttribute("value");
        return v == null ? "" : v.trim();
    }

    // ------------------------------------------- OrangeHRM form helpers

    /** Input inside the form group whose label is {@code label}. */
    protected static By inputByLabel(String label) {
        return By.xpath("//label[normalize-space()='" + label + "']"
                + "/ancestor::div[contains(concat(' ',normalize-space(@class),' '),' oxd-input-group ')][1]//input");
    }

    /** Custom select ("-- Select --") inside the form group whose label is {@code label}. */
    protected static By selectByLabel(String label) {
        return By.xpath("//label[normalize-space()='" + label + "']"
                + "/ancestor::div[contains(concat(' ',normalize-space(@class),' '),' oxd-input-group ')][1]//div[contains(@class,'oxd-select-text-input')]");
    }

    /** Validation message displayed under the form group whose label is {@code label}. */
    protected static By errorByLabel(String label) {
        return By.xpath("//label[normalize-space()='" + label + "']"
                + "/ancestor::div[contains(concat(' ',normalize-space(@class),' '),' oxd-input-group ')][1]//span[contains(@class,'oxd-input-field-error-message')]");
    }

    protected static By buttonByText(String text) {
        return By.xpath("//button[normalize-space()='" + text + "']");
    }

    /** Opens a custom select and chooses the option with the exact visible text. */
    protected void chooseOption(By select, String optionText) {
        click(select);
        By option = By.xpath("//div[@role='listbox']//div[@role='option'][normalize-space()='" + optionText + "']");
        click(option);
    }

    /** Opens a custom select and returns all real options (placeholder "-- Select --" excluded). */
    protected List<String> listOptions(By select) {
        click(select);
        wait.until(ExpectedConditions.visibilityOfElementLocated(SELECT_OPTIONS));
        List<String> options = driver.findElements(SELECT_OPTIONS).stream()
                .map(e -> e.getText().trim())
                .filter(t -> !t.isEmpty() && !t.startsWith("--"))
                .toList();
        // close the dropdown again
        driver.findElement(select).click();
        return options;
    }

    protected String selectedText(By select) {
        return visible(select).getText().trim();
    }

    /**
     * Types into an autocomplete and picks the first suggestion containing all of {@code mustContain}.
     * Returns the chosen suggestion text, or empty when no matching suggestion appeared.
     */
    protected Optional<String> typeAndPickSuggestion(By input, String text, String... mustContain) {
        type(input, text);
        try {
            WebDriverWait suggestWait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.explicitTimeout()));
            WebElement match = suggestWait.until(d -> d.findElements(AUTOCOMPLETE_OPTIONS).stream()
                    .filter(o -> {
                        String t = o.getText().toLowerCase();
                        if (t.contains("searching")) {
                            return false;
                        }
                        for (String part : mustContain) {
                            if (!t.contains(part.toLowerCase())) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .findFirst().orElse(null));
            String chosen = match.getText().trim();
            match.click();
            return Optional.of(chosen);
        } catch (TimeoutException | StaleElementReferenceException e) {
            return Optional.empty();
        }
    }

    // ----------------------------------------------------------- tables

    protected static final By TABLE_HEADER_CELLS = By.cssSelector(".oxd-table-header .oxd-table-th");
    protected static final By TABLE_ROW_CARDS = By.cssSelector(".oxd-table-body .oxd-table-card");
    protected static final By TABLE_CELLS = By.cssSelector(".oxd-table-cell");

    /**
     * Reads the visible OrangeHRM table into maps keyed by column header,
     * so tests address cells by meaning rather than by position.
     */
    protected List<Map<String, String>> readTable() {
        waitForLoadersToDisappear();
        List<String> headers = driver.findElements(TABLE_HEADER_CELLS).stream()
                .map(h -> h.getText().trim())
                .toList();
        List<Map<String, String>> rows = new ArrayList<>();
        for (WebElement card : driver.findElements(TABLE_ROW_CARDS)) {
            List<WebElement> cells = card.findElements(TABLE_CELLS);
            Map<String, String> row = new LinkedHashMap<>();
            for (int i = 0; i < headers.size() && i < cells.size(); i++) {
                row.put(headers.get(i), cells.get(i).getText().trim().replaceAll("\\s+", " "));
            }
            rows.add(row);
        }
        return rows;
    }

    // ---------------------------------------------------------- toasts

    /** Waits for a toast and returns its message (e.g. "Successfully Saved"). */
    @Step("Wait for notification")
    public String waitForToastMessage() {
        String message = visible(TOAST_MESSAGE).getText().trim();
        return message;
    }

    public boolean isToastDisplayed(int seconds) {
        return isVisible(TOAST, seconds);
    }

    public void waitForToastToDisappear() {
        new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.invisibilityOfElementLocated(TOAST));
    }

    // ------------------------------------------------------ navigation

    public String getPageHeader() {
        return text(PAGE_HEADER);
    }

    public String getLoggedInUserName() {
        return text(USER_DROPDOWN_NAME);
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    @Step("Open main menu item \"{item}\"")
    public void openMainMenu(String item) {
        click(By.xpath("//a[contains(@class,'oxd-main-menu-item')][.//span[normalize-space()='" + item + "']]"));
        waitForLoadersToDisappear();
    }

    @Step("Open top bar item \"{item}\"")
    public void openTopBarItem(String item) {
        click(By.xpath("//nav[contains(@class,'oxd-topbar-body-nav')]//a[normalize-space()='" + item + "']"));
        waitForLoadersToDisappear();
    }

    protected void openPath(String path) {
        driver.get(ConfigReader.baseUrl() + path);
        waitForLoadersToDisappear();
    }

    public void refresh() {
        driver.navigate().refresh();
        waitForLoadersToDisappear();
    }
}
