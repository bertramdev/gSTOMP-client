package com.bertramlabs.plugins.gstomp.sockjs

import com.bertramlabs.plugins.gstomp.ws.WebSocketHandler
import groovy.util.logging.Commons
import com.bertramlabs.plugins.gstomp.ws.MessageHandler
import com.bertramlabs.plugins.gstomp.ws.WebSocketOnCloseInterceptor

@Commons
public class SockJsWsHandler extends WebSocketHandler{
    public SockJsWsHandler(URI endpointURI, Map headers = null, MessageHandler msgHandler = null, WebSocketOnCloseInterceptor closeInterceptor=null) {
        super(endpointURI,headers,msgHandler,closeInterceptor);
    }
}