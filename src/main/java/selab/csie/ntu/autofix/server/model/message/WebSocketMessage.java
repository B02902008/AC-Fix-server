package selab.csie.ntu.autofix.server.model.message;

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
