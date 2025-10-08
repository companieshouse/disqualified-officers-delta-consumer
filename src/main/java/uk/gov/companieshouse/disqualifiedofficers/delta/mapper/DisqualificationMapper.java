package uk.gov.companieshouse.disqualifiedofficers.delta.mapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import uk.gov.companieshouse.api.delta.Disqualification;
import uk.gov.companieshouse.api.disqualification.Address;
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
                || sourceDisq.getVarInstrumentStartDate().isEmpty()) {
            return;
        }
        LastVariation lastVariation = new LastVariation(); 
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate varDate = LocalDate.parse(sourceDisq.getVarInstrumentStartDate(), formatter);
        lastVariation.setVariedOn(varDate);
        lastVariation.setCaseIdentifier(sourceDisq.getVariationCourtRefNo());
        lastVariation.setCourtName(sourceDisq.getVariationCourt());
        
        target.setLastVariation(lastVariation);
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
        disqualifyingLaw.put("SAMLA", "sanctions-anti-money-laundering-act-2018");
        disqualifyingLaw.put("CT-Regs", "sanctions-counter-terrorism-regulations-2019");

        Map<String, String> descriptionIdentifier = MapperUtils.createIdentifierHashMap();

        String[] sectionParts = sourceDisq.getSectionOfTheAct().split(" ");
        HashMap<String, String> reason = new HashMap<>();
        reason.put("act", disqualifyingLaw.get(sectionParts[0]));
        reason.put("description_identifier", descriptionIdentifier.get(sectionParts[2]));

        String disqualificationReference = "article";
        if (sectionParts[0].equals("CDDA") || sectionParts[0].equals("SAMLA") || sectionParts[0].equals("CT-Regs")) {
            disqualificationReference = "section";
        }

        if (sectionParts[0].equals("CT-Regs")){
            reason.put(disqualificationReference, sectionParts[2]);
        } else {
            reason.put(disqualificationReference, sectionParts[2].substring(1));
        }

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
        disqualificationType.put("SANCTION", "sanction");
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        if (sourceDisq.getDisqType().equals("ORDER")) {
            target.setCourtName(sourceDisq.getCourtName());
            target.setHeardOn(LocalDate.parse(sourceDisq.getHearingDate(), formatter));
        } else if (sourceDisq.getDisqType().equals("UNDERTAKING")) {
            target.setUndertakenOn(LocalDate.parse(sourceDisq.getHearingDate(), formatter));
        }
    }

    /**
    * Invoked at the end of the auto-generated mapping methods.
    *
    * @param target     the target disqualification
    * @param sourceDisq    the source disqualification
    */
    @AfterMapping
    default void mapAddressPremiseToPremises(
                @MappingTarget uk.gov.companieshouse.api.disqualification.Disqualification target,
                Disqualification sourceDisq) {
        Address address = target.getAddress();

        address.setPremises(sourceDisq.getAddress().getPremise());
    }
}
