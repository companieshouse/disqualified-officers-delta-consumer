Feature: Delete

  Scenario: send DELETE request to data Api
    Given the application is running
    When the consumer receives a delete payload
    Then a DELETE request is sent to the disqualifications api with the encoded Id

  Scenario: send DELETE with invalid JSON
    Given the application is running
    When the consumer receives an invalid delete payload
    Then the message should be moved to topic disqualified-officers-delta-invalid

  Scenario: send DELETE with 400 from data api
    Given the application is running
    When the consumer receives a delete message but the data api returns a 400
    Then the message should be moved to topic disqualified-officers-delta-invalid

  Scenario Outline: send DELETE with retryable response from data api
    Given the application is running
    When the consumer receives a delete message but the data api returns a <code>
    Then the message should retry 3 times and then error
    Examples:
    | code |
    | 404  |
    | 503  |