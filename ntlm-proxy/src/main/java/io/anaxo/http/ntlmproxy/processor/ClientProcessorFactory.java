package io.anaxo.http.ntlmproxy.processor;

import java.io.IOException;
import java.util.Properties;

import io.anaxo.http.ntlmproxy.Clients;
import io.anaxo.http.ntlmproxy.connection.Connection;
import io.anaxo.http.ntlmproxy.message.HttpInfo;
import io.anaxo.http.ntlmproxy.message.HttpMethod;

public class ClientProcessorFactory {

	public static ClientProcessor getClientProcessor(Connection connection, Properties properties) throws IOException {
		HttpInfo httpInfo = new HttpInfo(connection);
		return getClientProcessor(connection, properties, httpInfo);
	}
	
	public static ClientProcessor getClientProcessor(Connection connection, Properties properties, String uri) throws IOException {
		HttpInfo httpInfo = new HttpInfo(uri);
		return getClientProcessor(connection, properties, httpInfo);
	}
	
	private static ClientProcessor getClientProcessor(Connection connection, Properties properties, HttpInfo httpInfo) throws IOException {
		Clients clients = new Clients(properties);
		if (HttpMethod.CONNECT.equals(httpInfo.getMethod())) {
			return new ClientConnectProcessor(connection, httpInfo, clients);
		} else {
			return new ClientRequestProcessor(connection, httpInfo, clients);
		}
	}
}
