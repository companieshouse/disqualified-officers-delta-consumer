Feature: Corporate Officer

Scenario Outline: Can transform and send a corporate officer of <type>
  Given the application is running
  When the consumer receives a corporate disqualification of <type>
  Then a PUT request is sent to the disqualifications api with the transformed data
  Examples:
  | type        |
  | undertaking |
  | court order |