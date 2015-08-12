package com.bertramlabs.plugins.gstomp

/**
 * Provides a Standard Channel Interface for the STOMP protocol
 * This can be used to implement support for STOMP over multiple Mediums. Including Websockets, and Straight Sockets
 * @author David Estes
 */
interface StompChannelInterface {

    public void setStompClient(StompClient stompClient)

    public StompClient getStompClient()

    public Boolean connect();

    public void disconnect();

    public Boolean isConnected();

    public void sendMessage(String message)

    public void sendStompFrame(StompFrame frame)

    public void handleMessage(String message)
}