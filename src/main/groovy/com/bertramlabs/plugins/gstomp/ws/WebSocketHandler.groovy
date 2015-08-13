package com.bertramlabs.plugins.gstomp.ws


import groovy.util.logging.Commons

import javax.websocket.ClientEndpointConfig
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider
import javax.websocket.Endpoint
import javax.websocket.EndpointConfig
import javax.websocket.Session;
import javax.websocket.WebSocketContainer
import javax.websocket.MessageHandler.Whole

@Commons
public class WebSocketHandler extends Endpoint implements javax.websocket.MessageHandler.Whole<String> {
    Session userSession = null;
    private boolean connected = false;
    private MessageHandler messageHandler;
    private WebSocketOnCloseInterceptor closeInterceptor;


    public WebSocketHandler(URI endpointURI, ClientEndpointConfig clientEndpointConfig) {
        try {
            log.info("Opening WS Connection ${endpointURI}")

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, clientEndpointConfig, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public WebSocketHandler(URI endpointURI, Map headers=null) {
        try {
            log.info("Opening WS Connection ${endpointURI}")
            WebSocketConfigurator configurator = new WebSocketConfigurator(headers)
            ClientEndpointConfig clientEndpointConfig = ClientEndpointConfig.Builder.create().configurator(configurator).build();
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, clientEndpointConfig, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void disconnect() {
        if(!this.connected) {
            return
        }
        this.userSession?.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE,"User Disconnect Requested"))
        this.connected = false
    }

    public Boolean isConnected() {
        return this.connected
    }
 
    /**
     * Callback hook for Connection open events.
     * 
     * @param userSession
     *            the userSession which is opened.
     */
    public void onOpen(Session userSession, EndpointConfig config) {
        this.userSession = userSession;
        this.connected = true
        def messageHandler = this.messageHandler
        this.userSession.addMessageHandler(this)
    }
 
    /**
     * Callback hook for Connection close events.
     * 
     * @param userSession
     *            the userSession which is getting closed.
     * @param reason
     *            the reason for connection close
     */
    public void onClose(Session userSession, CloseReason reason) {
        this.userSession = null;
//        if(reason.closeCode != CloseReason.CloseCodes.NORMAL_CLOSURE) {
//         //FIXME: We need to notify the root object immediately and attempt reconnects
//        }
        if(this.closeInterceptor) {
            this.closeInterceptor.onClose()
        }
        this.connected = false
    }

    public void onError(Session session, Throwable thr) {
        log.error("Websocket Error Occurred! ${thr}",thr)
    }

    public void onMessage(String text) {
        if(this.messageHandler != null) {
            this.messageHandler.handleMessage(text)
        }
    }


    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    public void addCloseInterceptor(WebSocketOnCloseInterceptor closeInterceptor) {
        this.closeInterceptor = closeInterceptor
    }
 
    public void sendMessage(String message) {
        this.userSession.getAsyncRemote().sendText(message);
    }


}