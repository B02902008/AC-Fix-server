package selab.csie.ntu.acfix.server.service.thread;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;

class DockerClientTests {

    private DockerClient docker;
    private Runtime runtime;
    private Process process;

    @BeforeEach
    void setup() throws IOException {
        runtime = Mockito.mock(Runtime.class);
        process = Mockito.mock(Process.class);
        docker = new DockerClient(runtime);
        BDDMockito.given(runtime.exec(any(String[].class))).willReturn(process);
    }

    @Test
    void testCreateSuccess() throws IOException {
        BDDMockito.given(process.getInputStream()).willReturn(new ByteArrayInputStream("{\"Id\":\"123\"}".getBytes()));
        assertThat(docker.createContainer(new JSONObject().put("Image", "image"))).isTrue();
        assertThat(docker.containerId).isEqualTo("123");
    }

    @Test
    void testCreateFailed() throws IOException {
        BDDMockito.given(process.getInputStream()).willReturn(new ByteArrayInputStream("{\"Key\":\"Value\"}".getBytes()));
        assertThat(docker.createContainer(new JSONObject().put("Image", "image"))).isFalse();
    }

    @Test
    void testStart() throws IOException {
        docker.containerId = "123";
        docker.startContainer();
        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        Mockito.verify(runtime, Mockito.times(1)).exec(captor.capture());
        assertThat(captor.getValue()).hasSize(3);
        assertThat(captor.getValue()[2]).isEqualTo("curl -X POST --unix-socket /var/run/docker.sock http://localhost/containers/123/start");
    }

}
