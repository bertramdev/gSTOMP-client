package com.bertramlabs.plugins.gstomp.ws

import groovy.util.logging.Commons

import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

@Commons
public class WebSocketHandler {
    Session userSession = null;
    private MessageHandler messageHandler;

    public WebSocketHandler(URI endpointURI) {
        try {
            log.info("Opening WS Connection ${endpointURI}")
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
 
    /**
     * Callback hook for Connection open events.
     * 
     * @param userSession
     *            the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        this.userSession = userSession;
    }
 
    /**
     * Callback hook for Connection close events.
     * 
     * @param userSession
     *            the userSession which is getting closed.
     * @param reason
     *            the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        this.userSession = null;
    }
 

    @OnMessage
    public void onMessage(String message) {
        if (this.messageHandler != null)
            this.messageHandler.handleMessage(message);
    }
 

    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }
 
    public void sendMessage(String message) {
        this.userSession.getAsyncRemote().sendText(message);
    }


}