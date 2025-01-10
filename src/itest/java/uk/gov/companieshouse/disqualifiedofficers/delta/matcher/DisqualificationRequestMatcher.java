package uk.gov.companieshouse.disqualifiedofficers.delta.matcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.ValueMatcher;

/**
 *  Custom matcher class used to match requests made by the consumer to the
 *  data api. The url, request type and request body are compared.
 */
public class DisqualificationRequestMatcher implements ValueMatcher<Request> {

    private final String expectedOutput;
    private final String type;

    public DisqualificationRequestMatcher(String type, String output) {
        this.type = type;
        this.expectedOutput = output;
    }

    @Override
    public MatchResult match(Request value) {

        return MatchResult.aggregate(matchUrl(value.getUrl()), matchMethod(value.getMethod()),
                matchBody(value.getBodyAsString()));
    }

    private MatchResult matchUrl(String actualUrl) {
        String expectedUrl = "/disqualified-officers/" + type + "/1kETe9SJWIp9OlvZgO1xmjyt5_s/internal";

        MatchResult urlResult = MatchResult.of(expectedUrl.equals(actualUrl));

        if (! urlResult.isExactMatch()) {
            System.out.println("url does not match expected: <" + expectedUrl + "> actual: <" + actualUrl + ">");
        }

        return urlResult;
    }

    private MatchResult matchMethod(RequestMethod actualMethod) {
        RequestMethod expectedMethod = RequestMethod.PUT;

        MatchResult typeResult = MatchResult.of(expectedMethod.equals(actualMethod));

        if (! typeResult.isExactMatch()) {
            System.out.println("Method does not match expected: <" + expectedMethod + "> actual: <" + actualMethod + ">");
        }

        return typeResult;
    }

    private MatchResult matchBody(String actualBody) {

        ObjectMapper mapper = new ObjectMapper();

        MatchResult bodyResult;
        JsonNode expectedBody;
        try {
            expectedBody = mapper.readTree(expectedOutput);
        } catch (JsonProcessingException e) {
            System.out.println("Could not process expectedBody JSON: " + e);
            return MatchResult.of(false);
        }

        JsonNode actual;
        try {
            actual = mapper.readTree(actualBody);
        } catch (JsonProcessingException e) {
            System.out.println("Could not process actualBody JSON: " + e);
            return MatchResult.of(false);
        }

        bodyResult = MatchResult.of(expectedBody.equals(actual));

        if (! bodyResult.isExactMatch()) {
            System.out.println("Body does not match expected: <" + expectedBody + "> actual: <" + actualBody + ">");
        }

        return bodyResult;
    }
}

