package selab.csie.ntu.autofix.server.service.websocket;

import java.security.Principal;

public class StompPrincipal implements Principal {

    private final String name;

    StompPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

}
