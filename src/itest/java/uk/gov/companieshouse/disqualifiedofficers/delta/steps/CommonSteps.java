package uk.gov.companieshouse.disqualifiedofficers.delta.steps;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.disqualifiedofficers.delta.data.TestData;
import uk.gov.companieshouse.disqualifiedofficers.delta.matcher.DisqualificationRequestMatcher;
import uk.gov.companieshouse.logging.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.requestMadeFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;


public class CommonSteps {

    @Value("${disqualified-officers.delta.topic.main}")
    private String mainTopic;

    @Value("${wiremock.server.port}")
    private String port;

    private static WireMockServer wireMockServer;

    @Autowired
    private KafkaTemplate<String, ChsDelta> kafkaTemplate;
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

        ChsDelta delta = new ChsDelta(TestData.getInputData(officerType, disqType), 1, "1");
        kafkaTemplate.send(mainTopic, delta);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(5, TimeUnit.SECONDS);
    }

    @Then("a PUT request is sent to the disqualifications api with the transformed data")
    public void putRequestIsSentToTheDisqualificationsApi() {
        verify(1, requestMadeFor(new DisqualificationRequestMatcher(logger, type, output)));
    }

    @After
    public void shutdownWiremock(){
        wireMockServer.stop();
    }

    private void configureWiremock() {
        wireMockServer = new WireMockServer(Integer.parseInt(port));
        wireMockServer.start();
        configureFor("localhost", Integer.parseInt(port));
    }

    private void stubPutDisqualification(String type) {
        stubFor(put(urlEqualTo("/disqualified-officers/" + type + "/1234567890/internal"))
                .willReturn(aResponse().withStatus(200)));
    }
}

