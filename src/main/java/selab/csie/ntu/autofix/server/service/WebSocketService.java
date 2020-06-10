package selab.csie.ntu.autofix.server.service;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class WebSocketService {

    private Set<String> socketIDs;
    private final SimpMessagingTemplate template;
    private static final String WS_DESTINATION_TERMINATE = "/terminate";
    private static final String WS_DESTINATION_AUTOFIX_LOG = "/autofix/log";
    private static final String WS_DESTINATION_AUTOFIX_STAGE = "/autofix/stage";

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
        log.debug("WebSocket connected: " + socketID);
    }

    @EventListener(SessionDisconnectEvent.class)
    public void handleWebSocketDisconnected(SessionDisconnectEvent event) {
        MessageHeaders headers = event.getMessage().getHeaders();
        Principal principal = (Principal) headers.getOrDefault("simpUser", null);
        String socketID = principal != null ? principal.getName() : "Anonymous";
        this.socketIDs.remove(socketID);
        log.debug("WebSocket disconnected: " + socketID);
    }

    public void sendWebSocketTerminate(String socketID) {
        if ( socketID != null && this.socketIDs.contains(socketID) )
            this.template.convertAndSendToUser(socketID, WS_DESTINATION_TERMINATE, "");
    }

    public void sendAutoFixLog(String socketID, String msg) {
        if ( socketID != null && this.socketIDs.contains(socketID) )
            this.template.convertAndSendToUser(socketID, WS_DESTINATION_AUTOFIX_LOG, msg);
    }

    public void sendAutoFixStage(String socketID, String msg) {
        if ( socketID != null && this.socketIDs.contains(socketID) )
            this.template.convertAndSendToUser(socketID, WS_DESTINATION_AUTOFIX_STAGE, msg);
    }

    public boolean socketAlive(String socketID) {
        return socketID != null && this.socketIDs.contains(socketID);
    }

}
