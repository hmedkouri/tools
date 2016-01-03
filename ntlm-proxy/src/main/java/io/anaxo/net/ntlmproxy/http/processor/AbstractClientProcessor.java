package io.anaxo.net.ntlmproxy.http.processor;

import java.net.SocketTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.anaxo.net.ntlmproxy.http.connection.Connection;
import io.anaxo.net.ntlmproxy.http.message.HTTP;
import io.anaxo.net.ntlmproxy.http.message.HttpInfo;

public abstract class AbstractClientProcessor implements ClientProcessor {

	private static final Logger log = LoggerFactory.getLogger(AbstractClientProcessor.class);
	
	private final Connection connection;
	
	public AbstractClientProcessor(Connection connection) {
		this.connection = connection;
	}
	
	public void process() {
		while (!Thread.interrupted() && connection.isOpen()) {
			try {

				HttpInfo httpInfo = new HttpInfo(connection);

				log.info("New request received." + httpInfo.getMethod() + " " + httpInfo.getURI() + " "
						+ httpInfo.getHttpVersion());

				execute(httpInfo);

				closeConnectionIfItIsNotPersistent(httpInfo);

			} catch (Exception e) {
				if (e instanceof SocketTimeoutException) {
					log.error("Read time out occurred closing connection");
				} else {
				}
				connection.disconnect();
			}
		}
	}
	
	public abstract void execute(HttpInfo httpInfo) throws Exception;
	
	private void closeConnectionIfItIsNotPersistent(HttpInfo httpInfo) {
		if (httpInfo.getHttpVersion().equalsIgnoreCase(HTTP.VERSION)
				&& httpInfo.getHeaders().containsKey(HTTP.CONNECTION)) {
			log.info("Not going to close connection due to persistent properties...");
		} else {
			connection.disconnect();
		}
	}
	
	public Connection getConnection() {
		return connection;
	}
}
