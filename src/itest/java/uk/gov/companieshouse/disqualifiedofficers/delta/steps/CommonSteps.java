package uk.gov.companieshouse.disqualifiedofficers.delta.steps;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.disqualifiedofficers.delta.data.TestData;
import uk.gov.companieshouse.disqualifiedofficers.delta.matcher.DisqualificationRequestMatcher;
import uk.gov.companieshouse.logging.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class CommonSteps {

    @Value("${disqualified-officers.delta.topic}")
    private String mainTopic;

    @Value("${wiremock.server.port}")
    private String port;

    private static WireMockServer wireMockServer;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    public KafkaConsumer<String, Object> kafkaConsumer;
    @Autowired
    private Logger logger;

    private String type;
    private String output;

    @Given("the application is running")
    public void theApplicationRunning() {
        assertThat(kafkaTemplate).isNotNull();
    }

    @When("^the consumer receives a (natural|corporate) disqualification of (undertaking|court order)$")
    public void theConsumerReceivesDisqualificationOfType(String officerType, String disqType) throws Exception {
        configureWiremock();
        stubPutDisqualification(officerType);
        this.type = officerType;
        this.output = TestData.getOutputData(officerType, disqType);

        ChsDelta delta = new ChsDelta(TestData.getInputData(officerType, disqType), 1, "1", false);
        kafkaTemplate.send(mainTopic, delta);

        countDown();
    }

    @When("an invalid avro message is sent")
    public void invalidAvroMessageIsSent() throws Exception {
        kafkaTemplate.send(mainTopic, "InvalidData");

        countDown();
    }

    @When("a message with invalid data is sent")
    public void messageWithInvalidDataIsSent() throws Exception {
        ChsDelta delta = new ChsDelta("InvalidData", 1, "1", false);
        kafkaTemplate.send(mainTopic, delta);

        countDown();
    }

    @When("^the consumer receives a message but the data api returns a (\\d*)$")
    public void theConsumerReceivesMessageButDataApiReturns(int responseCode) throws Exception{
        configureWiremock();
        stubPutDisqualification("natural", responseCode);

        ChsDelta delta = new ChsDelta(
                TestData.getInputData("natural", "undertaking"), 1, "1", false);
        kafkaTemplate.send(mainTopic, delta);

        countDown();
    }

    @When("the consumer receives a message that causes an error")
    public void theConsumerReceivesMessageThatCausesAnError() throws Exception {
        ChsDelta delta = new ChsDelta(
                TestData.getInputData("natural", "error"), 1, "1", false);
        kafkaTemplate.send(mainTopic, delta);

        countDown();
    }

    @Then("a PUT request is sent to the disqualifications api with the transformed data")
    public void putRequestIsSentToTheDisqualificationsApi() {
        verify(1, requestMadeFor(new DisqualificationRequestMatcher(logger, type, output)));
    }

    @Then("^the message should be moved to topic (.*)$")
    public void theMessageShouldBeMovedToTopic(String topic) {
        ConsumerRecord<String, Object> singleRecord = KafkaTestUtils.getSingleRecord(kafkaConsumer, topic);

        assertThat(singleRecord.value()).isNotNull();
    }

    @Then("^the message should retry (\\d*) times and then error$")
    public void theMessageShouldRetryAndError(int retries) {
        ConsumerRecords<String, Object> records = KafkaTestUtils.getRecords(kafkaConsumer);
        Iterable<ConsumerRecord<String, Object>> retryRecords =  records.records("disqualified-officers-delta-retry");
        Iterable<ConsumerRecord<String, Object>> errorRecords =  records.records("disqualified-officers-delta-error");

        int actualRetries = (int) StreamSupport.stream(retryRecords.spliterator(), false).count();
        int errors = (int) StreamSupport.stream(errorRecords.spliterator(), false).count();

        assertThat(actualRetries).isEqualTo(retries);
        assertThat(errors).isEqualTo(1);
    }

    @When("the consumer receives a delete payload")
    public void theConsumerReceivesDelete() throws Exception {
        configureWiremock();
        stubDeleteDisqualification(200);
        ChsDelta delta = new ChsDelta(TestData.getDeleteData(), 1, "1", true);
        kafkaTemplate.send(mainTopic, delta);

        countDown();
    }

    @When("the consumer receives an invalid delete payload")
    public void theConsumerReceivesInvalidDelete() throws Exception {
        configureWiremock();
        ChsDelta delta = new ChsDelta("invalid", 1, "1", true);
        kafkaTemplate.send(mainTopic, delta);

        countDown();
    }

    @When("^the consumer receives a delete message but the data api returns a (\\d*)$")
    public void theConsumerReceivesDeleteMessageButDataApiReturns(int responseCode) throws Exception{
        configureWiremock();
        stubDeleteDisqualification(responseCode);
        ChsDelta delta = new ChsDelta(TestData.getDeleteData(), 1, "1", true);
        kafkaTemplate.send(mainTopic, delta);

        countDown();
    }

    @Then("a DELETE request is sent to the disqualifications api with the encoded Id")
    public void deleteRequestIsSent() {
        verify(1, deleteRequestedFor(urlMatching(
                "/disqualified-officers/delete/1kETe9SJWIp9OlvZgO1xmjyt5_s/internal")));
    }

    @After
    public void shutdownWiremock(){
        if (wireMockServer != null)
            wireMockServer.stop();
    }

    private void configureWiremock() {
        wireMockServer = new WireMockServer(Integer.parseInt(port));
        wireMockServer.start();
        configureFor("localhost", Integer.parseInt(port));
    }

    private void stubPutDisqualification(String type) {
        stubPutDisqualification(type, 200);
    }

    private void stubPutDisqualification(String type, int responseCode) {
        stubFor(put(urlEqualTo("/disqualified-officers/" + type + "/1kETe9SJWIp9OlvZgO1xmjyt5_s/internal"))
                .willReturn(aResponse().withStatus(responseCode)));
    }

    private void stubDeleteDisqualification(int responseCode) {
        stubFor(delete(urlEqualTo("/disqualified-officers/delete/1kETe9SJWIp9OlvZgO1xmjyt5_s/internal"))
                .willReturn(aResponse().withStatus(responseCode)));
    }

    private void countDown() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(5, TimeUnit.SECONDS);
    }
}

