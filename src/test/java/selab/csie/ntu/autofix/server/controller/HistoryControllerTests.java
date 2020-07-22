package selab.csie.ntu.autofix.server.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.BDDMockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import selab.csie.ntu.autofix.server.model.FixingRecord;
import selab.csie.ntu.autofix.server.util.FixingRecordBuilder;
import selab.csie.ntu.autofix.server.service.FixingRecordService;
import selab.csie.ntu.autofix.server.service.HistoryService;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;

class HistoryControllerTests {

    private MockMvc mockMvc;
    private FixingRecordService recordService;
    private HistoryService historyService;

    @BeforeEach
    void setup() {
        recordService = Mockito.mock(FixingRecordService.class);
        historyService = Mockito.mock(HistoryService.class);
        HistoryController controller = new HistoryController(recordService, historyService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Nested
    class AllHistory {

        private String api;
        private String apiDefault;

        @BeforeEach
        void setup() {
            api = "/history/page/{page_num}";
            apiDefault = "/history/";
            List<FixingRecord> records = new ArrayList<>();
            Random random = new Random();
            for (int i = 0; i < FixingRecordService.PER_PAGE; i ++) {
                records.add(new FixingRecordBuilder().setId(i)
                        .setStat((random.nextBoolean() ? 1 : -1) * random.nextInt(2))
                        .setStart(new Date(1500000000000L + random.nextInt(100000) * 1000))
                        .setEnd(new Date(1500000000000L + random.nextInt(100000) * 1000))
                        .build());
            }
            BDDMockito.given(recordService.getFixingRecords(ArgumentMatchers.any(Pageable.class)))
                    .willReturn(new PageImpl<>(records));
        }

        /* Test code 200 for API: Get history list with page */
        @Test
        void testAPICodeOk() throws Exception {
            mockMvc.perform(get(api, 1))
                    .andExpect(status().isOk());
        }

        /* Test code 200 for API: Get history list default */
        @Test
        void testAPICodeOkDefault() throws Exception {
            mockMvc.perform(get(apiDefault))
                    .andExpect(status().isOk());
        }

        /* Test return format and pagination value for API: Get history list with page (with query parameters) */
        @Test
        void testAPICorrectReturn() throws Exception {
            mockMvc.perform(get(api, 10).queryParam("direction", "asc").queryParam("sorting", "end"))
                    .andExpect(jsonPath("$.content", hasSize(10)))
                    .andExpect(jsonPath("$.totalElements", Matchers.any(Integer.class)))
                    .andExpect(jsonPath("$.totalPages", Matchers.any(Integer.class)))
                    .andExpect(jsonPath("$.size", Matchers.any(Integer.class)))
                    .andExpect(jsonPath("$.number", Matchers.any(Integer.class)));
            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            PageRequest expected = PageRequest.of(9, FixingRecordService.PER_PAGE, Sort.Direction.ASC, "end", "id");
            Mockito.verify(recordService).getFixingRecords(captor.capture());
            Assertions.assertEquals(expected, captor.getValue());
        }

        /* Test return format and pagination value for API: Get history list with page (without query parameters) */
        @Test
        void testAPICorrectReturnWithDefaultValue() throws Exception {
            mockMvc.perform(get(api, 1))
                    .andExpect(jsonPath("$.content", hasSize(10)))
                    .andExpect(jsonPath("$.totalElements", Matchers.any(Integer.class)))
                    .andExpect(jsonPath("$.totalPages", Matchers.any(Integer.class)))
                    .andExpect(jsonPath("$.size", Matchers.any(Integer.class)))
                    .andExpect(jsonPath("$.number", Matchers.any(Integer.class)));
            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            PageRequest expected = PageRequest.of(0, FixingRecordService.PER_PAGE, Sort.Direction.DESC, "id");
            Mockito.verify(recordService).getFixingRecords(captor.capture());
            Assertions.assertEquals(expected, captor.getValue());
        }

        /* Test return format and pagination value for API: Get history list default */
        @Test
        void testAPICorrectReturnDefault() throws Exception {
            mockMvc.perform(get(apiDefault))
                    .andExpect(jsonPath("$.content", hasSize(10)))
                    .andExpect(jsonPath("$.totalElements", Matchers.any(Integer.class)))
                    .andExpect(jsonPath("$.totalPages", Matchers.any(Integer.class)))
                    .andExpect(jsonPath("$.size", Matchers.any(Integer.class)))
                    .andExpect(jsonPath("$.number", Matchers.any(Integer.class)));
            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            PageRequest expected = PageRequest.of(0, FixingRecordService.PER_PAGE, Sort.Direction.DESC, "id");
            Mockito.verify(recordService).getFixingRecords(captor.capture());
            Assertions.assertEquals(expected, captor.getValue());
        }

        /* Test code 401 for API: Get history list with page */
        @Test
        void testAPICodeBadRequest() throws Exception {
            mockMvc.perform(get(api, "UNIT_TEST"))
                    .andExpect(status().isBadRequest());
        }

        /* Test code 405 for API: Get history list with page */
        @Test
        void testAPICodeMethodNotAllowed() throws Exception {
            mockMvc.perform(post(api, 1))
                    .andExpect(status().isMethodNotAllowed());
            mockMvc.perform(patch(api, 1))
                    .andExpect(status().isMethodNotAllowed());
            mockMvc.perform(delete(api, 1))
                    .andExpect(status().isMethodNotAllowed());
        }

        /* Test code 405 for API: Get history list default */
        @Test
        void testAPICodeMethodNotAllowedDefault() throws Exception {
            mockMvc.perform(post(apiDefault))
                    .andExpect(status().isMethodNotAllowed());
            mockMvc.perform(patch(apiDefault))
                    .andExpect(status().isMethodNotAllowed());
            mockMvc.perform(delete(apiDefault))
                    .andExpect(status().isMethodNotAllowed());
        }

    }

    @Nested
    class GetHistory {

        private String api;

        @BeforeEach
        void setup() throws FileNotFoundException {
            api = "/history/{id}";
            FixingRecord record = new FixingRecordBuilder()
                    .setId(7122)
                    .setStat(1)
                    .setName("Mock-Project-7122")
                    .setLang("Java")
                    .setTool("Gradle")
                    .setStart(new Date(1553467650000L))
                    .setEnd(new Date(1557653460000L))
                    .build();
            BDDMockito.given(recordService.getFixingRecord(7122))
                    .willReturn(record);
            BDDMockito.given(recordService.getFixingRecord(9999))
                    .willThrow(new FileNotFoundException());
        }

        /* Test code 200 for API: Get history with id */
        @Test
        void testAPICodeOk() throws Exception {
            mockMvc.perform(get(api, 7122))
                    .andExpect(status().isOk());
        }

        /* Test return value for API: Get history with id */
        @Test
        void testAPICorrectReturn() throws Exception {
            mockMvc.perform(get(api, 7122))
                    .andExpect(jsonPath("id", is(7122)))
                    .andExpect(jsonPath("stat", is(1)))
                    .andExpect(jsonPath("name", is("Mock-Project-7122")))
                    .andExpect(jsonPath("lang", is("Java")))
                    .andExpect(jsonPath("tool", is("Gradle")));
        }

        /* Test code 401 for API: Get history with id */
        @Test
        void testAPICodeBadRequest() throws Exception {
            mockMvc.perform(get(api, "UNIT_TEST"))
                    .andExpect(status().isBadRequest());
        }

        /* Test code 404 for API: Get history with id */
        @Test
        void testAPICodeNotFound() throws Exception {
            mockMvc.perform(get(api, 9999))
                    .andExpect(status().isNotFound());
        }

        /* Test code 405 for API: Get history with id */
        @Test
        void testAPICodeMethodNotAllowed() throws Exception {
            mockMvc.perform(post(api, 7122))
                    .andExpect(status().isMethodNotAllowed());
            mockMvc.perform(patch(api, 7122))
                    .andExpect(status().isMethodNotAllowed());
            mockMvc.perform(delete(api, 7122))
                    .andExpect(status().isMethodNotAllowed());
        }

    }

    @Nested
    class GetFixingProduct {

        private String api;

        @BeforeEach
        void setup() throws FileNotFoundException {
            api = "/history/product/{id}";
            BDDMockito.given(historyService.retrieveFixingProduct(7122))
                    .willReturn("./build.gradle");
            BDDMockito.given(historyService.retrieveFixingProduct(9999))
                    .willThrow(new FileNotFoundException());
        }

        /* Test code 200 for API: Get/Head fixed product */
        @Test
        void testAPICodeOk() throws Exception {
            mockMvc.perform(get(api, 7122))
                    .andExpect(status().isOk());
            mockMvc.perform(head(api, 7122))
                    .andExpect(status().isOk());
        }

        /* Test return header for API: Get/Head fixed product */
        @Test
        void testAPICorrectReturn() throws Exception {
            mockMvc.perform(head(api, 7122))
                    .andExpect(header().string("Content-Type", "application/octet-stream"))
                    .andExpect(header().string("Content-Disposition", "attachment; filename=\"build.gradle\""))
                    .andExpect(header().exists("Content-Length"));
        }

        /* Test code 401 for API: Get/Head fixed product */
        @Test
        void testAPICodeBadRequest() throws Exception {
            mockMvc.perform(head(api, "UNIT_TEST"))
                    .andExpect(status().isBadRequest());
        }

        /* Test code 404 for API: Get/Head fixed product */
        @Test
        void testAPICodeNotFound() throws Exception {
            mockMvc.perform(head(api, 9999))
                    .andExpect(status().isNotFound());
        }

        /* Test code 405 for API: Get/Head fixed product */
        @Test
        void testAPICodeMethodNotAllowed() throws Exception {
            mockMvc.perform(post(api, 7122))
                    .andExpect(status().isMethodNotAllowed());
            mockMvc.perform(patch(api, 7122))
                    .andExpect(status().isMethodNotAllowed());
            mockMvc.perform(delete(api, 7122))
                    .andExpect(status().isMethodNotAllowed());
        }

    }

    @Nested
    class InvokeLogStream {

        private String api;
        private String mockJson;

        @BeforeEach
        void setup() {
            api = "/history/stream/{id}";
            mockJson = "{\"socketID\":\"SOCKET_ID\"}";
            BDDMockito.willDoNothing().given(historyService).invokeLogStream(7122, "SOCKET_ID");
            BDDMockito.willThrow(new IllegalArgumentException()).given(historyService).invokeLogStream(7122, null);
            BDDMockito.willThrow(new RejectedExecutionException()).given(historyService).invokeLogStream(9999, "SOCKET_ID");
        }

        /* Test code 200 for API: Invoke log stream */
        @Test
        void testAPICodeOK() throws Exception {
            mockMvc.perform(post(api, 7122).contentType(MediaType.APPLICATION_JSON).content(mockJson))
                    .andExpect(status().isOk());
        }

        /* Test code 401 for API: Invoke log stream */
        @Test
        void testAPICodeBadRequest() throws Exception {
            mockMvc.perform(post(api, "UNIT_TEST").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
            mockMvc.perform(post(api, 7122).contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isBadRequest());
        }

        /* Test code 405 for API: Invoke log stream */
        @Test
        void testAPICodeMethodNotAllowed() throws Exception {
            mockMvc.perform(get(api, 7122).contentType(MediaType.APPLICATION_JSON).content(mockJson))
                    .andExpect(status().isMethodNotAllowed());
            mockMvc.perform(patch(api, 7122).contentType(MediaType.APPLICATION_JSON).content(mockJson))
                    .andExpect(status().isMethodNotAllowed());
            mockMvc.perform(delete(api, 7122).contentType(MediaType.APPLICATION_JSON).content(mockJson))
                    .andExpect(status().isMethodNotAllowed());
        }

        /* Test code 503 for API: Invoke log stream */
        @Test
        void testAPICodeServiceUnavailable() throws Exception {
            mockMvc.perform(post(api, 9999).contentType(MediaType.APPLICATION_JSON).content(mockJson))
                    .andExpect(status().isServiceUnavailable());
        }

    }

}
