package com.sample.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ThermostatPage extends BasePage {

    @FindBy(css = ".current-temp")
    private WebElement currentTemp;

    @FindBy(css = ".target-temp")
    private WebElement targetTemp;

    @FindBy(css = ".mode-indicator")
    private WebElement modeIndicator;

    @FindBy(css = ".temp-up")
    private WebElement tempUpButton;

    @FindBy(css = ".temp-down")
    private WebElement tempDownButton;

    @FindBy(css = ".mode-selector")
    private WebElement modeSelector;

    @FindBy(css = ".fan-status")
    private WebElement fanStatus;

    public ThermostatPage(WebDriver driver) {
        super(driver);
    }

    public String getCurrentTemperature() {
        return wait.waitForVisible(currentTemp).getText();
    }

    public String getTargetTemperature() {
        return wait.waitForVisible(targetTemp).getText();
    }

    public String getMode() {
        return wait.waitForVisible(modeIndicator).getText();
    }

    public void increaseTemperature() {
        wait.waitForClickable(tempUpButton).click();
    }

    public void decreaseTemperature() {
        wait.waitForClickable(tempDownButton).click();
    }

    public void selectMode(String mode) {
        wait.waitForClickable(modeSelector).click();
        WebElement option = driver.findElement(
                org.openqa.selenium.By.cssSelector(".mode-option[data-mode='" + mode + "']"));
        wait.waitForClickable(option).click();
    }

    public boolean isFanRunning() {
        return wait.waitForVisible(fanStatus).getText().contains("Running");
    }

    public String getFanStatus() {
        return wait.waitForVisible(fanStatus).getText();
    }
}
