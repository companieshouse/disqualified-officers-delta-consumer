package uk.gov.companieshouse.disqualifiedofficers.delta.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.delta.Disqualification;
import uk.gov.companieshouse.api.delta.DisqualificationAddress;
import uk.gov.companieshouse.api.delta.DisqualificationDelta;
import uk.gov.companieshouse.api.delta.DisqualificationOfficer;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.disqualifiedofficers.delta.producer.DisqualifiedOfficersDeltaProducer;
import uk.gov.companieshouse.disqualifiedofficers.delta.transformer.DisqualifiedOfficersApiTransformer;
import uk.gov.companieshouse.logging.Logger;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DisqualifiedOfficersProcessorTest {

    private DisqualifiedOfficersDeltaProcessor deltaProcessor;

    @Mock
    private DisqualifiedOfficersDeltaProducer disqualifiedOfficersDeltaProducer;
    @Mock
    private DisqualifiedOfficersApiTransformer transformer;
    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        deltaProcessor = new DisqualifiedOfficersDeltaProcessor(
                disqualifiedOfficersDeltaProducer,
                transformer,
                logger
        );
    }

    @Test
    @DisplayName("Transforms a kafka message containing a ChsDelta payload into a DisqualificationDelta")
    void When_ValidChsDeltaMessage_Expect_ValidDisqualificationDeltaMapping() throws IOException {
        Message<ChsDelta> mockChsDeltaMessage = createChsDeltaMessage();
        DisqualificationDelta expectedDelta = createDisqualificationDelta();
        when(transformer.transformNaturalDisqualification(expectedDelta)).thenCallRealMethod();

        deltaProcessor.processDelta(mockChsDeltaMessage);

        verify(transformer).transformNaturalDisqualification(expectedDelta);
    }

    private Message<ChsDelta> createChsDeltaMessage() throws IOException {
        InputStreamReader exampleJsonPayload = new InputStreamReader(
                ClassLoader.getSystemClassLoader().getResourceAsStream("disqualified-officers-delta-example.json"));
        String data = FileCopyUtils.copyToString(exampleJsonPayload);

        ChsDelta mockChsDelta = ChsDelta.newBuilder()
                .setData(data)
                .setContextId("context_id")
                .setAttempt(1)
                .build();

        return MessageBuilder
                .withPayload(mockChsDelta)
                .setHeader(KafkaHeaders.RECEIVED_TOPIC, "test")
                .setHeader("DISQUALIFIED_OFFICERS_DELTA_RETRY_COUNT", 1)
                .build();
    }

    private DisqualificationDelta createDisqualificationDelta() {
        DisqualificationAddress address = new DisqualificationAddress();
        address.setPremise("39");
        address.setAddressLine1("Arnold Gardens");
        address.setLocality("London");
        address.setPostalCode("N13 5JE");

        Disqualification disqualification = new Disqualification();
        disqualification.setDisqEffDate("20150813");
        disqualification.setDisqEndDate("20300812");
        disqualification.setDisqType("ORDER");
        disqualification.setHearingDate("20150723");
        disqualification.setSectionOfTheAct("CDDA 1986 S6");
        disqualification.setCourtRef("1396 OF 2015");
        disqualification.setCourtName("Companies Court - London");
        disqualification.addCompanyNamesItem("EARNSHAW EQUITIES LIMITED");
        disqualification.setAddress(address);

        DisqualificationOfficer disqualifiedOfficer = new DisqualificationOfficer();
        disqualifiedOfficer.setOfficerDisqId("3000035941");
        disqualifiedOfficer.setOfficerDetailId("3002842206");
        disqualifiedOfficer.setOfficerId("3002276133");
        disqualifiedOfficer.setExternalNumber("168544120001");
        disqualifiedOfficer.setDateOfBirth("19770718");
        disqualifiedOfficer.setForename("Jason");
        disqualifiedOfficer.setMiddleName("John");
        disqualifiedOfficer.setSurname("PISTOLAS");
        disqualifiedOfficer.setNationality("British");
        disqualifiedOfficer.addDisqualificationsItem(disqualification);

        return new DisqualificationDelta().addDisqualifiedOfficerItem(disqualifiedOfficer);
    }
}