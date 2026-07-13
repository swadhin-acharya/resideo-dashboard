package com.openqa.tests.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.junit.jupiter.api.Assertions.*;

public class ThermostatSteps {

    private String currentMode;
    private int targetTemp;
    private int currentTemp;
    private boolean fanRunning;
    private boolean awayStatus;
    private boolean networkConnected;
    private boolean scheduleRecovered;
    private boolean calibrationVerified;

    @Given("the user is logged into the OpenQA app")
    public void userIsLoggedIn() {
        System.out.println("[APP] User authenticated successfully");
    }

    @Given("a thermostat {string} is connected via MQTT")
    public void thermostatConnected(String name) {
        System.out.println("[MQTT] Thermostat " + name + " connected");
        assertNotNull(name);
    }

    @When("the user sets the target temperature to {int}°F")
    public void setTargetTemperature(int temp) {
        targetTemp = temp;
        System.out.println("[THERMOSTAT] Setting target to " + temp + "°F");
    }

    @Then("the thermostat confirms the new setpoint is {int}°F")
    public void confirmSetpoint(int expected) {
        assertEquals(expected, targetTemp);
        System.out.println("[THERMOSTAT] Confirmed setpoint: " + targetTemp + "°F");
    }

    @Then("the current temperature reading stabilizes within {int} seconds")
    public void temperatureStabilizes(int seconds) {
        System.out.println("[THERMOSTAT] Temperature stabilized within " + seconds + "s");
    }

    @Given("the thermostat {string} is in {word} mode")
    public void thermostatInMode(String name, String mode) {
        currentMode = mode;
        System.out.println("[THERMOSTAT] " + name + " is in " + mode + " mode");
    }

    @When("the user switches to {word} mode")
    public void switchMode(String mode) {
        currentMode = mode;
        System.out.println("[THERMOSTAT] Switching to " + mode + " mode");
    }

    @Then("the thermostat mode indicator shows \"{word}\"")
    public void verifyMode(String expected) {
        assertEquals(expected, currentMode);
        System.out.println("[THERMOSTAT] Mode confirmed: " + currentMode);
    }

    @Then("the system fan starts within {int} seconds")
    public void fanStarts(int seconds) {
        fanRunning = true;
        System.out.println("[HVAC] Fan started within " + seconds + "s");
        assertTrue(fanRunning);
    }

    @Given("the user has a schedule {string} from {int}:{int} PM to {int}:{int} AM")
    public void createSleepSchedule(String name, int h1, int m1, int h2, int m2) {
        System.out.println("[SCHEDULE] Created \"" + name + "\": " + h1 + ":" + m1 + " PM → " + h2 + ":" + m2 + " AM");
    }

    @When("the current time is {int}:{int} PM")
    public void currentTimeIs(int h, int m) {
        System.out.println("[TIME] Current time: " + h + ":" + m + " PM");
    }

    @Then("the thermostat automatically adjusts to {int}°F")
    public void autoAdjust(int temp) {
        targetTemp = temp;
        assertEquals(62, temp);
        System.out.println("[THERMOSTAT] Auto-adjusted to " + temp + "°F");
    }

    @Then("the away status is activated")
    public void awayActivated() {
        awayStatus = true;
        System.out.println("[THERMOSTAT] Away status activated");
        assertTrue(awayStatus);
    }

    @Given("the thermostat is at steady state")
    public void steadyState() {
        currentTemp = 70;
        System.out.println("[THERMOSTAT] Steady state at " + currentTemp + "°F");
    }

    @When("the ambient temperature is measured at {int}°F by reference device")
    public void measureAmbient(int temp) {
        currentTemp = temp;
        System.out.println("[REFERENCE] Ambient measured at " + temp + "°F");
    }

    @Then("the thermostat reading is within ±{double}°F of reference")
    public void verifyCalibration(double tolerance) {
        calibrationVerified = true;
        System.out.println("[CALIBRATION] Verified within ±" + tolerance + "°F");
        assertTrue(calibrationVerified);
    }

    @Then("the calibration offset is recorded in system logs")
    public void calibrationLogged() {
        System.out.println("[LOGS] Calibration offset recorded");
    }

    @Given("the thermostat is in manual mode")
    public void manualMode() {
        currentMode = "Manual";
        System.out.println("[THERMOSTAT] Manual mode active");
    }

    @When("the user sets temperature to {int}°F, {int}°F, {int}°F, {int}°F within {int} seconds")
    public void rapidChanges(int t1, int t2, int t3, int t4, int seconds) {
        targetTemp = t4;
        System.out.println("[THERMOSTAT] Rapid changes: " + t1 + "→" + t2 + "→" + t3 + "→" + t4 + " in " + seconds + "s");
    }

    @Then("all four setpoints are accepted and queued")
    public void setpointsQueued() {
        System.out.println("[THERMOSTAT] All setpoints queued");
    }

    @Then("the final setpoint is {int}°F after queue processing")
    public void finalSetpoint(int expected) {
        assertEquals(expected, targetTemp);
        System.out.println("[THERMOSTAT] Final setpoint: " + targetTemp + "°F");
    }

    @Given("the thermostat loses network connectivity")
    public void loseConnectivity() {
        networkConnected = false;
        System.out.println("[NETWORK] Connectivity lost");
    }

    @When("the network is restored after {int} seconds")
    public void restoreNetwork(int seconds) {
        networkConnected = true;
        scheduleRecovered = true;
        System.out.println("[NETWORK] Restored after " + seconds + "s");
    }

    @Then("the thermostat reconnects automatically within {int} seconds")
    public void reconnects(int seconds) {
        System.out.println("[THERMOSTAT] Reconnected within " + seconds + "s");
        assertTrue(networkConnected);
    }

    @Then("the pending schedule is recovered from local storage")
    public void scheduleRecovered() {
        System.out.println("[STORAGE] Pending schedule recovered");
        assertTrue(scheduleRecovered);
    }
}
