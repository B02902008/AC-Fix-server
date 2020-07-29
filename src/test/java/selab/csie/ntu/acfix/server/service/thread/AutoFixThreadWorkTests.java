package selab.csie.ntu.acfix.server.service.thread;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.util.FileSystemUtils;
import selab.csie.ntu.acfix.server.config.GlobalConfig;
import selab.csie.ntu.acfix.server.model.message.ACFixInvokeMessage;
import selab.csie.ntu.acfix.server.service.FixingRecordService;
import selab.csie.ntu.acfix.server.service.WebSocketService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AutoFixThreadWorkTests {

    private ACFixInvokeMessage msg;
    private DockerClient docker;
    private GlobalConfig config;
    private FixingRecordService recordService;
    private WebSocketService socketService;

    @BeforeEach
    void setup() throws IOException {
        msg = new ACFixInvokeMessage("SOCKET_ID", "URL");
        docker = Mockito.mock(DockerClient.class);
        config = Mockito.mock(GlobalConfig.class);
        recordService = Mockito.mock(FixingRecordService.class);
        socketService = Mockito.mock(WebSocketService.class);
        BDDMockito.given(docker.createContainer(any(JSONObject.class))).willReturn(true);
        BDDMockito.willAnswer((Answer<Process>) invocation -> writeMockLog()).given(docker).startContainer();
        BDDMockito.given(config.isDockerMode()).willReturn(true);
        BDDMockito.given(config.getVolumeName()).willReturn("VOLUME");
        BDDMockito.given(config.getVolumePath()).willReturn("./data");
        BDDMockito.willDoNothing().given(recordService).removeRecord(anyInt());
        BDDMockito.willDoNothing().given(recordService).updateRecord(anyInt(), anyBoolean());
        BDDMockito.willDoNothing().given(socketService).sendACFixLog(anyString(), anyString());
        BDDMockito.willDoNothing().given(socketService).sendACFixStage(anyString(), anyString());
        BDDMockito.willDoNothing().given(socketService).sendWebSocketTerminate(anyString());
    }

    private Process writeMockLog() throws Exception {
        URL url = AutoFixThreadWorkTests.class.getClassLoader().getResource("0/0.log");
        FileWriter writer = new FileWriter("./data/0/0.log");
        for (String line: Files.readAllLines(Paths.get(Objects.requireNonNull(url).toURI())))
            writer.write(line + "\n");
        writer.close();
        return null;
    }

    @Test
    @Order(1)
    void testVolumeEnsured() {
        new AutoFixThreadWork(0, "DOCKER", msg, docker, recordService, socketService, config).run();
        Mockito.verify(recordService, Mockito.times(0)).removeRecord(0);
        Mockito.verify(recordService, Mockito.times(1)).updateRecord(0, false);
        Mockito.verify(socketService, Mockito.times(16)).sendACFixLog(anyString(), anyString());
        Mockito.verify(socketService, Mockito.times(10)).sendACFixStage(anyString(), anyString());
        Mockito.verify(socketService, Mockito.times(1)).sendWebSocketTerminate(anyString());
    }

    @Test
    @Order(2)
    void testVolumeNotEnsured() {
        assertThat(new File("./data/-1").mkdir()).isTrue();
        new AutoFixThreadWork(-1, "DOCKER", msg, docker, recordService, socketService, config).run();
        Mockito.verify(recordService, Mockito.times(1)).removeRecord(-1);
        Mockito.verify(recordService, Mockito.times(0)).updateRecord(anyInt(), anyBoolean());
        Mockito.verify(socketService, Mockito.times(0)).sendACFixLog(anyString(), anyString());
        Mockito.verify(socketService, Mockito.times(0)).sendACFixStage(anyString(), anyString());
        Mockito.verify(socketService, Mockito.times(1)).sendWebSocketTerminate(anyString());
    }

    @AfterEach
    void removeGenerated() {
        FileSystemUtils.deleteRecursively(new File("./data/0"));
        FileSystemUtils.deleteRecursively(new File("./data/-1"));
    }

}
