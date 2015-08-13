package com.bertramlabs.plugins.gstomp.ws

/**
 * A Simple interface for registering close interceptors
 * @author David Estes
 */
interface WebSocketOnCloseInterceptor {
    public void onClose()
}