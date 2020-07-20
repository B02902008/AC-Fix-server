package selab.csie.ntu.autofix.server.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import selab.csie.ntu.autofix.server.model.FixingRecord;
import selab.csie.ntu.autofix.server.model.message.AutoFixInvokeMessage;
import selab.csie.ntu.autofix.server.service.CmakeAutoFixService;
import selab.csie.ntu.autofix.server.service.GradleAutoFixService;
import selab.csie.ntu.autofix.server.service.PipAutoFixService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

class AutoFixControllerTests {

    private MockMvc mockMvc;
    private CmakeAutoFixService cmakeService;
    private GradleAutoFixService gradleService;
    private PipAutoFixService pipService;

    @BeforeEach
    void setup() {
        cmakeService = Mockito.mock(CmakeAutoFixService.class);
        gradleService = Mockito.mock(GradleAutoFixService.class);
        pipService = Mockito.mock(PipAutoFixService.class);
        AutoFixController controller = new AutoFixController(cmakeService, gradleService, pipService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Nested
    class InvokeAutoFix {

        private String api;
        private String mockJson;
        private String mockJsonEmpty;
        private String mockJsonRejected;
        private String mockJsonUnmatched;

        @BeforeEach
        void setup() {
            api = "/autofix/{tool}";
            mockJson = "{\"socketID\":\"SOCKET_ID\",\"url\":\"https://ntu.edu.tw\"}";
            mockJsonEmpty = "{\"socketID\":\"SOCKET_ID\",\"url\":\"\"}";
            mockJsonRejected = "{\"socketID\":\"SOCKET_ID\",\"url\":\"https://google.com\"}";
            mockJsonUnmatched = "{\"socketID\":\"SOCKET_ID\",\"url\":\"https://github.com\"}";
            BDDMockito.given(gradleService.generateNewRecord(anyString())).willReturn(new FixingRecord());
            BDDMockito.given(cmakeService.generateNewRecord("https://github.com")).willThrow(new IllegalArgumentException());
            BDDMockito.given(gradleService.invokeAutoFix(any(AutoFixInvokeMessage.class), any(FixingRecord.class)))
                    .willReturn(1);
            BDDMockito.given(gradleService.invokeAutoFix(argThat(msg -> msg.getUrl().equals("https://google.com")), any(FixingRecord.class)))
                    .willThrow(new RejectedExecutionException());
        }

        /* Test code 200 for API: Invoke autofix service */
        @Test
        void testAPICodeOk() throws Exception {
            mockMvc.perform(post(api, "gradle").contentType(MediaType.APPLICATION_JSON).content(mockJson))
                    .andExpect(status().isOk());
        }

        /* Test return value for API: Invoke autofix service */
        @Test
        void testAPICorrectReturn() throws Exception {
            mockMvc.perform(post(api, "gradle").contentType(MediaType.APPLICATION_JSON).content(mockJson))
                    .andExpect(content().string("1"));
        }

        /* Test code 401 for API: Invoke autofix service (empty or null url) */
        @Test
        void testAPICodeBadRequestWithEmptyUrl() throws Exception {
            ResultActions result = mockMvc.perform(post(api, "gradle").contentType(MediaType.APPLICATION_JSON).content(mockJsonEmpty));
            result.andExpect(status().isBadRequest());
            Throwable throwable = result.andReturn().getResolvedException();
            Assertions.assertTrue(throwable != null && throwable.getMessage().contains("non-empty"));
        }

        /* Test code 401 for API: Invoke autofix service (regex unmatched url) */
        @Test
        void testAPICodeBadRequestWithUnmatchedUrl() throws Exception {
            ResultActions result = mockMvc.perform(post(api, "cmake").contentType(MediaType.APPLICATION_JSON).content(mockJsonUnmatched));
            result.andExpect(status().isBadRequest());
            Throwable throwable = result.andReturn().getResolvedException();
            Assertions.assertTrue(throwable != null && throwable.getMessage().contains("valid"));
        }

        /* Test code 401 for API: Invoke autofix service (unknown service) */
        @Test
        void testAPICodeBadRequestWithUnknownService() throws Exception {
            ResultActions result = mockMvc.perform(post(api, "UNKNOWN").contentType(MediaType.APPLICATION_JSON).content(mockJson));
            result.andExpect(status().isBadRequest());
            Throwable throwable = result.andReturn().getResolvedException();
            Assertions.assertTrue(throwable != null && throwable.getMessage().contains("service"));
        }

        /* Test code 405 for API: Invoke autofix service */
        @Test
        void testAPICodeMethodNotAllowed() throws Exception {
            mockMvc.perform(get(api, "gradle").contentType(MediaType.APPLICATION_JSON).content(mockJson))
                    .andExpect(status().isMethodNotAllowed());
            mockMvc.perform(patch(api, "gradle").contentType(MediaType.APPLICATION_JSON).content(mockJson))
                    .andExpect(status().isMethodNotAllowed());
            mockMvc.perform(delete(api, "gradle").contentType(MediaType.APPLICATION_JSON).content(mockJson))
                    .andExpect(status().isMethodNotAllowed());
        }

        /* Test code 503 for API: Invoke autofix service */
        @Test
        void testAPICodeServiceUnavailable() throws Exception {
            mockMvc.perform(post(api, "gradle").contentType(MediaType.APPLICATION_JSON).content(mockJsonRejected))
                    .andExpect(status().isServiceUnavailable());
        }

    }

    @Nested
    class GetServiceLoading {

        private String api;

        @BeforeEach
        void setup() {
            api = "/autofix/loading/{tool}";
            Map<String, Integer> map = new HashMap<>();
            map.put("core", 10);
            map.put("load", 10);
            BDDMockito.given(pipService.getLoading()).willReturn(map);
        }

        /* Test code 200 for API: Get service loading */
        @Test
        void testAPICodeOk() throws Exception {
            mockMvc.perform(get(api, "pip"))
                    .andExpect(status().isOk());
        }

        /* Test return value for API: Get service loading */
        @Test
        void testAPICorrectReturn() throws Exception {
            mockMvc.perform(get(api, "pip"))
                    .andExpect(jsonPath("$.core", Matchers.any(Integer.class)))
                    .andExpect(jsonPath("$.load", Matchers.any(Integer.class)));
        }

        /* Test code 401 for API: Invoke autofix service (unknown service) */
        @Test
        void testAPICodeBadRequestWithUnknownService() throws Exception {
            mockMvc.perform(get(api, "UNKNOWN"))
                    .andExpect(status().isBadRequest());
        }

        /* Test code 405 for API: Invoke autofix service */
        @Test
        void testAPICodeMethodNotAllowed() throws Exception {
            mockMvc.perform(post(api, "pip"))
                    .andExpect(status().isMethodNotAllowed());
            mockMvc.perform(patch(api, "pip"))
                    .andExpect(status().isMethodNotAllowed());
            mockMvc.perform(delete(api, "pip"))
                    .andExpect(status().isMethodNotAllowed());
        }

    }

}
