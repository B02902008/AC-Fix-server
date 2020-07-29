package selab.csie.ntu.acfix.server.service.thread;

import lombok.SneakyThrows;
import selab.csie.ntu.acfix.server.config.GlobalConfig;
import selab.csie.ntu.acfix.server.service.WebSocketService;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class LogStreamThreadWork implements Runnable {

    private Integer id;
    private String socketID;
    private WebSocketService service;
    private GlobalConfig config;

    public LogStreamThreadWork(Integer id, String socketID, WebSocketService service, GlobalConfig config) {
        this.id = id;
        this.socketID = socketID;
        this.service = service;
        this.config = config;
    }

    @SneakyThrows
    public void run() {
        File logFile = new File(config.getVolumePath(), String.format("%d/%d.log", id, id));
        if (!logFile.exists()) {
            service.sendACFixLog(socketID, "Log file does not exist.");
            service.sendWebSocketTerminate(socketID);
            return;
        }
        Pattern pattern = Pattern.compile("\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}]\\[FINAL] .+");
        InputStreamReader stream = new InputStreamReader(new FileInputStream(logFile), StandardCharsets.UTF_8);
        while (service.socketAlive(socketID)) {
            String line = AutoFixThreadWork.readLineFromStream(stream);
            service.sendACFixLog(socketID, line);
            if (pattern.matcher(line).matches()) {
                service.sendWebSocketTerminate(socketID);
                break;
            }
        }
        stream.close();
    }

}
