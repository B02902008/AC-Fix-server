package selab.csie.ntu.autofix.server.websocket;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class WebSocketMessage {

    private final String content;

    public WebSocketMessage() {
        this.content = "";
    }

}
