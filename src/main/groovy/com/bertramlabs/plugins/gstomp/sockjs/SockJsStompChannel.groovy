package com.bertramlabs.plugins.gstomp.sockjs

import com.bertramlabs.plugins.gstomp.StompChannelInterface
import com.bertramlabs.plugins.gstomp.StompClient
import com.bertramlabs.plugins.gstomp.StompFrame
import groovy.json.JsonSlurper
import groovy.json.StringEscapeUtils
import groovy.util.logging.Commons

/**
 * Interfaces with a Sock.Js URL endpoint and assesses the proper underlying protocol to use with STOMP
 * Currently only the websocket SockJs interface is supported.
 * @author David Estes
 */
@Commons
public class SockJsStompChannel implements StompChannelInterface {
    URL endpointURL
    Map sockJsInfo
    String sessionId
    Integer serverId
    SockJsWsHandler websocketHandler
    StompClient stompClient
    Map requestHeaders = null
    Boolean connected = false

    public SockJsStompChannel(URL endpointURL,String sessionId = null, Map headers = null) {
        this.endpointURL = endpointURL;
        this.stompClient = stompClient
        if(sessionId) {
            this.sessionId = sessionId
        } else {
            this.sessionId = UUID.randomUUID()
        }
        this.requestHeaders = headers
        this.serverId = (Math.random()*999).toInteger()
    }

    public Boolean connect() {
        URL infoUrl = new URL(endpointURL, "info")

        try {
            log.info("Getting SockJs Connection Info ${infoUrl}")

            String infoText = infoUrl.text
            this.sockJsInfo = new JsonSlurper().parseText(infoText) as Map;

            if(sockJsInfo.websocket) {
                return connectWs();
            } else {
                throw new ProtocolException("The STOMP Sockjs Interface currently only supports the websocket interface")
            }
        } catch(ex) {
            log.error("Error fetching connection information from the socket. ${ex}",ex)
            return false;
        }
    }

    private Boolean connectWs() {
        URI webSocketURI = getWebsocketURI()
        this.websocketHandler = new SockJsWsHandler(webSocketURI, requestHeaders)
        websocketHandler.addMessageHandler(new StompMessageHandler(this))
        websocketHandler.addCloseInterceptor(stompClient)
        this.connected = true
        this.sendMessage("[\"\"]")
        //this.stompClient.sendSTOMPConnectRequest()
        return true;
    }

    public Boolean isConnected() {
        return this.connected
    }

    public void disconnect() {
        if(!this.connected) {
            return
        }
        this.websocketHandler.disconnect()
        this.stompClient.setConnected(false)
    }


    public StompClient getStompClient() {
        return this.stompClient
    }

    public void setStompClient(StompClient stompClient) {
        this.stompClient = stompClient
    }

    public void sendMessage(String message) {
        log.debug("Sending Message over SockJs: ${message}")
        this.websocketHandler.sendMessage(message)
    }

    public void sendStompFrame(StompFrame stompFrame) {
        String escapedFrame = StringEscapeUtils.escapeJava(stompFrame.toString())
        sendMessage("[\"${escapedFrame}\"]")
    }

    public void handleMessage(String message) {
        stompClient.handleMessage(StringEscapeUtils.unescapeJava(message).replaceAll("\u0000", ""))

    }


    protected URI getWebsocketURI() {
        String wsProtocol = "ws"
        if(endpointURL.protocol.toLowerCase() == 'https') {
            wsProtocol = 'wss'
        }
        String wsStringURI = "${wsProtocol}://${endpointURL.host}"
        if(endpointURL.port != -1) {
            wsStringURI += ":${endpointURL.port}"
        }

        wsStringURI += "${endpointURL.path}${serverId}/${sessionId}/websocket"
        return new URI(wsStringURI)
    }
}