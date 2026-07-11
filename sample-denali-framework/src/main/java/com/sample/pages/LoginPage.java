package com.sample.pages;

import com.sample.config.Config;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginPage extends BasePage {

    @FindBy(id = "email")
    private WebElement emailInput;

    @FindBy(id = "password")
    private WebElement passwordInput;

    @FindBy(id = "login-button")
    private WebElement loginButton;

    @FindBy(css = ".error-message")
    private WebElement errorMessage;

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage navigate() {
        driver.get(Config.App.baseUrl() + "/login");
        return this;
    }

    public LoginPage enterEmail(String email) {
        wait.waitForVisible(emailInput).sendKeys(email);
        return this;
    }

    public LoginPage enterPassword(String password) {
        wait.waitForVisible(passwordInput).sendKeys(password);
        return this;
    }

    public void clickLogin() {
        wait.waitForClickable(loginButton).click();
    }

    public void loginAs(String username, String password) {
        enterEmail(username);
        enterPassword(password);
        clickLogin();
    }

    public void loginAsDefault() {
        loginAs(Config.App.username(), Config.App.password());
    }

    public String getErrorMessage() {
        return wait.waitForVisible(errorMessage).getText();
    }
}
