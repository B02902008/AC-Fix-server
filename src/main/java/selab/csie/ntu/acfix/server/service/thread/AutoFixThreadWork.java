package selab.csie.ntu.acfix.server.service.thread;

import lombok.SneakyThrows;
import org.json.JSONObject;
import selab.csie.ntu.acfix.server.config.GlobalConfig;
import selab.csie.ntu.acfix.server.model.message.ACFixInvokeMessage;
import selab.csie.ntu.acfix.server.service.FixingRecordService;
import selab.csie.ntu.acfix.server.service.WebSocketService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoFixThreadWork implements Runnable {

    private Integer id;
    private String dockerImage;
    private ACFixInvokeMessage message;
    private DockerClient docker;
    private FixingRecordService recordService;
    private WebSocketService socketService;
    private GlobalConfig config;

    public AutoFixThreadWork(Integer id, String dockerImage, ACFixInvokeMessage message, DockerClient docker,
                             FixingRecordService recordService, WebSocketService socketService, GlobalConfig config) {
        this.id = id;
        this.dockerImage = dockerImage;
        this.message = message;
        this.docker = docker;
        this.recordService = recordService;
        this.socketService = socketService;
        this.config = config;
    }

    @SneakyThrows
    public void run() {
        if (!ensureDockerVolume()) {
            abortWork();
            return;
        }
        if (docker.createContainer(configContainer())) {
            docker.startContainer();
        } else {
            abortWork();
            return;
        }
        Pattern pattern = Pattern.compile("\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}]\\[(.{5})] (.+)");
        InputStreamReader stream = new InputStreamReader(
                new FileInputStream(new File(config.getVolumePath(), String.format("%d/%d.log", id, id))),
                StandardCharsets.UTF_8
        );
        boolean result = true;
        boolean finished = false;
        while (!finished) {
            String line = AutoFixThreadWork.readLineFromStream(stream);
            socketService.sendACFixLog(message.getSocketID(), line);
            Matcher matcher = pattern.matcher(line);
            if (!matcher.matches())
                continue;
            switch (matcher.group(1)) {
                case "START":
                    socketService.sendACFixStage(message.getSocketID(), matcher.group(2));
                    break;
                case "STAGE":
                    result &= !matcher.group(2).endsWith("Failed");
                    socketService.sendACFixStage(message.getSocketID(), matcher.group(2));
                    break;
                case "FINAL":
                    recordService.updateRecord(id, result);
                    socketService.sendACFixStage(message.getSocketID(), matcher.group(2));
                    socketService.sendWebSocketTerminate(message.getSocketID());
                    finished = true;
                    break;
                default:
                    break;
            }
        }
        stream.close();
    }

    private void abortWork() {
        recordService.removeRecord(id);
        socketService.sendWebSocketTerminate(message.getSocketID());
    }

    private boolean ensureDockerVolume() throws IOException {
        File resultRoot = new File(config.getVolumePath());
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

    private JSONObject configContainer() {
        String volumeSrc = config.isDockerMode() ? config.getVolumeName() : config.getVolumePath();
        return new JSONObject()
                .put("Image", dockerImage)
                .put("Env", Arrays.asList(
                        String.format("BUILD_INDEX=%d", id),
                        String.format("BUILD_TARGET=%s", message.getUrl())
                ))
                .put("HostConfig", new JSONObject()
                        .put("AutoRemove", true)
                        .put("Binds", Collections.singletonList(String.format("%s:/opt/volume", volumeSrc)))
                );
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

}
