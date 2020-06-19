package selab.csie.ntu.autofix.server.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import selab.csie.ntu.autofix.server.model.message.WebSocketMessage;

import java.security.Principal;

@Controller
@CrossOrigin("*")
public class WebSocketController {

    @MessageMapping("/whoami")
    @SendToUser("/topic/socket-ID")
    public WebSocketMessage returnID(Principal principal) {
        return new WebSocketMessage(principal.getName());
    }

}
