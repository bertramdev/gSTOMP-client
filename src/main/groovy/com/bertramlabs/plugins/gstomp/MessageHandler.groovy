package com.bertramlabs.plugins.gstomp

public interface MessageHandler {
    public void handleMessage(String message);
}