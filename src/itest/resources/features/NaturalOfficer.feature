Feature: Natural Officer

Scenario Outline: Can transform and send a natural officer
  Given the application is running
  When the consumer receives a natural disqualification of <type>
  Then a PUT request is sent to the disqualifications api with the transformed data
  Examples:
  | type        |
  | undertaking |