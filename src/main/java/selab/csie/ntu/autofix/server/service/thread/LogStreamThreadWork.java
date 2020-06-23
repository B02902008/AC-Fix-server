package selab.csie.ntu.autofix.server.service.thread;

import lombok.SneakyThrows;
import selab.csie.ntu.autofix.server.service.WebSocketService;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogStreamThreadWork implements Runnable {

    private Integer id;
    private String socketID;
    private WebSocketService service;

    private static final String AUTOFIX_RESULT_DIRECTORY = "./data";

    public LogStreamThreadWork(Integer id, String socketID, WebSocketService service) {
        this.id = id;
        this.socketID = socketID;
        this.service = service;
    }

    @SneakyThrows
    public void run() {
        File logFile = new File(AUTOFIX_RESULT_DIRECTORY + String.format("/%d/%d.log", id, id));
        if (!logFile.exists()) {
            service.sendAutoFixLog(socketID, "Log file does not exist.");
            service.sendWebSocketTerminate(socketID);
        }
        Pattern pattern = Pattern.compile("\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}]\\[(.{5})] .+");
        InputStreamReader stream = new InputStreamReader(new FileInputStream(logFile), StandardCharsets.UTF_8);
        while ( true ) {
            String line = AutoFixThreadWork.readLineFromStream(stream);
            service.sendAutoFixLog(socketID, line);
            Matcher matcher = pattern.matcher(line);
            if ( matcher.matches() && matcher.group(1).equals("FINAL") ) {
                service.sendWebSocketTerminate(socketID);
                break;
            } else if (!service.socketAlive(socketID)) {
                break;
            }
        }
    }

}
