package com.orangehrm.utils;

import com.orangehrm.config.ConfigReader;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Captures screenshots, stores them on disk and attaches them to the Allure report.
 */
public final class ScreenshotUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ScreenshotUtils.class);
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private ScreenshotUtils() {
    }

    /**
     * Captures a checkpoint screenshot (target/screenshots/checkpoints).
     */
    public static Path checkpoint(WebDriver driver, String name) {
        return capture(driver, "checkpoints", name);
    }

    /**
     * Captures a failure screenshot plus the page source (target/screenshots/failures).
     */
    public static Path failure(WebDriver driver, String testName) {
        Path path = capture(driver, "failures", testName);
        try {
            String source = driver.getPageSource();
            Allure.addAttachment("Page source on failure - " + testName, "text/html",
                    new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)), ".html");
        } catch (RuntimeException e) {
            LOG.warn("Could not capture page source: {}", e.getMessage());
        }
        return path;
    }

    private static Path capture(WebDriver driver, String folder, String name) {
        try {
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(name, "image/png", new ByteArrayInputStream(png), ".png");

            Path dir = Paths.get(ConfigReader.get("screenshots.dir"), folder);
            Files.createDirectories(dir);
            Path file = dir.resolve(sanitize(name) + "_" + LocalDateTime.now().format(STAMP) + ".png");
            Files.write(file, png);
            LOG.info("Screenshot saved: {}", file);
            return file;
        } catch (IOException | RuntimeException e) {
            LOG.warn("Unable to capture screenshot '{}': {}", name, e.getMessage());
            return null;
        }
    }

    private static String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9-_]+", "_");
    }
}
