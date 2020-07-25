package selab.csie.ntu.autofix.server.service.thread;

import lombok.SneakyThrows;
import selab.csie.ntu.autofix.server.model.message.AutoFixInvokeMessage;
import selab.csie.ntu.autofix.server.service.FixingRecordService;
import selab.csie.ntu.autofix.server.service.WebSocketService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoFixThreadWork implements Runnable {

    private Integer id;
    private String dockerImage;
    private AutoFixInvokeMessage message;
    private Runtime runtime;
    private FixingRecordService recordService;
    private WebSocketService socketService;

    private static final String AUTOFIX_RESULT_DIRECTORY = "./data";
    private static final String AUTOFIX_CONTAINER_VOLUME = "/home/autofix/result";


    public AutoFixThreadWork(Integer id, String dockerImage, AutoFixInvokeMessage message, Runtime runtime,
                             FixingRecordService recordService, WebSocketService socketService) {
        this.id = id;
        this.dockerImage = dockerImage;
        this.message = message;
        this.runtime = runtime;
        this.recordService = recordService;
        this.socketService = socketService;
    }

    @SneakyThrows
    public void run() {
        if (!ensureDockerVolume()) {
            recordService.removeRecord(id);
            socketService.sendWebSocketTerminate(message.getSocketID());
            return;
        }
        runtime.exec(new String[] { "/bin/sh", "-c", constructDockerCmd() });
        Pattern pattern = Pattern.compile("\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}]\\[(.{5})] (.+)");
        InputStreamReader stream = new InputStreamReader(
                new FileInputStream(new File(AUTOFIX_RESULT_DIRECTORY, String.format("%d/%d.log", id, id))),
                StandardCharsets.UTF_8
        );
        boolean result = true;
        boolean finished = false;
        while (!finished) {
            String line = AutoFixThreadWork.readLineFromStream(stream);
            socketService.sendAutoFixLog(message.getSocketID(), line);
            Matcher matcher = pattern.matcher(line);
            if (!matcher.matches())
                continue;
            switch (matcher.group(1)) {
                case "START":
                    socketService.sendAutoFixStage(message.getSocketID(), matcher.group(2));
                    break;
                case "STAGE":
                    result &= !matcher.group(2).endsWith("Failed");
                    socketService.sendAutoFixStage(message.getSocketID(), matcher.group(2));
                    break;
                case "FINAL":
                    recordService.updateRecord(id, result);
                    socketService.sendAutoFixStage(message.getSocketID(), matcher.group(2));
                    socketService.sendWebSocketTerminate(message.getSocketID());
                    finished = true;
                    break;
                default:
                    break;
            }
        }
        stream.close();
    }

    private boolean ensureDockerVolume() throws IOException {
        File resultRoot = new File(AUTOFIX_RESULT_DIRECTORY);
        File resultDir  = new File(resultRoot, id.toString());
        File resultFile = new File(resultRoot, String.format("%d/%d.log", id, id));
        if (!resultRoot.exists() && !resultRoot.mkdir())
            return false;
        if (resultDir.exists() || !resultDir.mkdir())
            return false;
        if (resultFile.exists() || !resultFile.createNewFile())
            return false;
        return giveAllPermission(resultDir) && giveAllPermission(resultFile);
    }

    private boolean giveAllPermission(File path) {
        return path.setExecutable(true, false)
                && path.setReadable(true, false)
                && path.setWritable(true, false);
    }

    private String constructDockerCmd() {
        return new DockerRunCmdBuilder(dockerImage)
                .remove()
                .background()
                .addEnv("BUILD_INDEX", String.valueOf(id))
                .addEnv("BUILD_TARGET", message.getUrl())
                .addVolume(String.format("%s/%d", AUTOFIX_RESULT_DIRECTORY, id))
                .build();
    }

    static String readLineFromStream(InputStreamReader stream) throws IOException, InterruptedException {
        StringBuilder sb = new StringBuilder();
        while ( true ) {
            if ( stream.ready() ) {
                char c = (char) stream.read();
                if (c == '\n' || c == '\r')
                    return sb.toString();
                else
                    sb.append(c);
            } else {
                Thread.sleep(1000);
            }
        }
    }

    private static class DockerRunCmdBuilder {
        private boolean removeInTerminate;
        private boolean executeInBackground;
        private List<Map.Entry<String, String>> envList;
        private Map.Entry<String, String> volume;
        private String image;

        DockerRunCmdBuilder(String image) {
            this.removeInTerminate = false;
            this.executeInBackground = false;
            this.envList = new ArrayList<>();
            this.volume = null;
            this.image = image;
        }

        DockerRunCmdBuilder remove() {
            this.removeInTerminate = true;
            return this;
        }

        DockerRunCmdBuilder background() {
            this.executeInBackground = true;
            return this;
        }

        DockerRunCmdBuilder addEnv(String key, String val) {
            this.envList.add(new AbstractMap.SimpleEntry<>(key, val));
            return this;
        }

        DockerRunCmdBuilder addVolume(String host) {
            this.volume = new AbstractMap.SimpleEntry<>(host, AUTOFIX_CONTAINER_VOLUME);
            return this;
        }

        String build() {
            List<String> cmd = new ArrayList<>();
            cmd.add("docker");
            cmd.add("run");
            if (this.removeInTerminate)
                cmd.add("--rm");
            if (this.executeInBackground)
                cmd.add("-d");
            for (Map.Entry<String, String> entry : this.envList)
                cmd.add(String.format("-e \"%s=%s\"", entry.getKey(), entry.getValue()));
            if (this.volume != null)
                cmd.add(String.format("--volume=%s:%s", this.volume.getKey(), this.volume.getValue()));
            cmd.add(this.image);
            return String.join(" ", cmd);
        }
    }
}
