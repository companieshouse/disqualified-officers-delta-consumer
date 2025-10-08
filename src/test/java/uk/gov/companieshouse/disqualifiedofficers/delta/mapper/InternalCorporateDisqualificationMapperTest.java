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
import uk.gov.companieshouse.api.disqualification.InternalCorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.InternalDisqualificationApiInternalData;
import uk.gov.companieshouse.api.disqualification.LastVariation;
import uk.gov.companieshouse.api.disqualification.CorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.PermissionToAct;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        InternalCorporateDisqualificationMapperImpl.class,
        DisqualificationMapperImpl.class,
        PermissionToActMapperImpl.class})
class InternalCorporateDisqualificationMapperTest {

    private ObjectMapper mapper;
    private DisqualificationDelta disqualificationDelta;
    private DisqualificationOfficer disqualificationOfficer;

    @Autowired
    InternalCorporateDisqualificationMapper disqualificationMapper;

    @BeforeEach
    void setUp() throws IOException {
        mapper = new ObjectMapper();

        String path = "disqualified-officers-corporate-example.json";
        String input =
                FileCopyUtils.copyToString(new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream(path)));

        disqualificationDelta = mapper.readValue(input, DisqualificationDelta.class);
        disqualificationOfficer = disqualificationDelta.getDisqualifiedOfficer().getFirst();
    }

    @Test
    void shouldMapDisqualificationOfficerToCorporateDisqualification() {
        InternalCorporateDisqualificationApi disqualificationTarget =
        disqualificationMapper.disqualificationDeltaToApi(disqualificationOfficer);
        CorporateDisqualificationApi externalDisqualificationTarget = disqualificationTarget.getExternalData();
        InternalDisqualificationApiInternalData internalDisqualificationTarget = disqualificationTarget
                .getInternalData();

        PermissionToAct permissionToAct = new PermissionToAct();
        List<String> companyNames = new ArrayList<>();
        companyNames.add("TEST1 LTD");
        companyNames.add("TEST LTD");
        permissionToAct.setCompanyNames(companyNames);
        permissionToAct.setCourtName("CARDIFF");
        permissionToAct.setExpiresOn(LocalDate.of(2015, 2, 23));
        permissionToAct.setGrantedOn(LocalDate.of(2013, 5, 15));

        assertThat(disqualificationDelta).isNotNull();
        assertThat(disqualificationTarget).isNotNull();
        assertEquals("BOOMSHACK LTD.", externalDisqualificationTarget.getName());
        assertNull(internalDisqualificationTarget.getOfficerDetailId());
        assertNull(internalDisqualificationTarget.getOfficerDisqId());
        assertEquals("D7WbjxLxswJPHWaLzilZ98PoaZU", internalDisqualificationTarget.getOfficerId());
        assertEquals("1234554321", internalDisqualificationTarget.getOfficerIdRaw());
        assertNull(externalDisqualificationTarget.getPersonNumber());
        assertEquals(permissionToAct, externalDisqualificationTarget.getPermissionsToAct().getFirst());
        DisqualificationLinks links = new DisqualificationLinks();
        links.setSelf("/disqualified-officers/corporate/D7WbjxLxswJPHWaLzilZ98PoaZU");
        assertEquals(links, externalDisqualificationTarget.getLinks());
        assertEquals("0000000012", externalDisqualificationTarget.getCompanyNumber());
        assertEquals("England", externalDisqualificationTarget.getCountryOfRegistration());

        Disqualification disqualification = externalDisqualificationTarget.getDisqualifications().getFirst();
        assertEquals("IME3707935", disqualification.getCaseIdentifier());
        List<String> list = new ArrayList<>();
        list.add("TEEHEE LIMITED");
        list.add("HOOHAH LIMITED");
        assertEquals(list, disqualification.getCompanyNames());
        assertEquals("UNDERTAKING", disqualification.getCourtName());
        LastVariation lastVar = new LastVariation();
        lastVar.setVariedOn(LocalDate.of(2015, 11, 17));
        lastVar.setCaseIdentifier("VARY DQ01 EFF DATE TO CURRENT");
        lastVar.setCourtName("CHDBALDWIN");
        assertEquals(lastVar, disqualification.getLastVariation());
        assertEquals("court-order", disqualification.getDisqualificationType());
        HashMap<String, String> reason = new HashMap<>();
        reason.put("act", "company-directors-disqualification-northern-ireland-order-2002");
        reason.put("article", "4");
        reason.put("description_identifier", "fraud-etc-in-winding-up");
        assertEquals(reason, disqualification.getReason());
        assertEquals(LocalDate.of(2013, 1, 1), disqualification.getHeardOn());
        assertNull(disqualification.getUndertakenOn());
        assertEquals(LocalDate.of(2013, 6, 24), disqualification.getDisqualifiedFrom());
        assertEquals(LocalDate.of(2018, 6, 23), disqualification.getDisqualifiedUntil());
        assertEquals("Companies House", disqualification.getAddress().getPremises());
    }
}