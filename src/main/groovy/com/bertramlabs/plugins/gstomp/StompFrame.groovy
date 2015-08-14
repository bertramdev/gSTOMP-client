package com.bertramlabs.plugins.gstomp

import groovy.json.JsonSlurper
import groovy.util.logging.Commons

/**
 * Representation of a STOMP Frame
 * @author David Estes
 */
@Commons
class StompFrame {
    /**
     * The Command from the STOMP frame
     */
    private String command

    /**
     * Map of header entries parsed from the STOMP frame
     */
    private Map headers

    /**
     * The Body Response if it exists
     */
    private String body

    /**
     * Creates a StompFrame representation containing the command,headers, and body
     * @param command
     * @param headers
     * @param body
     */
    public StompFrame(String command, Map headers=null, String body=null) {
        this.command = command
        this.headers = headers
        this.body = body
    }


    public def getJsonBody() {
        if(this.body) {
            try {
                return new JsonSlurper().parseText(this.body)
            } catch(ex) {
                log.warn("Error Processing Response as JSON ${ex}",ex)
                return null
            }
        }
        return null
    }

    public String toString() {
        String message = command + "\n"
        headers?.each { entry ->
            message += "${entry.key}:${entry.value}\n"
        }
        message += "\n"
        if(body) {
            message += body
        }
        message += "\u0000"
        return message
    }
}
