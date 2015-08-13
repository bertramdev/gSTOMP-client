import com.bertramlabs.plugins.gstomp.*

def stompClient = StompClient.overSockJs(new java.net.URL("http://localhost:8080/stomp/"), null, ['X-API-KEY':'1234567'])
stompClient.setAutoReconnect(true)
stompClient.connect()

stompClient.isConnected()

stompClient.subscribe("/user/queue/statCommands") { stompFrame ->
    println "received StompFrame ${stompFrame.body}"
}

while(true) {
    if(stompClient.isConnected()) {
            stompClient.send("/app/requestStatJobs",[:],"123456")
    }    
    sleep(5000)
}
