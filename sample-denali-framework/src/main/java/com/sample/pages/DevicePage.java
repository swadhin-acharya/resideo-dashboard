package com.sample.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class DevicePage extends BasePage {

    @FindBy(css = ".device-list")
    private WebElement deviceList;

    @FindBy(css = ".device-item")
    private List<WebElement> deviceItems;

    @FindBy(css = ".pair-device-btn")
    private WebElement pairDeviceButton;

    @FindBy(css = ".connection-status")
    private WebElement connectionStatus;

    @FindBy(css = ".firmware-version")
    private WebElement firmwareVersion;

    public DevicePage(WebDriver driver) {
        super(driver);
    }

    public void clickPairDevice() {
        wait.waitForClickable(pairDeviceButton).click();
    }

    public int getDeviceCount() {
        return wait.waitForVisible(deviceList).findElements(
                org.openqa.selenium.By.cssSelector(".device-item")).size();
    }

    public boolean isDeviceInList(String deviceName) {
        return deviceItems.stream()
                .anyMatch(item -> item.getText().contains(deviceName));
    }

    public String getConnectionStatus() {
        return wait.waitForVisible(connectionStatus).getText();
    }

    public String getFirmwareVersion() {
        return wait.waitForVisible(firmwareVersion).getText();
    }

    public boolean isOnline() {
        return getConnectionStatus().equalsIgnoreCase("ONLINE");
    }
}
