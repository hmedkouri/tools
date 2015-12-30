package io.anaxo.net.ntlmproxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.anaxo.net.ntlmproxy.handlers.Handler;
import io.anaxo.net.ntlmproxy.handlers.HttpConnectHandler;
import io.anaxo.net.ntlmproxy.handlers.HttpGetHandler;
import io.anaxo.net.ntlmproxy.handlers.HttpHeadHandler;
import io.anaxo.net.ntlmproxy.handlers.HttpPostHandler;

public class HttpForwarder extends Thread {

	private static final Logger log = LoggerFactory.getLogger(HttpForwarder.class);

	private final ExecutorService threadPool = Executors.newCachedThreadPool();
	private final ServerSocket ssocket;
	private final Properties props;
	private final Clients clients;

	public HttpForwarder(Properties props, int localPort) throws IOException {
		ssocket = new ServerSocket(localPort);
		this.props = props;
		this.clients = new Clients(props);
	}

	@Override
	public void run() {
		while (true) {
			try {
				Socket localSocket = ssocket.accept();
				Handler handler = getHandler(localSocket);
				threadPool.execute(handler);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				break;
			}
		}
	}

	private Handler getHandler(Socket localSocket) throws Exception {
		HttpParser parser = getParser(localSocket);
		String method = parser.getMethod();
		EndPoints endPoints = new EndPoints(props, parser);
		if (method.equals("GET")) {
			return new HttpGetHandler(localSocket, clients, endPoints, parser);
		} else if (method.equals("POST")) {
			return new HttpPostHandler(localSocket, clients, endPoints, parser);
		} else if (method.equals("HEAD")) {
			return new HttpHeadHandler(localSocket, clients, endPoints, parser);
		} else if (method.equals("CONNECT")) {
			return new HttpConnectHandler(localSocket, clients, endPoints, parser);
		} else {
			throw new Exception("Unknown method: " + parser.getMethod());
		}
	}

	public void close() throws IOException {
		ssocket.close();
	}

	private HttpParser getParser(Socket localSocket) throws IOException, ParseException {
		HttpParser parser = new HttpParser(localSocket.getInputStream());
		while (!parser.parse());
		return parser;
	}
}
