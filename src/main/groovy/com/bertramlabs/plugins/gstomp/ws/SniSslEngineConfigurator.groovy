package com.bertramlabs.plugins.gstomp.ws

import groovy.util.logging.Commons
import javax.net.ssl.*
import org.glassfish.tyrus.client.SslContextConfigurator
import org.glassfish.tyrus.client.SslEngineConfigurator

@Commons
public class SniSslEngineConfigurator extends SslEngineConfigurator {

	private String sniHostname

	public SniSslEngineConfigurator(SslContextConfigurator sslContextConfiguration, String sniHostname) {
		super(sslContextConfiguration)
		this.sniHostname = sniHostname
	}

	public String getSniHostname() {
		return sniHostname
	}

	public void setSniHostname(String sniHostname) {
		this.sniHostname = sniHostname
	}

	public SSLEngine configure(final SSLEngine sslEngine) {
		if(sniHostname) {
			SSLParameters sslParameters = sslEngine.getSSLParameters()
			if(sslParameters == null)
				sslParameters = new SSLParameters()
			List sniHostNames = []
			sniHostNames << new SNIHostName(sniHostname)
			sslParameters.setServerNames(sniHostNames)
			sslEngine.setSSLParameters(sslParameters)
		}
		return super.configure(sslEngine)
	}

}