package com.bertramlabs.plugins.gstomp.ws

public interface MessageHandler {
    public void handleMessage(String message);
}