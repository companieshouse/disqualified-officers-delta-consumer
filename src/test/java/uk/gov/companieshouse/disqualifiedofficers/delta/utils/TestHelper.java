package uk.gov.companieshouse.disqualifiedofficers.delta.utils;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.delta.DisqualificationDelta;
import uk.gov.companieshouse.api.delta.DisqualificationOfficer;
import uk.gov.companieshouse.api.disqualification.*;
import uk.gov.companieshouse.api.delta.DisqualificationAddress;
import uk.gov.companieshouse.delta.ChsDelta;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.Objects;

import static org.springframework.kafka.support.KafkaHeaders.EXCEPTION_CAUSE_FQCN;

public class TestHelper {

    public Message<ChsDelta> createChsDeltaMessage(boolean isDelete) throws IOException {
        String filename = isDelete ? "disqualified-officers-delete-example.json":"disqualified-officers-delta-example.json";
        InputStreamReader exampleJsonPayload = new InputStreamReader(
                Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResourceAsStream(filename)));
        String data = FileCopyUtils.copyToString(exampleJsonPayload);

        return buildMessage(data);
    }

    public InternalNaturalDisqualificationApi createDisqualificationApi() {
        InternalNaturalDisqualificationApi internalNaturalDisqualificationApi = new InternalNaturalDisqualificationApi();
        InternalDisqualificationApiInternalData internalData = new InternalDisqualificationApiInternalData();
        internalData.setOfficerIdRaw("3002276133");
        internalData.setOfficerId("3002276133");
        internalData.setOfficerDisqId("3000035941");

        Address address = new Address();
        address.setPremises("39");
        address.setAddressLine1("Arnold Gardens");
        address.setLocality("London");
        address.setPostalCode("N13 5JE");

        Disqualification disqualification = new Disqualification();
        disqualification.setDisqualifiedFrom(LocalDate.of(2015,8,13));
        disqualification.setDisqualifiedUntil(LocalDate.of(2030,8,12));
        disqualification.setDisqualificationType("ORDER");
        disqualification.setHeardOn(LocalDate.of(2015,7,23));
        disqualification.setCaseIdentifier("1396 OF 2015");
        disqualification.setCourtName("Companies Court - London");
        disqualification.addCompanyNamesItem("EARNSHAW EQUITIES LIMITED");
        disqualification.setAddress(address);

        NaturalDisqualificationApi externalData = new NaturalDisqualificationApi();
        externalData.setPersonNumber("168544120001");
        externalData.setDateOfBirth(LocalDate.of(1977, 7, 18));
        externalData.setForename("Jason");
        externalData.setOtherForenames("John");
        externalData.setSurname("PISTOLAS");
        externalData.setNationality("British");
        externalData.addDisqualificationsItem(disqualification);

        internalNaturalDisqualificationApi.setInternalData(internalData);
        internalNaturalDisqualificationApi.setExternalData(externalData);
        return internalNaturalDisqualificationApi;
    }

    public DisqualificationDelta createDisqualificationDelta() {
        DisqualificationAddress address = new DisqualificationAddress();
        address.setPremise("39");
        address.setAddressLine1("Arnold Gardens");
        address.setLocality("London");
        address.setPostalCode("N13 5JE");

        uk.gov.companieshouse.api.delta.Disqualification disqualification = new uk.gov.companieshouse.api.delta.Disqualification();
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

    public ProducerRecord<String, Object> createRecord(String topic, String header) {
        Object recordObj = new Object();
        RecordHeaders headers = new RecordHeaders();
        headers.add(EXCEPTION_CAUSE_FQCN, header.getBytes());

        return new ProducerRecord<>(topic, 1,1L ,null, recordObj, headers);
    }

    public Message<ChsDelta> createInvalidChsDeltaMessage() {
        return buildMessage("This is some invalid data");
    }

    public Message<ChsDelta> createBrokenChsDeltaMessage() throws IOException {
        InputStreamReader exampleJsonPayload = new InputStreamReader(
                Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResourceAsStream("invalid-disqualified-officers-delta-example.json")));
        String data = FileCopyUtils.copyToString(exampleJsonPayload);
        return buildMessage(data);
    }

    public Message<ChsDelta> createDeleteMessage() throws IOException {
        InputStreamReader exampleJsonPayload = new InputStreamReader(
                Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResourceAsStream("disqualified-officers-delete-example.json")));
        String data = FileCopyUtils.copyToString(exampleJsonPayload);

        return buildMessage(data);
    }

    private Message<ChsDelta> buildMessage(String data) {
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
}
