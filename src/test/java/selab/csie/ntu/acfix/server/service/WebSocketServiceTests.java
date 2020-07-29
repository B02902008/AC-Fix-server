package selab.csie.ntu.acfix.server.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import selab.csie.ntu.acfix.server.service.websocket.StompPrincipal;

import java.util.HashMap;
import java.util.Map;

class WebSocketServiceTests {

    private SimpMessagingTemplate template;
    private WebSocketService service;
    private static String socketID = "SOCKET_ID";
    private static String message = "MESSAGE";
    private static SessionConnectedEvent connected;
    private static SessionDisconnectEvent disconnect;

    @BeforeAll
    static void mockEvent() {
        Map<String, Object> map = new HashMap<>();
        map.put("simpUser", new StompPrincipal(socketID));
        Message<byte[]> msg = new GenericMessage<>(new byte[]{}, map);
        connected = new SessionConnectedEvent(new Object(), msg);
        disconnect = new SessionDisconnectEvent(new Object(), msg, "", null);
    }

    @BeforeEach
    void setup() {
        template = Mockito.mock(SimpMessagingTemplate.class);
        BDDMockito.willDoNothing().given(template).convertAndSendToUser(anyString(), anyString(), anyString());
        service = new WebSocketService(template);
    }

    /* Test socket alive */
    @Test
    void testSocketAlive() {
        assertFalse(service.socketAlive(socketID));
        service.handleWebSocketConnected(connected);
        assertTrue(service.socketAlive(socketID));
        service.handleWebSocketDisconnected(disconnect);
        assertFalse(service.socketAlive(socketID));
    }

    /* Test send terminate */
    @Test
    void testSendTerminate() {
        service.handleWebSocketConnected(connected);
        service.sendWebSocketTerminate(socketID);
        Mockito.verify(template, Mockito.times(1)).convertAndSendToUser(eq(socketID), anyString(), anyString());
    }

    /* Test send terminate not alive */
    @Test
    void testSendTerminateNotAlive() {
        service.sendWebSocketTerminate(socketID);
        Mockito.verify(template, Mockito.times(0)).convertAndSendToUser(anyString(), anyString(), anyString());
    }

    /* Test send log */
    @Test
    void testSendLog() {
        service.handleWebSocketConnected(connected);
        service.sendACFixLog(socketID, message);
        Mockito.verify(template, Mockito.times(1)).convertAndSendToUser(eq(socketID), anyString(), eq(message));
    }

    /* Test send log not alive */
    @Test
    void testSendLogNotAlive() {
        service.sendACFixLog(socketID, message);
        Mockito.verify(template, Mockito.times(0)).convertAndSendToUser(anyString(), anyString(), anyString());
    }

    /* Test send stage */
    @Test
    void testSendStage() {
        service.handleWebSocketConnected(connected);
        service.sendACFixStage(socketID, message);
        Mockito.verify(template, Mockito.times(1)).convertAndSendToUser(eq(socketID), anyString(), eq(message));
    }

    /* Test send stage not alive */
    @Test
    void testSendStageNotAlive() {
        service.sendACFixStage(socketID, message);
        Mockito.verify(template, Mockito.times(0)).convertAndSendToUser(anyString(), anyString(), anyString());
    }

}
