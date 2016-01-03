package io.anaxo.http.ntlmproxy.processor;

import java.net.Socket;

import org.apache.http.impl.client.ProxyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.anaxo.http.ntlmproxy.Clients;
import io.anaxo.http.ntlmproxy.connection.Connection;
import io.anaxo.http.ntlmproxy.message.HttpInfo;
import io.anaxo.http.ntlmproxy.utils.Piper;

public class ClientConnectProcessor extends AbstractClientProcessor {

	private static final Logger log = LoggerFactory.getLogger(ClientConnectProcessor.class);
	
	private final Clients clients;

	public ClientConnectProcessor(Connection connection, Clients clients) {
		super(connection);
		this.clients = clients;
	}

	@Override
	public void execute(HttpInfo httpInfo)  throws Exception {
		ProxyClient client = new ProxyClient();
		try (Socket remoteSocket = client.tunnel(clients.getProxyHost(),
				clients.getTargetHost(httpInfo.getURI()), clients.getCredentials())) {
			new Thread(new Piper(getConnection().getInputStream(), remoteSocket.getOutputStream())).start();
			new Piper(remoteSocket.getInputStream(), getConnection().getOutputStream()).run();
		}
	}
}
