@thermostat @smoke
Feature: Thermostat Temperature Control
  Validate thermostat temperature adjustments and mode switching

  @TCC-101 @smoke
  Scenario: Set target temperature on connected thermostat
    Given the user is logged into the OpenQA app
    And a thermostat "Living Room" is connected via MQTT
    When the user sets the target temperature to 72°F
    Then the thermostat confirms the new setpoint is 72°F
    And the current temperature reading stabilizes within 30 seconds

  @TCC-102 @smoke
  Scenario: Switch thermostat mode from Heat to Cool
    Given the thermostat "Living Room" is in Heat mode
    When the user switches to Cool mode
    Then the thermostat mode indicator shows "Cool"
    And the system fan starts within 5 seconds

  @TCC-103 @regression
  Scenario: Schedule temperature change for overnight setback
    Given the user has a schedule "Night Setback" from 11:00 PM to 6:00 AM
    When the current time is 11:00 PM
    Then the thermostat automatically adjusts to 62°F
    And the away status is activated

  @TCC-104 @regression
  Scenario: Verify temperature calibration accuracy
    Given the thermostat is at steady state
    When the ambient temperature is measured at 70°F by reference device
    Then the thermostat reading is within ±0.5°F of reference
    And the calibration offset is recorded in system logs

  @TCC-105 @stress
  Scenario: Handle rapid sequential temperature changes
    Given the thermostat is in manual mode
    When the user sets temperature to 68°F, 72°F, 65°F, 74°F within 10 seconds
    Then all four setpoints are accepted and queued
    And the final setpoint is 74°F after queue processing

  @TCC-106 @regression @failed
  Scenario: Thermostat offline reconnection
    Given the thermostat loses network connectivity
    When the network is restored after 30 seconds
    Then the thermostat reconnects automatically within 5 seconds
    And the pending schedule is recovered from local storage
