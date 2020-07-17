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
    private FixingRecordService fixingRecordService;
    private WebSocketService webSocketService;

    private static final String AUTOFIX_RESULT_DIRECTORY = "./data";
    private static final String AUTOFIX_CONTAINER_VOLUME = "/home/autofix/result";


    public AutoFixThreadWork(Integer id, String dockerImage, AutoFixInvokeMessage message,
                      FixingRecordService fixingRecordService, WebSocketService webSocketService) {
        this.id = id;
        this.dockerImage = dockerImage;
        this.message = message;
        this.fixingRecordService = fixingRecordService;
        this.webSocketService = webSocketService;
    }

    @SneakyThrows
    public void run() {
        // Create path for autofix result
        File resultDir = new File(AUTOFIX_RESULT_DIRECTORY);
        File logDir = new File(AUTOFIX_RESULT_DIRECTORY + String.format("/%d", id));
        File logFile = new File(AUTOFIX_RESULT_DIRECTORY + String.format("/%d/%d.log", id, id));
        if ( ( !resultDir.exists() && !resultDir.mkdir() ) || logDir.exists() || logFile.exists() ) {
            fixingRecordService.removeRecord(id);
            webSocketService.sendWebSocketTerminate(message.getSocketID());
        }
        if ( !createAllPermissionPath(logDir, false) || !createAllPermissionPath(logFile, true) ) {
            fixingRecordService.removeRecord(id);
            webSocketService.sendWebSocketTerminate(message.getSocketID());
        }

        // Build and execute docker command
        DockerRunCmdBuilder cmdBuilder = new DockerRunCmdBuilder(dockerImage);
        String command = cmdBuilder
                .remove()
                .background()
                .addEnv("BUILD_INDEX", String.valueOf(id))
                .addEnv("BUILD_TARGET", message.getUrl())
                .addVolume(logDir.getCanonicalPath())
                .build();
        Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", command });

        // Read log file and send to websocket
        Pattern pattern = Pattern.compile("\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}]\\[(.{5})] (.+)");
        InputStreamReader stream = new InputStreamReader(new FileInputStream(logFile), StandardCharsets.UTF_8);
        boolean result = true;
        while ( true ) {
            String line = AutoFixThreadWork.readLineFromStream(stream);
            Matcher matcher = pattern.matcher(line);
            if ( matcher.matches() ) {
                webSocketService.sendAutoFixLog(message.getSocketID(), line);
                if ( matcher.group(1).equals("START") ) {
                    webSocketService.sendAutoFixStage(message.getSocketID(), matcher.group(2));
                } else if ( matcher.group(1).equals("STAGE") ) {
                    result &= !matcher.group(2).substring(matcher.group(2).lastIndexOf(":") + 2).equals("Failed");
                    webSocketService.sendAutoFixStage(message.getSocketID(), matcher.group(2));
                } else if ( matcher.group(1).equals("FINAL") ) {
                    fixingRecordService.updateRecord(id, result);
                    webSocketService.sendAutoFixStage(message.getSocketID(), matcher.group(2));
                    webSocketService.sendWebSocketTerminate(message.getSocketID());
                    break;
                }
            }
        }
        stream.close();
    }

    private boolean createAllPermissionPath(File path, boolean isFile) throws IOException {
        boolean res = isFile ? path.createNewFile() : path.mkdir();
        res &= path.setExecutable(true, false);
        res &= path.setReadable(true, false);
        res &= path.setWritable(true, false);
        return res;
    }

    public static String readLineFromStream(InputStreamReader stream) throws IOException, InterruptedException {
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
