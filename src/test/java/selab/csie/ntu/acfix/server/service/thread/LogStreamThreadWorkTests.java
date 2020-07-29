package selab.csie.ntu.acfix.server.service.thread;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.util.FileSystemUtils;
import selab.csie.ntu.acfix.server.config.GlobalConfig;
import selab.csie.ntu.acfix.server.service.WebSocketService;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

class LogStreamThreadWorkTests {

    private WebSocketService service;
    private GlobalConfig config;
    private static List<String> mockLog;
    private static String liveID = "LIVE_ID";
    private static String deadID = "DEAD_ID";

    @BeforeAll
    static void mockData() {
        File dataDir = new File("./data");
        if (!dataDir.exists() && !dataDir.mkdir())
            throw new RuntimeException("Failed to create path: ./data");
        try {
            File src = new File(Objects.requireNonNull(LogStreamThreadWorkTests.class.getClassLoader().getResource("0")).getFile());
            File dst = new File("./data/0");
            FileSystemUtils.copyRecursively(src, dst);
            mockLog = Files.readAllLines(new File(src, "0.log").toPath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy mock file to path: ./data/0");
        }
    }

    @BeforeEach
    void setup() {
        service = Mockito.mock(WebSocketService.class);
        config = Mockito.mock(GlobalConfig.class);
        BDDMockito.given(service.socketAlive(liveID)).willReturn(true);
        BDDMockito.given(service.socketAlive(deadID)).willReturn(false);
        BDDMockito.willDoNothing().given(service).sendACFixLog(anyString(), anyString());
        BDDMockito.willDoNothing().given(service).sendWebSocketTerminate(anyString());
        BDDMockito.given(config.getVolumePath()).willReturn("./data");
    }

    /* Test if log file not exists */
    @Test
    void testRunLogFileNotExists() {
        new LogStreamThreadWork(-1, liveID, service, config).run();
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(service, Mockito.times(1)).sendACFixLog(eq(liveID), captor.capture());
        Mockito.verify(service, Mockito.times(1)).sendWebSocketTerminate(eq(liveID));
        Assertions.assertEquals("Log file does not exist.", captor.getValue());
    }

    /* Test if socketID is not alive */
    @Test
    void testRunSocketIDNotAlive() {
        new LogStreamThreadWork(0, deadID, service, config).run();
        Mockito.verify(service, Mockito.times(0)).sendACFixLog(anyString(), anyString());
        Mockito.verify(service, Mockito.times(0)).sendWebSocketTerminate(anyString());
    }

    /* Test in regular case */
    @Test
    void testRunCorrectMessageSent() {
        new LogStreamThreadWork(0, liveID, service, config).run();
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(service, Mockito.times(mockLog.size())).sendACFixLog(eq(liveID), captor.capture());
        Mockito.verify(service, Mockito.times(1)).sendWebSocketTerminate(eq(liveID));
        Assertions.assertEquals(mockLog, captor.getAllValues());
    }

    @AfterAll
    static void mockRemove() {
        FileSystemUtils.deleteRecursively(new File("./data/0"));
    }

}
