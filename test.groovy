import com.bertramlabs.plugins.gstomp.*
import sun.net.www.protocol.https.*
import sun.net.www.protocol.http.*
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLContext
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.HostnameVerifier

// Create a trust manager that does not validate certificate chains
TrustManager[] trustAllCerts = [
    new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }
        public void checkClientTrusted(
            java.security.cert.X509Certificate[] certs, String authType) {
        }
        public void checkServerTrusted(
            java.security.cert.X509Certificate[] certs, String authType) {
        }
    }
] as TrustManager[]

// Install the all-trusting trust manager
//try {
    SSLContext sc = SSLContext.getInstance("SSL");
    sc.init(null, trustAllCerts, new java.security.SecureRandom());
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
        public boolean verify(String host, SSLSession sess) {
        return true
        }
    });
//} catch (Exception e) {
//}



def stompClient = StompClient.overSockJs(new java.net.URL("https://10.100.54.2/stomp/"), null, ['X-API-KEY':'1234567'])
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