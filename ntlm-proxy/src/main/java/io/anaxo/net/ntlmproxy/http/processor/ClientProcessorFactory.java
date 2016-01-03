package io.anaxo.net.ntlmproxy.http.processor;

import java.io.IOException;
import java.util.Properties;

import io.anaxo.net.ntlmproxy.http.Clients;
import io.anaxo.net.ntlmproxy.http.connection.Connection;
import io.anaxo.net.ntlmproxy.http.message.HttpInfo;
import io.anaxo.net.ntlmproxy.http.message.HttpMethod;

public class ClientProcessorFactory {

	public static ClientProcessor getClientProcessor(Connection connection, Properties properties) throws IOException {
		Clients clients = new Clients(properties);
		HttpInfo httpInfo = new HttpInfo(connection);
		if (HttpMethod.CONNECT.equals(httpInfo.getMethod())) {
			return new ClientConnectProcessor(connection, clients);
		} else {
			return new ClientRequestProcessor(connection, clients);
		}
	}
}
