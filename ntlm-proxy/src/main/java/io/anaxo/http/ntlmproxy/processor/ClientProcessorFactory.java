package io.anaxo.http.ntlmproxy.processor;

import java.io.IOException;
import java.util.Properties;

import io.anaxo.http.ntlmproxy.Clients;
import io.anaxo.http.ntlmproxy.connection.Connection;
import io.anaxo.http.ntlmproxy.message.HttpInfo;
import io.anaxo.http.ntlmproxy.message.HttpMethod;

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
