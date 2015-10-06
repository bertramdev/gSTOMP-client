package com.bertramlabs.plugins.gstomp

import com.bertramlabs.plugins.gstomp.sockjs.SockJsStompChannel
import com.bertramlabs.plugins.gstomp.ws.WebSocketOnCloseInterceptor
import groovy.json.JsonOutput
import groovy.util.logging.Commons


/**
 * The primary class for interfacing with a STOMP protocol connection
 * @author David Estes
 */
@Commons
public class StompClient implements WebSocketOnCloseInterceptor {
    public static Integer STOMP_CONNECTION_TIMEOUT = 30000
    private Integer senderHeartbeat = 0;
    private Integer heartbeat = 10000;
    private Boolean autoReconnect = false;
    private Integer maxRetries = 0;
    private Boolean connected;
    private Boolean disconnecting = false;
    private List acceptedProtocols = ['1.0', '1.1', '1.2']
    private String login
    private String passcode
    private StompChannelInterface stompChannel

    private Map subscriptions = [:]
    private Map pendingReceipts = [:]
    private Integer subscriptionIdIncrementer = 0

    /**
     * Creates a StompClient tied to a new StompChannelInterface for communicating over a SockJs Websocket
     * @param endpointURL - The root endpointURL for the SockJs interface (/info and /$serverId/$sessionId/websocket)
     * are automatically appended.
     * @param sessionId - Override to use a specific sessionId . If left blank a random UUID will be used
     * @return stompClient - The initialized stompClient. Now you can set other options and
     * call the {@link connect ( ) connect} method
     */
    public static StompClient overSockJs(URL endpointURL, String sessionId = null, Map headers = null) {
        SockJsStompChannel stompChannel = new SockJsStompChannel(endpointURL, sessionId, headers);

        def stompClient = new StompClient(stompChannel);
        return stompClient;
    }

    /**
     * Can be used to construct a STOMPClient to communicate over a passed in StompChannelInterface
     * @param stompChannel
     */
    public StompClient(StompChannelInterface stompChannel) {
        this.connected = false;
        this.stompChannel = stompChannel;
        stompChannel.setStompClient(this);
    }

    /**
     * Instantiates a connection request to the underlying Channel
     */
    public void connect() {
        if (this.connected) {
            throw new IllegalStateException("The STOMP client is already connected")
            return
        }
        stompChannel.connect();
        Integer counter = STOMP_CONNECTION_TIMEOUT
        while (!this.connected) {
            sleep(250)
            counter -= 250
            if (counter <= 0) {
                stompChannel.disconnect()
                throw new SocketTimeoutException()
            }
        }
    }

    private void reconnect() {
        if (this.connected) {
            throw new IllegalStateException("The STOMP client is already connected")
            return
        }
        Integer retryCounter = maxRetries
        Boolean connectSucceeded = false
        while (!connectSucceeded && (maxRetries == 0 || retryCounter > 0)) {
            try {
                this.connect()
                connectSucceeded = true
            } catch (ex) {
                if(maxRetries > 0) {
                    retryCounter--
                    log.warn("Attempt ${5 - retryCounter} of 5 failed. Trying again in 15 seconds")
                } else {
                    log.warn("STOMP Reconnect attempt failed. Retrying again in 15 seconds")
                }

                sleep(15000)
            }
        }
        if (!connectSucceeded) {
            log.error("STOMP Interface connection failed after 5 retry attempts")
        }
    }

    /**
     * Used to disconnect the stomp client interface
     */
    public void disconnect() {
        if (!this.connected) {
            throw new IllegalStateException("The STOMP client is not yet connected")
            return
        }
        try {
            this.disconnecting = true
            String receiptId = (Math.random() * 100).toInteger().toString()
            StompFrame disconnectFrame = new StompFrame('DISCONNECT', ['receipt-id': receiptId])
            stompChannel.sendStompFrame(disconnectFrame)

            Integer counter = 5000
            while (pendingReceipts[receiptId] != true) {
                sleep(250)
                counter -= 250
                if (this.connected == false) {
                    return
                }
                if (counter <= 0) {
                    break
                }
            }
            this.connected = false
            pendingReceipts.remove(receiptId)
            stompChannel.disconnect()
        } finally {
            this.subscriptions = [:]
            this.disconnecting = false
        }
    }

    /**
     * Returns whether or not the STOMP client is currently connected
     * @return
     */
    public Boolean isConnected() {
        return this.connected
    }

    /**
     * Not for external use. Allows the underlying protocol to flag the STOMP interface as connected or not
     * @param connected
     */
    public void setConnected(Boolean connected) {
        this.connected = connected
    }

    /**
     * Returns the current login name for STOMP
     * @return login
     */
    public String getLogin() {
        return login
    }

    /**
     * Optionally set the login name to be passed on STOMP connect
     * @param login
     */
    public void setLogin(String login) {
        this.login = login
    }

    /**
     * Returns the currently assigned passcode value
     * @return passcode
     */
    public String getPasscode() {
        return passcode
    }

    /**
     * Optionally set a password/passcode to be passed upon STOMP connect
     * @param passcode
     */
    public void setPasscode(String passcode) {
        this.passcode = passcode
    }

    /**
     * Returns the client heartbeat frequency in milliseconds. (Defaults to 0)
     * @return
     */
    public Integer getSenderHeartbeat() {
        return senderHeartbeat
    }

    /**
     * Assigns the client heartbeat frequency in milliseconds.
     * @param senderHeartbeat
     */
    public void setSenderHeartbeat(Integer senderHeartbeat) {
        this.senderHeartbeat = senderHeartbeat
    }

    /**
     * Returns the desired server heartbeat value that will be requested upon connection in milliseconds.
     * (Defaults to 10000)
     * @return
     */
    public Integer getHeartbeat() {
        return heartbeat
    }

    /**
     * Allows assignment of the server heartbeat in milliseconds.
     * @param heartbeat
     */
    public void setHeartbeat(Integer heartbeat) {
        this.heartbeat = heartbeat
    }

    /**
     * Returns whether or not the underlying connection interface should attempt a reconnect in the event of a drop
     * @return autoReconnect
     */
    public Boolean getAutoReconnect() {
        return autoReconnect
    }

    /**
     * Can set whether or not the underlying connection interface should attempt a reconnect
     * If a connection is lost and a reconnect is attempted. gSTOMP will automatically attempt to resubscribe to topics.
     * @param autoReconnect
     */
    public void setAutoReconnect(Boolean autoReconnect) {
        this.autoReconnect = autoReconnect
    }

    /**
     * Sets the max number of retries during a reconnect phase
     * @param retryCount The number of connection retries to attempt. If set to 0 unlimited is assumed
     */
    public void setMaxRetries(Integer retryCount) {
        this.maxRetries = retryCount
    }

    /**
     * Returns a List of supported protocol versions for STOMP
     * @return acceptedProtocols
     */
    public List getSupportedProtocolVersions() {
        return this.acceptedProtocols
    }

    /**
     * Sends the STOMP Connect Frame to the underlying protocol interface
     * This is normally called by the underlying implementation
     */
    public void sendSTOMPConnectRequest() {
        Map connectHeaders = [:]
        connectHeaders['accept-version'] = getSupportedProtocolVersions().join(",")
        connectHeaders['heart-beat'] = "${senderHeartbeat},${heartbeat}"
        if (login) {
            connectHeaders['login'] = login
        }
        if (passcode) {
            connectHeaders['passcode'] = passcode
        }

        StompFrame stompFrame = new StompFrame('CONNECT', connectHeaders, '')

        stompChannel.sendStompFrame(stompFrame)
    }

    private void sendMessage(StompFrame frame) {
        if (!connected) {
            throw new IllegalStateException("The STOMP client is not yet connected")
            return
        }

        stompChannel.sendStompFrame(frame)
    }

    public void handleMessage(String message) {
        StompFrame frame = extractFrameFromMessage(message)

        //Time to do stuff with this
        switch (frame.command) {
            case 'CONNECTED':
                this.connected = true;
                if (this.subscriptions) {
                    recoverSubscriptions()
                }
                break
            case 'RECEIPT':
                handleReceiptFrame(frame)
                break;
            case 'ERROR':
                log.error("Error Received From Server: \n ${frame.body}")
                this.connected = false
                this.stompChannel.disconnect()
                if (this.autoReconnect && !this.disconnecting) {
                    reconnect();
                }
                break
            case 'MESSAGE':
                handleMessageFrame(frame)
                break;
        }
    }

    private handleReceiptFrame(StompFrame frame) {
        pendingReceipts[frame.headers['receipt-id']] = true
    }

    private handleMessageFrame(StompFrame frame) {
        def subscriptionId = frame.headers['subscription']
        def subscription = subscriptions[subscriptionId]
        if (subscription.destination == frame.headers['destination']) {
            try {
                def result = subscription.callback.call(frame)
                if (frame.headers['ack']) {
                    if (result == false) {
                        this.nack(frame.headers['ack'])
                    } else {
                        this.ack(frame.headers['ack'])
                    }
                }
            } catch (ex) {
                this.nack(frame.headers['ack'])
                throw ex
            }
        }
    }

    /**
     * Used to acknowledge a received message
     * Typically a callback is auto acknowledged if the closure executes successfully and
     * returns true
     * @param ackId
     */
    public void ack(String ackId) {
        stompChannel.sendStompFrame(new StompFrame('ACK', [id: ackId]))
    }

    /**
     * Used to notify the server of a failed processing of a message
     * This is typically automatically handled if the callback Closure returns false
     * Or if an exception is thrown
     * @param ackId
     */
    public void nack(String ackId) {
        stompChannel.sendStompFrame(new StompFrame('NACK', [id: ackId]))
    }

    /**
     * STOMP Send Command to send a message to the server
     * @param destination The destination queue/topic/exchange we are sending to
     * @param headers Any headers we want to pass additional (see STOMP 1.2 Spec)
     * @param message The Message contents in String form
     */
    public void send(String destination, Map headers = null, String message = null) {
        if (!connected) {
            throw new IllegalStateException("The STOMP client is not yet connected")
            return
        }
        Map frameHeaders = [destination: destination]

        if (headers) {
            frameHeaders += headers
        }

        if (message) {
            if (!frameHeaders['content-type']) {
                frameHeaders['content-type'] = 'text/plain'
            }
            frameHeaders['content-length'] = message.size()
        }

        StompFrame frame = new StompFrame('SEND', frameHeaders, message)
        stompChannel.sendStompFrame(frame)
    }

    /**
     * STOMP Send Command to send a Json Encoded Object to the Server
     * @param destination The destination queue/topic/exchange we are sending to
     * @param headers Any custom headers
     * @param message The Message Object we are encoding as JSON
     */
    public void send(String destination, Map headers = null, Object message) {
        if (!connected) {
            throw new IllegalStateException("The STOMP client is not yet connected")
            return
        }
        if (message) {

            Map frameHeaders = (headers ?: [:]) + ['content-type': 'application/json']
            String jsonMessage = JsonOutput.toJson(message)
            send(destination, frameHeaders, jsonMessage)
        } else {
            send(destination, headers)
        }
    }

    /**
     * Subscribes a Closure to handle responses from a STOMP Queue
     * @param destination
     * @param callback
     * @param headers
     * @return subscriptionId
     */
    public Integer subscribe(String destination, Closure callback, Map headers = null) {
        if (!connected) {
            throw new IllegalStateException("The STOMP client is not yet connected")
            return
        }
        Map frameHeaders = [destination: destination, id: subscriptionIdIncrementer++]

        if (headers) {
            frameHeaders += headers
        }
        subscriptions[frameHeaders.id.toString()] = [callback: callback, destination: destination, headers: headers]
        StompFrame frame = new StompFrame('SUBSCRIBE', frameHeaders)
        stompChannel.sendStompFrame(frame)
        return frameHeaders.id
    }

    /**
     * Internally handles recovery of subscriptions in the event a connection is lost and re-instantiated
     */
    private void recoverSubscriptions() {
        this.subscriptions?.each { subscriptionEntry ->

            Map frameHeaders = [destination: subscriptionEntry.value.destination, id: subscriptionEntry.key]
            if (subscriptionEntry.value.headers) {
                frameHeaders += subscriptionEntry.value.headers
            }
            StompFrame frame = new StompFrame('SUBSCRIBE', frameHeaders)
            stompChannel.sendStompFrame(frame)
        }
    }

    /**
     * Unsubscribe all events tied to a specific destination or callback Closure
     * @param destination
     * @param callback (optional) pass null if you want to unsubscribe from the destination entirely
     */
    public void unsubscribe(String destination, Closure callback) {
        if (!connected) {
            throw new IllegalStateException("The STOMP client is not yet connected")
            return
        }
        Map matchedSubscriptions = frameHeaders.findAll { entry ->
            if (callback) {
                return entry.value.destination == destination && entry.value.callback == callback
            } else {
                return entry.value.destination == destination
            }
        }

        matchedSubscriptions?.each { entry ->
            ubsubscribe(entry.key.toInteger())
        }
    }

    /**
     * Unsubscribe from STOMP subscription by id
     * @param id
     */
    public void unsubscribe(Integer id) {
        if (!connected) {
            throw new IllegalStateException("The STOMP client is not yet connected")
            return
        }
        stompChannel.sendStompFrame(new StompFrame('UNSUBSCRIBE', [id: id]))
        subscriptions.remove(id.toString())
    }

    /**
     * Extracts a STOMP formatted String body into a StompFrame object
     * @param message The input String to be parsed
     * @return StompFrame
     */
    private StompFrame extractFrameFromMessage(String message) {
        def lines = message.split("\n")
        def bodyArgs
        String command = lines ? lines[0] : ''
        String body
        Map headers
        Boolean blankLineFound = false

        if(lines && lines.size() > 1) {
            lines[1..-1].each { line ->
                if (blankLineFound) {
                    if (!bodyArgs) {
                        bodyArgs = [line]
                    } else {
                        bodyArgs << line
                    }
                } else if (!blankLineFound && line == '') {
                    blankLineFound = true
                } else {
                    def headerArgs = line.split(":")
                    if (headerArgs.size() == 2) {
                        if (!headers) {
                            headers = [:]
                        }
                        headers[headerArgs[0]] = headerArgs[1]
                    }
                }
            }
        }

        if (bodyArgs) {
            body = bodyArgs.join("\n").replaceAll("\u0000", "")
        }

        return new StompFrame(command, headers, body)
    }

    @Override
    public void onClose() {
        if (this.connected && !this.disconnecting) {
            this.connected = false
            if (this.autoReconnect) {
                this.reconnect()
            }
        }
    }
}