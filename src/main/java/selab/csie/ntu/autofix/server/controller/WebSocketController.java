package selab.csie.ntu.autofix.server.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import selab.csie.ntu.autofix.server.websocket.WebSocketMessage;

import java.security.Principal;

@Controller
public class WebSocketController {

    @MessageMapping("/whoami")
    @SendToUser("socket-ID")
    public WebSocketMessage returnID(Principal principal) {
        return new WebSocketMessage(principal.getName());
    }

}
