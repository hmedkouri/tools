package io.anaxo.net.ntlmproxy.proxy;

import java.util.Properties;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import io.anaxo.net.ntlmproxy.Main;

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

	private HttpClient getDelegateClient() {
		String proxyHost = props.getProperty(Main.PROXY_DELEGATE_HOST_NAME);
		int proxyPort = Integer.parseInt(props.getProperty(Main.PROXY_DELEGATE_HOST_PORT));
		return HttpClients.custom().setConnectionManager(manager)
				.setProxy(new HttpHost(proxyHost, proxyPort)).build();
	}

	private HttpClient getNoDelegateClient() {
		return HttpClientBuilder.create().setConnectionManager(manager).build();
	}
}
