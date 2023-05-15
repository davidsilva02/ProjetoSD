package com.ProjetoSD_META2.ProjetoSD_META2;

import com.ProjetoSD_META2.ProjetoSD_META2.Component;
import com.ProjetoSD_META2.ProjetoSD_META2.Searched;
import com.ProjetoSD_META2.ProjetoSD_META2.Spring.StatsMessage;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class WebSocketClient {
    List<Transport> transports;
    SockJsClient sockJsClient;
    WebSocketStompClient stompClient;
    StompSessionHandler sessionHandler;

    public boolean sendMessage (HashMap<String, Component> components, List<Searched> searches){
        try {
            StompSession stompSession = null;
            stompSession = stompClient.connect("ws://localhost:8080/stats-websocket", sessionHandler).get();
            stompSession.send("/app/update-stats",new StatsMessage(components,searches));
            stompSession.disconnect();
            return true;
        } catch (InterruptedException | ExecutionException|IllegalStateException | MessageDeliveryException e) {
            return false;
        }
    }

    static class MyStompSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
/*
            System.out.println("Connected");
*/
            session.subscribe("/stats/messages", new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return String.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    System.out.println("Received message: " + payload);
                }
            });
        }
    }

    public WebSocketClient(){
        transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        sockJsClient = new SockJsClient(transports);
        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        stompClient.setTaskScheduler(new ConcurrentTaskScheduler());
        sessionHandler = new MyStompSessionHandler();
    }




}
