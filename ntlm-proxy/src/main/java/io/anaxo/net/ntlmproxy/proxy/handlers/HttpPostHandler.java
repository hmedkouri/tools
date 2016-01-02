package io.anaxo.net.ntlmproxy.proxy.handlers;

import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.anaxo.net.ntlmproxy.proxy.Clients;
import io.anaxo.net.ntlmproxy.proxy.EndPoints;
import io.anaxo.net.ntlmproxy.proxy.HttpParser;

public class HttpPostHandler extends Handler {

	private static final Logger log = LoggerFactory.getLogger(HttpPostHandler.class);

	public HttpPostHandler(Socket localSocket, Clients clients, EndPoints endPoints,
			HttpParser parser) {
		super(localSocket, clients, endPoints, parser);
	}

	@Override
	public void run() {
		HttpPost method = null;
		try {
			method = new HttpPost();
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
