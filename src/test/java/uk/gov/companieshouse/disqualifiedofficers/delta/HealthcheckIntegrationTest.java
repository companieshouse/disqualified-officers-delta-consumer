package uk.gov.companieshouse.disqualifiedofficers.delta;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class HealthcheckIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Successfully returns health status")
    public void returnsHealthStatusSuccessfully() throws Exception {
        mockMvc.perform(get("/disqualified-officers-delta-consumer/healthcheck")).andExpect(status().isOk());
    }

}
