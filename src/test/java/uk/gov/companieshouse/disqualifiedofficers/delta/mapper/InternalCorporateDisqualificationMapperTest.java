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
    public void setUp() throws IOException {
        mapper = new ObjectMapper();

        String path = "disqualified-officers-corporate-example.json";
        String input =
                FileCopyUtils.copyToString(new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream(path)));

        disqualificationDelta = mapper.readValue(input, DisqualificationDelta.class);
        disqualificationOfficer = disqualificationDelta.getDisqualifiedOfficer().get(0);
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
        permissionToAct.setExpiresOn(LocalDate.of(2015, 02, 23));
        permissionToAct.setGrantedOn(LocalDate.of(2013, 05, 15));

        assertThat(disqualificationDelta).isNotNull();
        assertThat(disqualificationTarget).isNotNull();
        assertEquals(externalDisqualificationTarget.getName(), "BOOMSHACK LTD.");
        assertEquals(internalDisqualificationTarget.getOfficerDetailId(), null);
        assertEquals(internalDisqualificationTarget.getOfficerDisqId(), null);
        assertEquals(internalDisqualificationTarget.getOfficerId(), "D7WbjxLxswJPHWaLzilZ98PoaZU");
        assertEquals(internalDisqualificationTarget.getOfficerIdRaw(), "1234554321");
        assertEquals(externalDisqualificationTarget.getPersonNumber(), null);
        assertEquals(externalDisqualificationTarget.getPermissionsToAct().get(0), permissionToAct);
        DisqualificationLinks links = new DisqualificationLinks();
        links.setSelf("/disqualified-officers/corporate/D7WbjxLxswJPHWaLzilZ98PoaZU");
        assertEquals(externalDisqualificationTarget.getLinks(), links);
        assertEquals(externalDisqualificationTarget.getCompanyNumber(), "0000000012");
        assertEquals(externalDisqualificationTarget.getCountryOfRegistration(), "England");

        Disqualification disqualification = externalDisqualificationTarget.getDisqualifications().get(0);
        assertEquals(disqualification.getCaseIdentifier(), "IME3707935");
        List<String> list = new ArrayList<>();
        list.add("TEEHEE LIMITED");
        list.add("HOOHAH LIMITED");
        assertEquals(disqualification.getCompanyNames(), list);
        assertEquals(disqualification.getCourtName(), "UNDERTAKING");
        LastVariation var = new LastVariation();
        var.setVariedOn(LocalDate.of(2015, 11, 17));
        var.setCaseIdentifier("VARY DQ01 EFF DATE TO CURRENT");
        var.setCourtName("CHDBALDWIN");
        assertEquals(var, disqualification.getLastVariation());
        assertEquals(disqualification.getDisqualificationType(), "court-order");
        HashMap<String, String> reason = new HashMap<>();
        reason.put("act", "company-directors-disqualification-northern-ireland-order-2002");
        reason.put("article", "4");
        reason.put("description_identifier", "fraud-etc-in-winding-up");
        assertEquals(disqualification.getReason(), reason);
        assertEquals(disqualification.getHeardOn(), LocalDate.of(2013, 01, 01));
        assertNull(disqualification.getUndertakenOn());
        assertEquals(disqualification.getDisqualifiedFrom(), LocalDate.of(2013, 06, 24));
        assertEquals(disqualification.getDisqualifiedUntil(), LocalDate.of(2018, 06, 23));
        assertEquals(disqualification.getAddress().getPremises(), "Companies House");
    }
}