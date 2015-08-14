package com.bertramlabs.plugins.gstomp.sockjs

import com.bertramlabs.plugins.gstomp.StompClient
import com.bertramlabs.plugins.gstomp.ws.MessageHandler
import groovy.util.logging.Commons

/**
 * WebSocket message handler for SockJs to STOMP
 * @author David Estes
 */
@Commons
public class StompMessageHandler implements MessageHandler {

    SockJsStompChannel sockHandler;
    StompClient stompClient

    public StompMessageHandler(SockJsStompChannel sockHandler) {
        this.sockHandler = sockHandler
        this.stompClient = sockHandler.stompClient
    }

    public void handleMessage(String message) {
        log.debug("Received Message over SockJs: ${message}")
        if(message == 'o') {
            //We received an open connection init
            stompClient.sendSTOMPConnectRequest()
            return
        }
        if(message.startsWith("a[")) {
            String extractedMessage = message.substring(3,message.size() - 3)
            sockHandler.handleMessage(extractedMessage)
        }
    }

}
