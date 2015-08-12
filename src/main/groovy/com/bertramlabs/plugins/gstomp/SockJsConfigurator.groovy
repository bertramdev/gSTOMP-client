package com.bertramlabs.plugins.gstomp


import javax.websocket.ClientEndpoint;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.HandshakeResponse;

public class SockJsConfigurator extends ClientEndpointConfig.Configurator {
    static volatile boolean called = false;

    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        called = true;
        
        headers.put("Origin", Arrays.asList("http://localhost:8080"));
    }

    @Override
    public void afterResponse(HandshakeResponse handshakeResponse) {
        final Map<String, List<String>> headers = handshakeResponse.getHeaders();
        println "received Header ${headers.get("origin")}"
        // assertEquals("*", headers.get("origin").get(0));
    }
}