package io.anaxo.net.ntlmproxy.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.anaxo.net.ntlmproxy.http.connection.Connection;
import io.anaxo.net.ntlmproxy.http.connection.ConnectionManager;
import io.anaxo.net.ntlmproxy.http.handlers.Handler;
import io.anaxo.net.ntlmproxy.http.handlers.HttpConnectHandler;
import io.anaxo.net.ntlmproxy.http.handlers.HttpGetHandler;
import io.anaxo.net.ntlmproxy.http.handlers.HttpHeadHandler;
import io.anaxo.net.ntlmproxy.http.handlers.HttpPostHandler;

public class Proxy {

	private static final Logger log = LoggerFactory.getLogger(Proxy.class);

	private volatile boolean isProxyAlive = true;

	private final ConnectionManager connectionManager;
	private final ExecutorService mainExecutor;
	private final ExecutorService threadPool = Executors.newCachedThreadPool();
	private final ServerSocket ssocket;
	private final Properties props;
	private final Clients clients;

	public Proxy(Properties props, int localPort) throws IOException {
		ssocket = new ServerSocket(localPort);
		this.props = props;
		this.clients = new Clients(props);

		int socketTimeoutInMilliseconds = Integer.parseInt(props.getProperty("timeout"));
		connectionManager = new ConnectionManager(localPort, socketTimeoutInMilliseconds);
		mainExecutor = Executors.newSingleThreadExecutor();
	}

	public void start() {
		log.info("Http Proxy started...");
		threadPool.execute(new Runnable() {
			public void run() {
				while (isProxyAlive) {
					try {
						Connection connection = connectionManager.awaitClient();
						Socket localSocket = ssocket.accept();
						Handler handler = getHandler(localSocket);
						threadPool.execute(handler);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
						break;
					}
				}
			}
		});
	}
	
	public void stop() {
        connectionManager.shutDown();
        mainExecutor.shutdown();
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
		while (!parser.parse())
			;
		return parser;
	}
}
