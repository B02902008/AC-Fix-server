package selab.csie.ntu.autofix.server.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import selab.csie.ntu.autofix.server.model.FixingRecord;
import selab.csie.ntu.autofix.server.service.FixingRecordService;
import selab.csie.ntu.autofix.server.util.FixingRecordBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

class DashboardControllerTests {

    private MockMvc mockMvc;
    private FixingRecordService service;

    @BeforeEach
    void setup() {
        service = Mockito.mock(FixingRecordService.class);
        DashboardController controller = new DashboardController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Nested
    class CurrentQueue {

        private String api;

        @BeforeEach
        void setup() {
            api = "/dashboard/current";
            List<FixingRecord> mockRecords = new ArrayList<>();
            Random random = new Random();
            for (int i = 0; i < 10; i ++) {
                mockRecords.add(new FixingRecordBuilder().setId(i)
                        .setStat(0)
                        .setStart(new Date(1500000000000L + random.nextInt(100000) * 1000))
                        .build());
            }
            BDDMockito.given(service.getCurrentFixings()).willReturn(mockRecords);
        }

        /* Test code 200 for API: Get current queue */
        @Test
        void testAPICodeOk() throws Exception {
            mockMvc.perform(get(api)).andExpect(status().isOk());
        }

        /* Test return format and pagination value for API: Get current queue */
        @Test
        void testAPICorrectReturn() throws Exception {
            mockMvc.perform(get(api)).andExpect(jsonPath("$", hasSize(10)));
        }

        /* Test code 405 for API: Get current queue */
        @Test
        void testAPICodeMethodNotAllowed() throws Exception {
            mockMvc.perform(post(api)).andExpect(status().isMethodNotAllowed());
            mockMvc.perform(patch(api)).andExpect(status().isMethodNotAllowed());
            mockMvc.perform(delete(api)).andExpect(status().isMethodNotAllowed());
        }

    }

    @Nested
    class RecentResult {

        private String api;

        @BeforeEach
        void setup() {
            api = "/dashboard/recent";
            List<FixingRecord> mockRecords = new ArrayList<>();
            Random random = new Random();
            for (int i = 0; i < 10; i ++) {
                mockRecords.add(new FixingRecordBuilder().setId(i)
                        .setStat((random.nextBoolean() ? 1 : -1))
                        .setStart(new Date(1500000000000L + random.nextInt(100000) * 1000))
                        .setEnd(new Date(1500000000000L + random.nextInt(100000) * 1000))
                        .build());
            }
            BDDMockito.given(service.getRecentResults()).willReturn(mockRecords);
        }

        /* Test code 200 for API: Get recent result */
        @Test
        void testAPICodeOk() throws Exception {
            mockMvc.perform(get(api)).andExpect(status().isOk());
        }

        /* Test return format and pagination value for API: Get recent result */
        @Test
        void testAPICorrectReturn() throws Exception {
            mockMvc.perform(get(api)).andExpect(jsonPath("$", hasSize(10)));
        }

        /* Test code 405 for API: Get recent result */
        @Test
        void testAPICodeMethodNotAllowed() throws Exception {
            mockMvc.perform(post(api)).andExpect(status().isMethodNotAllowed());
            mockMvc.perform(patch(api)).andExpect(status().isMethodNotAllowed());
            mockMvc.perform(delete(api)).andExpect(status().isMethodNotAllowed());
        }

    }

}
