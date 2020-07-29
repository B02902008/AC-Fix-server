package selab.csie.ntu.acfix.server.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import selab.csie.ntu.acfix.server.service.websocket.StompHandshakeHandler;

@Configuration
@EnableWebSocketMessageBroker
@EnableConfigurationProperties(GlobalConfig.class)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/ws-private");
        registry.setApplicationDestinationPrefixes("/websocket");
        registry.setUserDestinationPrefix("/ws-private");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-connect")
                .setHandshakeHandler(new StompHandshakeHandler())
                .setAllowedOrigins("*");
    }

}
