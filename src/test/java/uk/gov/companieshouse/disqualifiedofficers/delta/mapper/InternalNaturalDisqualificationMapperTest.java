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
    public void setUp() throws IOException {
        mapper = new ObjectMapper();

        String path = "disqualified-officers-natural-example.json";
        String input =
                FileCopyUtils.copyToString(new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream(path)));

        disqualificationDelta = mapper.readValue(input, DisqualificationDelta.class);
        disqualificationOfficer = disqualificationDelta.getDisqualifiedOfficer().get(0);
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
        permissionToAct.setExpiresOn(LocalDate.of(2016, 04, 12));
        permissionToAct.setGrantedOn(LocalDate.of(2014, 02, 03));

        assertThat(disqualificationDelta).isNotNull();
        assertThat(disqualificationTarget).isNotNull();
        assertEquals(externalDisqualificationTarget.getForename(), "Dust");
        assertEquals(externalDisqualificationTarget.getDateOfBirth(), LocalDate.of(1976, 02, 06));
        assertEquals(externalDisqualificationTarget.getSurname(), "KINDNESSLIQUOR");
        assertTrue(externalDisqualificationTarget.getHonours().isEmpty());
        assertEquals(externalDisqualificationTarget.getNationality(), "British");
        assertEquals(internalDisqualificationTarget.getOfficerDetailId(), "3002560732");
        assertEquals(internalDisqualificationTarget.getOfficerDisqId(), "3000034602");
        assertEquals(internalDisqualificationTarget.getOfficerId(), "1kETe9SJWIp9OlvZgO1xmjyt5_s");
        assertEquals(internalDisqualificationTarget.getOfficerIdRaw(), "1234567890");
        assertEquals(externalDisqualificationTarget.getTitle(), "Mr");
        assertEquals(externalDisqualificationTarget.getPersonNumber(), "166284060001");
        assertEquals(externalDisqualificationTarget.getOtherForenames(), "Condition Reserve");
        assertEquals(externalDisqualificationTarget.getPermissionsToAct().get(0), permissionToAct);
        DisqualificationLinks links = new DisqualificationLinks();
        links.setSelf("/disqualified-officers/natural/1kETe9SJWIp9OlvZgO1xmjyt5_s");
        assertEquals(externalDisqualificationTarget.getLinks(), links);

        Disqualification disqualification = externalDisqualificationTarget.getDisqualifications().get(0);
        assertEquals(disqualification.getCaseIdentifier(), "INV3975227");
        List<String> list = new ArrayList<>();
        list.add("CONSORTIUM TECHNOLOGY LIMITED");
        assertEquals(disqualification.getCompanyNames(), list);
        assertNull(disqualification.getCourtName());
        LastVariation var = new LastVariation();
        var.setVariedOn(LocalDate.of(2021, 02, 17));
        var.setCaseIdentifier("1");
        var.setCourtName("Judys");
        assertEquals(var, disqualification.getLastVariation().get(0));
        assertEquals(disqualification.getDisqualificationType(), "undertaking");
        HashMap<String, String> reason = new HashMap<>();
        reason.put("act", "company-directors-disqualification-act-1986");
        reason.put("section", "7");
        reason.put("description_identifier", "order-or-undertaking-and-reporting-provisions");
        assertEquals(disqualification.getReason(), reason);
        assertEquals(disqualification.getUndertakenOn(), LocalDate.of(2015, 02, 14));
        assertEquals(disqualification.getDisqualifiedFrom(), LocalDate.of(2015, 02, 18));
        assertEquals(disqualification.getDisqualifiedUntil(), LocalDate.of(2025, 02, 17));
    }
}