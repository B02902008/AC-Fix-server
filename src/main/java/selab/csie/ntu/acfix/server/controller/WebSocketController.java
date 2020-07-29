package selab.csie.ntu.acfix.server.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.security.Principal;

@Controller
@CrossOrigin("*")
public class WebSocketController {

    @MessageMapping("/whoami")
    @SendToUser("/topic/socket-ID")
    public String returnID(Principal principal) {
        return principal.getName();
    }

}
