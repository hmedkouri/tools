package io.anaxo.net.ntlmproxy.proxy;

import java.util.Properties;

import org.apache.http.HttpHost;
import org.apache.http.auth.NTCredentials;

import io.anaxo.net.ntlmproxy.Main;
import io.anaxo.net.ntlmproxy.utils.InetUtils;

public class EndPoints {

	private final Properties props;
	private final HttpParser parser;

	public EndPoints(Properties props, HttpParser parser) {
		this.props = props;
		this.parser = parser;
	}

	public HttpHost getProxyHost() {
		String proxyHost = props.getProperty(Main.PROXY_DELEGATE_HOST_NAME);
		int proxyPort = Integer.parseInt(props.getProperty(Main.PROXY_DELEGATE_HOST_PORT));
		return new HttpHost(proxyHost, proxyPort);
	}

	public HttpHost getTargetHost() {
		String[] uri = parser.getUri().split(":");
		String remoteHost = uri[0];
		int remotePort = Integer.parseInt(uri[1]);
		return new HttpHost(remoteHost, remotePort);
	}

	public NTCredentials getCredentials() {
		String userName = props.getProperty(Main.PROXY_DELEGATE_USERNAME);
		String password = props.getProperty(Main.PROXY_DELEGATE_PASSWORD);
		String hostName = InetUtils.getHostName();
		String domain = props.getProperty(Main.PROXY_DELEGATE_DOMAIN);
		return new NTCredentials(userName, password, hostName, domain);
	}
}
