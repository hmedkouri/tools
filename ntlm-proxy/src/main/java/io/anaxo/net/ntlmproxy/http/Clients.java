package io.anaxo.net.ntlmproxy.http;

import java.util.Properties;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import io.anaxo.net.ntlmproxy.Main;
import io.anaxo.net.ntlmproxy.utils.InetUtils;

public class Clients {

	private final HttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
	private final Properties props;

	public Clients(Properties props) {
		this.props = props;
	}

	public HttpClient getClient(String uri) {
		return (Main.noForwardPattern != null && Main.noForwardPattern.matcher(uri).find())
				? getNoDelegateClient() : getDelegateClient();
	}
	
	public HttpHost getProxyHost() {
		String proxyHost = props.getProperty(Main.PROXY_DELEGATE_HOST_NAME);
		int proxyPort = Integer.parseInt(props.getProperty(Main.PROXY_DELEGATE_HOST_PORT));
		return new HttpHost(proxyHost, proxyPort);
	}

	public HttpHost getTargetHost(String uri) {
		String[] u = uri.split(":");
		String remoteHost = u[0];
		int remotePort = Integer.parseInt(u[1]);
		return new HttpHost(remoteHost, remotePort);
	}

	public NTCredentials getCredentials() {
		String userName = props.getProperty(Main.PROXY_DELEGATE_USERNAME);
		String password = props.getProperty(Main.PROXY_DELEGATE_PASSWORD);
		String hostName = InetUtils.getHostName();
		String domain = props.getProperty(Main.PROXY_DELEGATE_DOMAIN);
		return new NTCredentials(userName, password, hostName, domain);
	}

	private HttpClient getDelegateClient() {
		String proxyHost = props.getProperty(Main.PROXY_DELEGATE_HOST_NAME);
		int proxyPort = Integer.parseInt(props.getProperty(Main.PROXY_DELEGATE_HOST_PORT));
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY, getCredentials());
		return HttpClients.custom().setConnectionManager(manager)
				.setProxy(new HttpHost(proxyHost, proxyPort))
				.setDefaultCredentialsProvider(credentialsProvider)
				.build();
	}

	private HttpClient getNoDelegateClient() {
		return HttpClientBuilder.create().setConnectionManager(manager).build();
	}
}
