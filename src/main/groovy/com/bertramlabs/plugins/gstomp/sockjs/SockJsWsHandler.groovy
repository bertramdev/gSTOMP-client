package com.bertramlabs.plugins.gstomp.sockjs

import com.bertramlabs.plugins.gstomp.ws.WebSocketHandler
import groovy.util.logging.Commons

@Commons
public class SockJsWsHandler extends WebSocketHandler{
    public SockJsWsHandler(URI endpointURI, Map headers = null) {
        super(endpointURI,headers);
    }
}