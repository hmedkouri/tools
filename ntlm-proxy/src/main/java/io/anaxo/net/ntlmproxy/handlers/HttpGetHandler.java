package io.anaxo.net.ntlmproxy.handlers;

import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.anaxo.net.ntlmproxy.proxy.Clients;
import io.anaxo.net.ntlmproxy.proxy.EndPoints;
import io.anaxo.net.ntlmproxy.proxy.HttpParser;

public class HttpGetHandler extends Handler {

	private static final Logger log = LoggerFactory.getLogger(HttpGetHandler.class);

	public HttpGetHandler(Socket localSocket, Clients clients, EndPoints endPoints,
			HttpParser parser) {
		super(localSocket, clients, endPoints, parser);
	}

	@Override
	public void run() {
		HttpGet method = null;
		try {
			method = new HttpGet();
			execute(method);
		} catch (URISyntaxException | IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (method != null) {
				method.releaseConnection();
			}
		}
	}
}
