package selab.csie.ntu.acfix.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

@Service
public class WebSocketService {

    private Set<String> socketIDs;
    private final SimpMessagingTemplate template;
    private static final String WS_DESTINATION_TERMINATE = "/topic/terminate";
    private static final String WS_DESTINATION_ACFIX_LOG = "/topic/acfix/log";
    private static final String WS_DESTINATION_ACFIX_STAGE = "/topic/acfix/stage";

    @Autowired
    WebSocketService(SimpMessagingTemplate template) {
        this.socketIDs = new HashSet<>();
        this.template = template;
    }

    @EventListener(SessionConnectedEvent.class)
    public void handleWebSocketConnected(SessionConnectedEvent event) {
        MessageHeaders headers = event.getMessage().getHeaders();
        Principal principal = (Principal) headers.getOrDefault("simpUser", null);
        String socketID = principal != null ? principal.getName() : "Anonymous";
        this.socketIDs.add(socketID);
    }

    @EventListener(SessionDisconnectEvent.class)
    public void handleWebSocketDisconnected(SessionDisconnectEvent event) {
        MessageHeaders headers = event.getMessage().getHeaders();
        Principal principal = (Principal) headers.getOrDefault("simpUser", null);
        String socketID = principal != null ? principal.getName() : "Anonymous";
        this.socketIDs.remove(socketID);
    }

    public void sendWebSocketTerminate(String socketID) {
        if (this.socketAlive(socketID))
            this.template.convertAndSendToUser(socketID, WS_DESTINATION_TERMINATE, "");
    }

    public void sendACFixLog(String socketID, String msg) {
        if (this.socketAlive(socketID))
            this.template.convertAndSendToUser(socketID, WS_DESTINATION_ACFIX_LOG, msg);
    }

    public void sendACFixStage(String socketID, String msg) {
        if (this.socketAlive(socketID))
            this.template.convertAndSendToUser(socketID, WS_DESTINATION_ACFIX_STAGE, msg);
    }

    public boolean socketAlive(String socketID) {
        return socketID != null && this.socketIDs.contains(socketID);
    }

}
