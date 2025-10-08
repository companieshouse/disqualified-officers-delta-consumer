package uk.gov.companieshouse.disqualifiedofficers.delta.mapper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.FileCopyUtils;

import uk.gov.companieshouse.api.delta.DisqualificationDelta;
import uk.gov.companieshouse.api.delta.DisqualificationOfficer;
import uk.gov.companieshouse.api.disqualification.Disqualification;
import uk.gov.companieshouse.api.disqualification.DisqualificationLinks;
import uk.gov.companieshouse.api.disqualification.InternalDisqualificationApiInternalData;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.LastVariation;
import uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.PermissionToAct;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        InternalNaturalDisqualificationMapperImpl.class,
        DisqualificationMapperImpl.class,
        PermissionToActMapperImpl.class})
class InternalNaturalDisqualificationMapperTest {

    private ObjectMapper mapper;
    private DisqualificationDelta disqualificationDelta;
    private DisqualificationOfficer disqualificationOfficer;

    @Autowired
    InternalNaturalDisqualificationMapper disqualificationMapper;

    @BeforeEach
    void setUp() throws IOException {
        mapper = new ObjectMapper();

        String path = "disqualified-officers-natural-example.json";
        String input =
                FileCopyUtils.copyToString(new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream(path)));

        disqualificationDelta = mapper.readValue(input, DisqualificationDelta.class);
        disqualificationOfficer = disqualificationDelta.getDisqualifiedOfficer().getFirst();
    }


    @Test
    void shouldMapDisqualificationOfficerToNaturalDisqualification() {
        InternalNaturalDisqualificationApi disqualificationTarget =
        disqualificationMapper.disqualificationDeltaToApi(disqualificationOfficer);
        NaturalDisqualificationApi externalDisqualificationTarget = disqualificationTarget.getExternalData();
        InternalDisqualificationApiInternalData internalDisqualificationTarget = disqualificationTarget
                .getInternalData();

        PermissionToAct permissionToAct = new PermissionToAct();
        List<String> companyNames = new ArrayList<>();
        companyNames.add("123 LTD");
        companyNames.add("321 LTD");
        permissionToAct.setCompanyNames(companyNames);
        permissionToAct.setCourtName("rinder");
        permissionToAct.setExpiresOn(LocalDate.of(2016, 4, 12));
        permissionToAct.setGrantedOn(LocalDate.of(2014, 2, 3));

        assertThat(disqualificationDelta).isNotNull();
        assertThat(disqualificationTarget).isNotNull();
        assertEquals("Dust", externalDisqualificationTarget.getForename());
        assertEquals(LocalDate.of(1976, 2, 6), externalDisqualificationTarget.getDateOfBirth());
        assertEquals("KINDNESSLIQUOR", externalDisqualificationTarget.getSurname());
        assertTrue(externalDisqualificationTarget.getHonours().isEmpty());
        assertEquals("British", externalDisqualificationTarget.getNationality());
        assertEquals("3002560732", internalDisqualificationTarget.getOfficerDetailId());
        assertEquals("3000034602", internalDisqualificationTarget.getOfficerDisqId());
        assertEquals("1kETe9SJWIp9OlvZgO1xmjyt5_s", internalDisqualificationTarget.getOfficerId());
        assertEquals("1234567890", internalDisqualificationTarget.getOfficerIdRaw());
        assertEquals("Mr", externalDisqualificationTarget.getTitle());
        assertEquals("166284060001", externalDisqualificationTarget.getPersonNumber());
        assertEquals("Condition Reserve", externalDisqualificationTarget.getOtherForenames());
        assertEquals(permissionToAct, externalDisqualificationTarget.getPermissionsToAct().getFirst());
        DisqualificationLinks links = new DisqualificationLinks();
        links.setSelf("/disqualified-officers/natural/1kETe9SJWIp9OlvZgO1xmjyt5_s");
        assertEquals(links, externalDisqualificationTarget.getLinks());

        assertOnDisqualification(externalDisqualificationTarget.getDisqualifications().getFirst());
    }


    private void assertOnDisqualification(Disqualification disqualification) {
        assertEquals("INV3975227", disqualification.getCaseIdentifier());
        List<String> list = new ArrayList<>();
        list.add("CONSORTIUM TECHNOLOGY LIMITED");
        assertEquals(list, disqualification.getCompanyNames());
        assertNull(disqualification.getCourtName());
        LastVariation lastVar = new LastVariation();
        lastVar.setVariedOn(LocalDate.of(2021, 2, 17));
        lastVar.setCaseIdentifier("1");
        lastVar.setCourtName("Judys");
        assertEquals(lastVar, disqualification.getLastVariation());
        assertEquals("undertaking", disqualification.getDisqualificationType());
        HashMap<String, String> reason = new HashMap<>();
        reason.put("act", "company-directors-disqualification-act-1986");
        reason.put("section", "7");
        reason.put("description_identifier", "order-or-undertaking-and-reporting-provisions");
        assertEquals(reason, disqualification.getReason());
        assertEquals(LocalDate.of(2015, 2, 14), disqualification.getUndertakenOn());
        assertEquals(LocalDate.of(2015, 2, 18), disqualification.getDisqualifiedFrom());
        assertEquals(LocalDate.of(2025, 2, 17), disqualification.getDisqualifiedUntil());
        assertEquals("30", disqualification.getAddress().getPremises());
    }
}