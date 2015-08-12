package com.bertramlabs.plugins.gstomp.sockjs

import com.bertramlabs.plugins.gstomp.ws.WebSocketHandler
import groovy.util.logging.Commons

import javax.websocket.ClientEndpoint

@ClientEndpoint(configurator = SockJsConfigurator.class)
@Commons
public class SockJsWsHandler extends WebSocketHandler{
    public SockJsWsHandler(URI endpointURI) {
        //Ok lets ask the Service which url we should hit first
        super(endpointURI);
    }
}