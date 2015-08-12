package com.bertramlabs.plugins.gstomp

public class TestMessageHandler implements MessageHandler {
    public void handleMessage(String message) {
    	println "RECEIVED MESSAGE: ${message}"
    }

}