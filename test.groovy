import com.bertramlabs.plugins.gstomp.*

def stompClient = StompClient.overSockJs(new java.net.URL("http://localhost:8080/stomp/"),"abcdefg")

stompClient.connect()

sleep(1000)
stompClient.isConnected()

stompClient.subscribe("/topic/statCommands") { stompFrame ->
    println "received StompFrame ${stompFrame.body}"
}

stompClient.send("/app/requestStatJobs",[:],"123456")