@hvac @regression
Feature: HVAC System Integration
  Validate HVAC system responses and safety controls

  @HVC-101 @smoke
  Scenario: Emergency heat activates during extreme cold
    Given the outdoor temperature is below 10°F
    When the heat pump efficiency drops below threshold
    Then emergency heat strip activates automatically
    And the thermostat displays "Emergency Heat" indicator

  @HVC-102 @regression
  Scenario: Compressor short cycle protection
    Given the AC compressor has been running
    When the compressor is turned off
    Then the minimum off-time of 5 minutes is enforced
    And any cooling request during this period is deferred

  @HVC-103 @stress
  Scenario: Simultaneous heating and cooling zones
    Given Zone 1 is in Heating mode and Zone 2 is in Cooling mode
    When both zones call for conditioning
    Then each zone operates independently
    And no cross-zone interference is detected
