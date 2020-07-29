package selab.csie.ntu.acfix.server.service.thread;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;

public class DockerClient {

    private Runtime runtime;
    String containerId;

    public DockerClient(Runtime runtime) {
        this.runtime = runtime;
    }

    boolean createContainer(JSONObject config) throws IOException {
        String cmd = String.join(" ", Arrays.asList(
                "curl",
                "-X", "POST",
                "--unix-socket", "/var/run/docker.sock",
                "-H", "\"Content-Type: application/json\"",
                "-d", String.format("\'%s\'", config.toString()),
                "http://localhost/containers/create"));
        try (Reader reader = new InputStreamReader(runtime.exec(new String[] { "/bin/sh", "-c", cmd }).getInputStream())) {
            StringBuilder sb = new StringBuilder();
            int count = 0;
            while (true) {
                if (reader.ready()) {
                    char c = (char) reader.read();
                    sb.append(c);
                    if (c == '{')
                        ++ count;
                    else if (c == '}' && (-- count) == 0)
                        break;
                }
            }
            JSONObject json = new JSONObject(sb.toString());
            containerId = json.has("Id") ? json.getString("Id") : null;
            return json.has("Id");
        }
    }

    void startContainer() throws IOException {
        String cmd = String.join(" ", Arrays.asList(
                "curl",
                "-X", "POST",
                "--unix-socket", "/var/run/docker.sock",
                String.format("http://localhost/containers/%s/start", containerId)));
        runtime.exec(new String[] { "/bin/sh", "-c", cmd });
    }

}
