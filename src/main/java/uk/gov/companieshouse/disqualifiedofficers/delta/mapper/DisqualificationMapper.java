package uk.gov.companieshouse.disqualifiedofficers.delta.mapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import uk.gov.companieshouse.api.delta.Disqualification;
import uk.gov.companieshouse.api.disqualification.LastVariation;

@Mapper(componentModel = "spring")
public interface DisqualificationMapper {

    @Mapping(target = "caseIdentifier", source = "courtRef")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "companyNames", source = "companyNames")
    @Mapping(target = "courtName", source = "courtName", ignore = true)
    @Mapping(target = "disqualificationType", source = "disqType", ignore = true)
    @Mapping(target = "disqualifiedFrom", source = "disqEffDate", dateFormat = "yyyyMMdd")
    @Mapping(target = "disqualifiedUntil", source = "disqEndDate", dateFormat = "yyyyMMdd")
    @Mapping(target = "heardOn", source = "hearingDate", ignore = true)
    @Mapping(target = "undertakenOn", source = "hearingDate", ignore = true)
    @Mapping(target = "lastVariation", source = "variationCourt", ignore = true)
    @Mapping(target = "reason", source = "sectionOfTheAct", ignore = true)
    uk.gov.companieshouse.api.disqualification.Disqualification map(
            Disqualification disqualification);

    /**
    * Invoked at the end of the auto-generated mapping methods.
    *
    * @param target     the target disqualification
    * @param sourceDisq    the source disqualification
    */
    @AfterMapping
    default void parseLastVariation(
                @MappingTarget uk.gov.companieshouse.api.disqualification.Disqualification target,
                Disqualification sourceDisq) {
        if (sourceDisq.getVarInstrumentStartDate() == null
                || sourceDisq.getVarInstrumentStartDate().equals("")) {
            return;
        }
        LastVariation lastVariation = new LastVariation(); 
        DateTimeFormatter formater = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate varDate = LocalDate.parse(sourceDisq.getVarInstrumentStartDate(), formater);
        lastVariation.setVariedOn(varDate);
        lastVariation.setCaseIdentifier(sourceDisq.getVariationCourtRefNo());
        lastVariation.setCourtName(sourceDisq.getVariationCourt());
        
        List<LastVariation> lastVarList = new ArrayList<>();
        lastVarList.add(lastVariation);
        target.setLastVariation(lastVarList);
    }

    /**
    * Invoked at the end of the auto-generated mapping methods.
    *
    * @param target     the target disqualification
    * @param sourceDisq    the source disqualification
    */
    @AfterMapping
    default void parseReason(
                @MappingTarget uk.gov.companieshouse.api.disqualification.Disqualification target,
                Disqualification sourceDisq) {

        HashMap<String, String> disqualifyingLaw = new HashMap<>();
        disqualifyingLaw.put("CDDA", "company-directors-disqualification-act-1986");
        disqualifyingLaw.put("CDDO",
                "company-directors-disqualification-northern-ireland-order-2002");

        HashMap<String, String> descriptionIdentifier = new HashMap<>();
        descriptionIdentifier.put("A5", "conviction-of-offence-punishable-on-"
                + "indictment-or-conviction-on-indictment-or-summary-conviction");
        descriptionIdentifier.put("A6", "persistent-default-under-companies-legislation");
        descriptionIdentifier.put("A7", "fraud-etc-in-winding-up");
        descriptionIdentifier.put("A8", "summary-conviction-of-offence");
        descriptionIdentifier.put("A8A", "certain-convictions-abroad");
        descriptionIdentifier.put("A9", 
                "high-court-to-disqualify-unfit-directors-of-insolvent-companies");
        descriptionIdentifier.put("A10", "order-or-undertaking-and-reporting-provisions");
        descriptionIdentifier.put("A11", 
                "investigation-of-company");
        descriptionIdentifier.put("A11A", "order-disqualifying-a-"
                + "person-instructing-an-unfit-director-of-an-insolvent-company");
        descriptionIdentifier.put("A11C", "undertaking-instead-of-order-under-article-11a");
        descriptionIdentifier.put("A11D", 
                "order-disqualifying-a-person-instructing-an-unfit-director");
        descriptionIdentifier.put("A11E", "undertaking-instead-of-order-under-article-11d");
        descriptionIdentifier.put("A13A", 
                "competition-and-markets-authority-disqualification-order");
        descriptionIdentifier.put("A13B", 
                "competition-and-markets-authority-disqualification-undertaking");
        descriptionIdentifier.put("A14", "participation-in-wrongful-trading");
        descriptionIdentifier.put("S2", "conviction-of-indictable-offence");
        descriptionIdentifier.put("S3", "persistent-breaches-of-companies-legislation");
        descriptionIdentifier.put("S4", "fraud-etc-in-winding-up");
        descriptionIdentifier.put("S5", "summary-conviction");
        descriptionIdentifier.put("S5A", "certain-convictions-abroad");
        descriptionIdentifier.put("S6", 
                "court-to-disqualify-unfit-directors-of-insolvent-companies");
        descriptionIdentifier.put("S7", "order-or-undertaking-and-reporting-provisions");
        descriptionIdentifier.put("S8", "investigation-of-company");
        descriptionIdentifier.put("S8ZA", "order-disqualifying-a-person"
                + "-instructing-an-unfit-director-of-an-insolvent-company");
        descriptionIdentifier.put("S8ZC", "undertaking-disqualifying-a-person"
                + "-instructing-an-unfit-director-of-an-insolvent-company");
        descriptionIdentifier.put("S8ZD", 
                "order-disqualifying-a-person-instructing-an-unfit-director");
        descriptionIdentifier.put("S8ZE", 
                "undertaking-disqualifying-a-person-instructing-an-unfit-director");
        descriptionIdentifier.put("S9", "matters-determining-unfitness-of-directors");
        descriptionIdentifier.put("S9A", 
                "competition-and-markets-authority-disqualification-order");
        descriptionIdentifier.put("S9B", 
                "competition-and-markets-authority-disqualification-undertaking");
        descriptionIdentifier.put("S10", "participation-in-wrongful-trading");

        String[] sectionParts = sourceDisq.getSectionOfTheAct().split(" ");
        HashMap<String, String> reason = new HashMap<>();
        reason.put("act", disqualifyingLaw.get(sectionParts[0]));
        reason.put("description_identifier", descriptionIdentifier.get(sectionParts[2]));

        String disqualificationReference = sectionParts[0].equals("CDDA") ? "section" : "article";

        reason.put(disqualificationReference, sectionParts[2].substring(1));

        target.setReason(reason);
    }

    /**
    * Invoked at the end of the auto-generated mapping methods.
    *
    * @param target     the target disqualification
    * @param sourceDisq    the source disqualification
    */
    @AfterMapping
    default void parseDisqType(
                @MappingTarget uk.gov.companieshouse.api.disqualification.Disqualification target,
                Disqualification sourceDisq) {
        HashMap<String, String> disqualificationType = new HashMap<>();
        disqualificationType.put("ORDER", "court-order");
        disqualificationType.put("UNDERTAKING", "undertaking");
        target.setDisqualificationType(disqualificationType.get(sourceDisq.getDisqType()));
    }

    /**
    * Invoked at the end of the auto-generated mapping methods.
    *
    * @param target     the target disqualification
    * @param sourceDisq    the source disqualification
    */
    @AfterMapping
    default void parseHearingDetails(
                @MappingTarget uk.gov.companieshouse.api.disqualification.Disqualification target,
                Disqualification sourceDisq) {
        DateTimeFormatter formater = DateTimeFormatter.ofPattern("yyyyMMdd");
        if (sourceDisq.getDisqType().equals("ORDER")) {
            target.setCourtName(sourceDisq.getCourtName());
            target.setHeardOn(LocalDate.parse(sourceDisq.getHearingDate(), formater));
        } else {
            target.setUndertakenOn(LocalDate.parse(sourceDisq.getHearingDate(), formater));
        }
    }
}
