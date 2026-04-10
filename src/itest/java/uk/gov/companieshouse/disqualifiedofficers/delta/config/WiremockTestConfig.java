package uk.gov.companieshouse.disqualifiedofficers.delta.config;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.util.ArrayList;
import java.util.List;

public class WiremockTestConfig {

    private static WireMockServer wireMockServer = null;

    public static void setupWiremock() {
        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(8888); // fixed port matching api.api-url
        }
        if (!wireMockServer.isRunning()) {
            wireMockServer.start();
            configureFor("localhost", 8888);
        } else {
            wireMockServer.resetAll();
        }
    }

    public static int getPort() {
        return wireMockServer != null ? wireMockServer.port() : -1;
    }

    public static void stopWiremock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
            wireMockServer = null;
        }
    }

    public static void stubKafkaApi(Integer responseCode) {
        stubFor(
                post(urlPathMatching("/private/resource-changed"))
                        .willReturn(aResponse()
                                .withStatus(responseCode)
                                .withHeader("Content-Type", "application/json"))
        );
    }

    public static List<ServeEvent> getServeEvents() {
        return wireMockServer != null ? wireMockServer.getAllServeEvents() : new ArrayList<>();
    }
}

