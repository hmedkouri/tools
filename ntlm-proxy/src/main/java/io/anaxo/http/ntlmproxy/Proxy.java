package io.anaxo.http.ntlmproxy;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.anaxo.http.ntlmproxy.connection.Connection;
import io.anaxo.http.ntlmproxy.connection.ConnectionManager;
import io.anaxo.http.ntlmproxy.processor.ClientRequestProcessor;
import io.anaxo.http.ntlmproxy.scheduler.ClientRequestScheduler;

public class Proxy {

	private static final Logger log = LoggerFactory.getLogger(Proxy.class);

	private volatile boolean isProxyAlive = true;

	private final ConnectionManager connectionManager;
	private final ClientRequestScheduler requestScheduler;
	private final ExecutorService mainExecutor;
	private final Clients clients;

	public Proxy(Properties props, int localPort) throws IOException {

		int threadCount = Integer.parseInt(props.getProperty("threadCount", "10"));
		int socketTimeoutInMilliseconds = Integer.parseInt(props.getProperty("timeout","10000"));
		this.connectionManager = new ConnectionManager(localPort, socketTimeoutInMilliseconds);
		this.requestScheduler = new ClientRequestScheduler(threadCount);
		this.mainExecutor = Executors.newSingleThreadExecutor();
		this.clients = new Clients(props);
	}

	public void start() {
		log.info("Http Proxy started...");
		mainExecutor.execute(new Runnable() {
			public void run() {
				while (isProxyAlive) {
					try {
						Connection connection = connectionManager.awaitClient();
						ClientRequestProcessor clientRequestProcessor = new ClientRequestProcessor(connection, clients);
						requestScheduler.schedule(clientRequestProcessor);						
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
        requestScheduler.shutDown();
    }
}