Feature: Delete

  Scenario: send DELETE request to data Api
    Given the application is running
    When the consumer receives a delete payload
    Then a DELETE request is sent to the disqualifications api with the encoded Id