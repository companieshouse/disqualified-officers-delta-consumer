package uk.gov.companieshouse.disqualifiedofficers.delta.data;

import org.springframework.util.FileCopyUtils;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestData {

    public static String getInputData(String officerType, String disqType) {
        disqType = disqType.replace(' ', '_');
        String path = "src/itest/resources/data/" + officerType + '_' + disqType + "_in.json";
        return readFile(path);
    }

    public static String getOutputData(String officerType, String disqType) {
        disqType = disqType.replace(' ', '_');
        String path = "src/itest/resources/data/" + officerType + '_' + disqType + "_out.json";
        return readFile(path).replaceAll("\n", "");
    }

    public static String getDeleteData() {
        String path = "src/itest/resources/data/delete_disqualification.json";
        return readFile(path).replaceAll("\n", "");
    }

    private static String readFile(String path) {
        String data;
        try {
            data = FileCopyUtils.copyToString(new InputStreamReader(new FileInputStream(path)));
        } catch (IOException e) {
            data = null;
        }
        return data;
    }
}
