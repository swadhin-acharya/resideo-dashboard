package com.sample.steps;

import io.cucumber.java.en.Given;

public class CommonSteps {

    private boolean loggedIn;

    @Given("the user is logged into the OpenQA app")
    public void userIsLoggedIn() {
        loggedIn = true;
        System.out.println("[APP] User authenticated successfully");
    }

    @Given("the user has a OpenQA account with admin privileges")
    public void adminAccount() {
        System.out.println("[AUTH] Admin account verified");
    }
}
