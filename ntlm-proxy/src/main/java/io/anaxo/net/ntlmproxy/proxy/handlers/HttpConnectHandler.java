package io.anaxo.net.ntlmproxy.proxy.handlers;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpException;
import org.apache.http.impl.client.ProxyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.anaxo.net.ntlmproxy.proxy.Clients;
import io.anaxo.net.ntlmproxy.proxy.EndPoints;
import io.anaxo.net.ntlmproxy.proxy.HttpParser;
import io.anaxo.net.ntlmproxy.utils.Piper;

public class HttpConnectHandler extends Handler {

	private static final Logger log = LoggerFactory.getLogger(HttpConnectHandler.class);

	private final ExecutorService threadPool = Executors.newCachedThreadPool();
	private final EndPoints endPoints;
	private final HttpParser parser;

	public HttpConnectHandler(Socket localSocket, Clients clients, EndPoints endPoints,
			HttpParser parser) {
		super(localSocket, clients, endPoints, parser);
		this.endPoints = endPoints;
		this.parser = parser;
	}

	@Override
	public void run() {
		ProxyClient client = new ProxyClient();
		try (Socket remoteSocket = client.tunnel(endPoints.getProxyHost(),
				endPoints.getTargetHost(), endPoints.getCredentials())) {
			threadPool.execute(new Piper(parser, remoteSocket.getOutputStream()));
			new Piper(remoteSocket.getInputStream(), localSocket.getOutputStream()).run();
		} catch (IOException | HttpException e) {
			log.error(e.getMessage(), e);
		}
	}
}
