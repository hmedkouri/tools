package io.anaxo.net.ntlmproxy.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.anaxo.net.ntlmproxy.proxy.Clients;
import io.anaxo.net.ntlmproxy.proxy.EndPoints;
import io.anaxo.net.ntlmproxy.proxy.HttpParser;
import io.anaxo.net.ntlmproxy.tunnel.Piper;

public abstract class Handler implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(Handler.class);

	private static List<String> stripHeadersIn =
			Arrays.asList(new String[] {"Content-Type", "Content-Length", "Proxy-Connection"});

	private static List<String> stripHeadersOut =
			Arrays.asList(new String[] {"Proxy-Authentication", "Proxy-Authorization"});

	protected final Socket localSocket;
	private final Clients clients;
	private final EndPoints endPoints;
	private final HttpParser parser;

	public Handler(Socket localSocket, Clients clients, EndPoints endPoints,
			HttpParser parser) {
		this.localSocket = localSocket;
		this.clients = clients;
		this.endPoints = endPoints;
		this.parser = parser;
	}

	protected void execute(HttpRequestBase method)
			throws URISyntaxException, IOException, ClientProtocolException {

		if (method instanceof HttpEntityEnclosingRequestBase) {
			HttpEntityEnclosingRequestBase method2 = (HttpEntityEnclosingRequestBase) method;
			method2.setEntity(new StreamingRequestEntity(parser));
		}

		URI uri = new URI(parser.getUri());
		method.setURI(uri);
		RequestConfig config = RequestConfig.custom().setRedirectsEnabled(false)
				.setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
		method.setConfig(config);

		for (int i = 0; i < parser.getHeaders().length; i++) {
			Header h = parser.getHeaders()[i];
			log.debug(h.getName());
			if (stripHeadersIn.contains(h.getName()))
				continue;
			method.addHeader(h);
		}

		HttpClient client = clients.getClient(parser.getUri());

		HttpResponse response = client.execute(method);
		localSocket.shutdownInput();
		OutputStream os = localSocket.getOutputStream();
		os.write(response.getStatusLine().toString().getBytes());
		os.write("\r\n".getBytes());
		log.debug(response.getStatusLine().toString());
		Header[] headers = response.getAllHeaders();
		for (int i = 0; i < headers.length; i++) {
			if (stripHeadersOut.contains(headers[i]))
				continue;
			os.write(headers[i].toString().getBytes());
			log.debug(headers[i].toString());
		}

		InputStream is = response.getEntity().getContent();
		if (is != null) {
			os.write("\r\n".getBytes());
			new Piper(is, os).run();
		}
	}
}
