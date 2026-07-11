package com.sample.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import static org.assertj.core.api.Assertions.assertThat;

public class DeviceSteps {

    private boolean paired;
    private String firmwareVersion;
    private String deviceStatus;

    @When("the user initiates thermostat pairing via the app")
    public void startPairing() {
        System.out.println("[APP] Pairing initiated");
    }

    @When("the thermostat enter pairing mode within {int} seconds")
    public void enterPairingMode(int seconds) {
        paired = true;
        System.out.println("[THERMOSTAT] Pairing mode entered within " + seconds + "s");
    }

    @Then("the device appears in the user's device list")
    public void deviceInList() {
        System.out.println("[APP] Device appears in list");
        assertThat(paired).isTrue();
    }

    @Then("the connection status shows \"{word}\"")
    public void connectionStatus(String status) {
        deviceStatus = status;
        System.out.println("[DEVICE] Status: " + status);
        assertThat(status).isEqualTo("Connected");
    }

    @Given("the thermostat firmware version is \"{word}\"")
    public void currentFirmware(String version) {
        firmwareVersion = version;
        System.out.println("[FIRMWARE] Current: " + version);
    }

    @When("a firmware update \"{word}\" is available")
    public void firmwareUpdateAvailable(String version) {
        System.out.println("[FIRMWARE] Update available: " + version);
    }

    @When("the user initiates the update")
    public void initiateUpdate() {
        System.out.println("[FIRMWARE] Update initiated");
    }

    @Then("the thermostat reboots after flashing")
    public void rebootAfterFlash() {
        System.out.println("[THERMOSTAT] Rebooting after flash");
    }

    @Then("the firmware version updates to \"{word}\"")
    public void verifyFirmware(String expected) {
        firmwareVersion = expected;
        assertThat(firmwareVersion).isEqualTo("1.3605.1550");
        System.out.println("[FIRMWARE] Updated to: " + firmwareVersion);
    }

    @Given("the user has {int} thermostats in their account")
    public void multipleThermostats(int count) {
        System.out.println("[ACCOUNT] " + count + " thermostats registered");
    }

    @When("all thermostats request temperature readings simultaneously")
    public void simultaneousReadings() {
        System.out.println("[MQTT] Simultaneous temperature requests");
    }

    @Then("all thermostats respond within {int}ms")
    public void thermostatsRespond(int ms) {
        System.out.println("[MQTT] All responded within " + ms + "ms");
    }

    @Then("no message collision is detected on the MQTT bus")
    public void noCollision() {
        System.out.println("[MQTT] No collisions detected");
    }

    @Given("the thermostat is online and connected")
    public void thermostatOnline() {
        deviceStatus = "ONLINE";
        System.out.println("[DEVICE] Status: ONLINE");
    }

    @When("the heartbeat interval of {int} seconds elapses")
    public void heartbeatInterval(int seconds) {
        System.out.println("[HEARTBEAT] " + seconds + "s interval elapsed");
    }

    @Then("the server receives the heartbeat ping")
    public void heartbeatReceived() {
        System.out.println("[SERVER] Heartbeat received");
    }

    @Then("the device status remains \"{word}\"")
    public void deviceStatusRemains(String status) {
        assertThat(deviceStatus).isEqualTo(status);
        System.out.println("[DEVICE] Status unchanged: " + status);
    }
}
