package uk.gov.companieshouse.disqualifiedofficers.delta.mapper;

import java.util.Base64;

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
        String encodedOfficerId = Base64.getUrlEncoder().withoutPadding().encodeToString(
                DigestUtils.sha1(officerId + salt));
        return encodedOfficerId;
    }
    
}
