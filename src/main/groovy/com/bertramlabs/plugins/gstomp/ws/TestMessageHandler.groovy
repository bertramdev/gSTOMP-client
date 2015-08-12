package com.bertramlabs.plugins.gstomp.ws

public class TestMessageHandler implements MessageHandler {
    public void handleMessage(String message) {
    	println "RECEIVED MESSAGE: ${message}"
    }

}