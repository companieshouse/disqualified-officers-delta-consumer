package uk.gov.companieshouse.disqualifiedofficers.delta.mapper;

import java.util.Base64;
import java.util.HashMap;

import org.apache.commons.codec.digest.DigestUtils;

public final class MapperUtils {

    // make the class noninstantiable
    private MapperUtils() {
    }

    /**
     * encode the officerId for use in links and id.
     */
    public static String encode(String officerId) {
        String salt = "my2_4s!gdDxC4$n9";
        return Base64.getUrlEncoder().withoutPadding().encodeToString(
                DigestUtils.sha1(officerId + salt));
    }
    
    /**
     * Create a map of values to lookup using description identifier.
     * @return the hashmap of description identifiers to descriptions
     */
    public static HashMap<String, String> createIdentifierHashMap() {

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
        descriptionIdentifier.put("S3A", "director-disqualification-sanctions");

        return descriptionIdentifier;
    }
}
