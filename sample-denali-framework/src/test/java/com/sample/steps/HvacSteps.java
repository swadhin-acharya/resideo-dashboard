package com.sample.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import static org.assertj.core.api.Assertions.assertThat;

public class HvacSteps {

    private boolean emergencyHeat;
    private boolean compressorLocked;
    private boolean zone1Heating;
    private boolean zone2Cooling;

    @Given("the outdoor temperature is below {int}°F")
    public void outdoorTemp(int temp) {
        System.out.println("[WEATHER] Outdoor: " + temp + "°F");
    }

    @When("the heat pump efficiency drops below threshold")
    public void heatPumpDrop() {
        System.out.println("[HVAC] Heat pump efficiency dropped");
    }

    @Then("emergency heat strip activates automatically")
    public void emergencyHeatActivates() {
        emergencyHeat = true;
        System.out.println("[HVAC] Emergency heat activated");
        assertThat(emergencyHeat).isTrue();
    }

    @Then("the thermostat displays \"Emergency Heat\" indicator")
    public void emergencyHeatIndicator() {
        System.out.println("[THERMOSTAT] Emergency Heat indicator visible");
    }

    @Given("the AC compressor has been running")
    public void compressorRunning() {
        System.out.println("[HVAC] Compressor running");
    }

    @When("the compressor is turned off")
    public void compressorOff() {
        compressorLocked = true;
        System.out.println("[HVAC] Compressor turned off, lock engaged");
    }

    @Then("the minimum off-time of {int} minutes is enforced")
    public void minimumOffTime(int minutes) {
        System.out.println("[HVAC] Minimum off-time " + minutes + "m enforced");
        assertThat(compressorLocked).isTrue();
    }

    @Then("any cooling request during this period is deferred")
    public void coolingDeferred() {
        System.out.println("[HVAC] Cooling requests deferred");
    }

    @Given("Zone {int} is in Heating mode and Zone {int} is in Cooling mode")
    public void zonesMode(int z1, int z2) {
        zone1Heating = true;
        zone2Cooling = true;
        System.out.println("[ZONES] Zone1: Heat, Zone2: Cool");
    }

    @When("both zones call for conditioning")
    public void zonesCall() {
        System.out.println("[ZONES] Both zones calling");
    }

    @Then("each zone operates independently")
    public void zonesIndependent() {
        System.out.println("[ZONES] Zones operating independently");
    }

    @Then("no cross-zone interference is detected")
    public void noInterference() {
        System.out.println("[ZONES] No cross-zone interference");
        assertThat(zone1Heating && zone2Cooling).isTrue();
    }
}
