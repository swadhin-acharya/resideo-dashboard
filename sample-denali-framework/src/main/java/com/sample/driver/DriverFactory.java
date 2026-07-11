package com.sample.driver;

import com.sample.config.Config;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

public class DriverFactory {

    private static final ThreadLocal<WebDriver> driverThread = new ThreadLocal<>();

    public static WebDriver createDriver() {
        WebDriver driver = createDriverInstance();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(Config.App.implicitWait()));
        driver.manage().window().maximize();
        driverThread.set(driver);
        return driver;
    }

    private static WebDriver createDriverInstance() {
        String browser = Config.Browser.name().toLowerCase();
        boolean headless = Config.Browser.headless();

        switch (browser) {
            case "chrome": {
                ChromeOptions opts = new ChromeOptions();
                if (headless) opts.addArguments("--headless=new");
                opts.addArguments("--no-sandbox", "--disable-dev-shm-usage");
                return new ChromeDriver(opts);
            }
            case "firefox": {
                FirefoxOptions opts = new FirefoxOptions();
                if (headless) opts.addArguments("--headless");
                return new FirefoxDriver(opts);
            }
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
    }

    public static WebDriver getDriver() {
        return driverThread.get();
    }

    public static void quitDriver() {
        WebDriver driver = driverThread.get();
        if (driver != null) {
            driver.quit();
            driverThread.remove();
        }
    }
}
