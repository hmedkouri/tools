package io.anaxo.http.ntlmproxy;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.anaxo.http.ntlmproxy.connection.Connection;
import io.anaxo.http.ntlmproxy.connection.ConnectionManager;
import io.anaxo.http.ntlmproxy.processor.ClientProcessor;
import io.anaxo.http.ntlmproxy.processor.ClientProcessorFactory;
import io.anaxo.http.ntlmproxy.scheduler.ClientRequestScheduler;

public class Tunnel {

	private static final Logger log = LoggerFactory.getLogger(Tunnel.class);

	private volatile boolean isTunnelAlive = true;
	
	private final ConnectionManager connectionManager;
	private final ClientRequestScheduler requestScheduler;
	private final ExecutorService mainExecutor;
	private final Properties properties;
	private final String uri;

	public Tunnel(Properties properties, int localPort, String remoteHost, int remotePort) throws IOException {
		int threadCount = Integer.parseInt(properties.getProperty("threadCount", "10"));
		int socketTimeoutInMilliseconds = Integer.parseInt(properties.getProperty("timeout", "10000"));
		this.connectionManager = new ConnectionManager(localPort, socketTimeoutInMilliseconds);
		this.requestScheduler = new ClientRequestScheduler(threadCount);
		this.uri = remoteHost + ":" + remotePort;
		this.mainExecutor = Executors.newSingleThreadExecutor();
		this.properties = properties;
	}

	public void start() {
		log.info("Http Tunnel started...");
		mainExecutor.execute(new Runnable() {
			public void run() {
				while (isTunnelAlive) {
					try {
						Connection connection = connectionManager.awaitClient();
						ClientProcessor clientProcessor = ClientProcessorFactory.getClientProcessor(connection, properties, uri) ;
						requestScheduler.schedule(clientProcessor);						
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