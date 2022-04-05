package uk.gov.companieshouse.disqualifiedofficers.delta.mapper;

import java.util.HashMap;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import uk.gov.companieshouse.api.delta.DisqualificationOfficer;
import uk.gov.companieshouse.api.disqualification.InternalDisqualificationApiInternalData;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi;

@Mapper(componentModel = "spring", uses = {DisqualificationMapper.class, 
        PermissionToActMapper.class})
public interface InternalNaturalDisqualificationMapper {

    @Mapping(target = "internalData.officerDisqId", source = "officerDisqId")
    @Mapping(target = "internalData.officerDetailId", source = "officerDetailId")
    @Mapping(target = "internalData.officerIdRaw", source = "officerId")
    @Mapping(target = "externalData.dateOfBirth", source = "dateOfBirth", dateFormat = "yyyyMMdd")
    @Mapping(target = "externalData.personNumber", source = "externalNumber")
    @Mapping(target = "externalData.forename", source = "forename")
    @Mapping(target = "externalData.honours", source = "honours")
    @Mapping(target = "externalData.nationality", source = "nationality")
    @Mapping(target = "externalData.surname", source = "surname")
    @Mapping(target = "externalData.title", source = "title")
    @Mapping(target = "externalData.otherForenames", source = "middleName")
    @Mapping(target = "externalData.disqualifications", source = "disqualifications")
    @Mapping(target = "externalData.permissionsToAct", source = "exemptions")
    InternalNaturalDisqualificationApi disqualificationDeltaToApi(
            DisqualificationOfficer disqualificationOfficer);

    /**
    * Invoked at the end of the auto-generated mapping methods.
    * @param target        the target object
    * @param sourceCase    the source object
    */
    @AfterMapping
    default void mapLinksAndId(@MappingTarget InternalNaturalDisqualificationApi target,
                                  DisqualificationOfficer sourceCase) {
        
        String encodedOfficerId = MapperUtils.encode(sourceCase.getOfficerId());
        String link = String.format("/disqualifiedofficer/natural/%s", encodedOfficerId);
        NaturalDisqualificationApi externalTarget = target.getExternalData();
        InternalDisqualificationApiInternalData internalData = target.getInternalData();
        HashMap<String, String> links = new HashMap<>();
        links.put("self", link);
        externalTarget.setLinks(links);
        internalData.setOfficerId(encodedOfficerId);
    }

}