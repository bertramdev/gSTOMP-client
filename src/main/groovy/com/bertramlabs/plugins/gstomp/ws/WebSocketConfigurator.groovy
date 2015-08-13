package com.bertramlabs.plugins.gstomp.ws

import javax.websocket.ClientEndpointConfig
import javax.websocket.HandshakeResponse

/**
 * Created by davydotcom on 8/13/15.
 */
class WebSocketConfigurator extends ClientEndpointConfig.Configurator {
    static volatile boolean called = false;
    private Map customHeaders=null

    public WebSocketConfigurator(Map headers=null) {
        this.customHeaders = headers
    }

    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        called = true;
        customHeaders?.each { customHeader ->
            headers.put(customHeader.key, Arrays.asList(customHeader.value))
        }
        headers.put("Origin", Arrays.asList("http://localhost:8080"));
    }

    @Override
    public void afterResponse(HandshakeResponse handshakeResponse) {
        final Map<String, List<String>> headers = handshakeResponse.getHeaders();
        // println "received Header ${headers.get("origin")}"
        // assertEquals("*", headers.get("origin").get(0));
    }
}
