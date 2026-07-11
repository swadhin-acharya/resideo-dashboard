 @device @smoke
Feature: Device Management
  Validate device registration and communication

  @DC-101 @smoke
  Scenario: Pair new thermostat to Resideo account
    Given the user has a Resideo account with admin privileges
    When the user initiates thermostat pairing via the app
    And the thermostat enter pairing mode within 60 seconds
    Then the device appears in the user's device list
    And the connection status shows "Connected"

  @DC-102 @regression
  Scenario: Firmware update pushes to thermostat
    Given the thermostat firmware version is "1.3605.1500"
    When a firmware update "1.3605.1550" is available
    And the user initiates the update
    Then the thermostat reboots after flashing
    And the firmware version updates to "1.3605.1550"

  @DC-103 @regression
  Scenario: Multiple thermostats simultaneously
    Given the user has 3 thermostats in their account
    When all thermostats request temperature readings simultaneously
    Then all thermostats respond within 200ms
    And no message collision is detected on the MQTT bus

  @DC-104 @smoke
  Scenario: Device heartbeat monitoring
    Given the thermostat is online and connected
    When the heartbeat interval of 60 seconds elapses
    Then the server receives the heartbeat ping
    And the device status remains "ONLINE"
