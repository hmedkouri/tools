package io.anaxo.net.ntlmproxy.http.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.anaxo.net.ntlmproxy.http.Clients;
import io.anaxo.net.ntlmproxy.http.connection.Connection;
import io.anaxo.net.ntlmproxy.http.message.HTTP;
import io.anaxo.net.ntlmproxy.http.message.HttpMethod;
import io.anaxo.net.ntlmproxy.utils.Piper;

public class ClientRequestProcessor {

	private static final Logger log = LoggerFactory.getLogger(ClientRequestProcessor.class);
	
	private static List<String> stripHeadersIn =
			Arrays.asList(new String[] {"Content-Type", "Content-Length", "Proxy-Connection"});

	private static List<String> stripHeadersOut =
			Arrays.asList(new String[] {"Proxy-Authentication", "Proxy-Authorization"});
	
	private Connection connection;
	private Clients clients;

	public ClientRequestProcessor(Connection connection, Clients clients) {
		this.connection = connection;
		this.clients = clients;
	}

	public void process() {
		while (!Thread.interrupted() && connection.isOpen()) {
			try {
				
				HttpInfo httpInfo = parseConnection();
				System.out.println("New request received." + httpInfo.getMethod() + " " + httpInfo.getURI() + " "
						+ httpInfo.getHttpVersion());
				
				execute(httpInfo);

				closeConnectionIfItIsNotPersistent(httpInfo);

			} catch (Exception e) {
				if (e instanceof SocketTimeoutException) {
					System.out.println("Read time out occurred closing connection");
				} else {}
				connection.disconnect();
			}
		}
	}

	private HttpInfo parseConnection() throws IOException {
		System.out.println("Extract request from connection");
		HttpInfo httpInfo = new HttpInfo();

		String line = connection.readLine();
		LineIterator lineIterator = new LineIterator(line);
		httpInfo.setMethod(HttpMethod.valueOf(lineIterator.next()));
		httpInfo.setURI(lineIterator.next());
		httpInfo.setHttpVersion(lineIterator.next());

		Map<String, String> headers = new HashMap<String, String>();
		String nextLine = "";
		while (!(nextLine = connection.readLine()).equals("")) {
			String values[] = nextLine.split(":", 2);
			headers.put(values[0], values[1].trim());
		}
		httpInfo.setRequestHeaders(headers);

		if (headers.containsKey(HTTP.CONTENT_LENGTH)) {
			int size = Integer.parseInt(headers.get(HTTP.CONTENT_LENGTH));
			byte[] data = new byte[size];
			int n;
			for (int i = 0; i < size && (n = connection.read()) != -1; i++) {
				data[i] = (byte) n;
			}
			httpInfo.setPayload(data);
		}

		return httpInfo;
	}

	private void closeConnectionIfItIsNotPersistent(HttpInfo httpInfo) {
		if (httpInfo.getHttpVersion().equalsIgnoreCase(HTTP.VERSION)
				&& httpInfo.getHeaders().containsKey(HTTP.CONNECTION)) {
			System.out.println("Not going to close connection due to persistent properties...");
		} else {
			connection.disconnect();
		}
	}
	
	private void execute(HttpInfo httpInfo) throws Exception {

		HttpRequestBase method = new HttpGet();
		if (httpInfo.hasPayload()) {
			((HttpEntityEnclosingRequestBase)method).setEntity(new StreamingRequestEntity(httpInfo));
		}

		URI uri = new URI(httpInfo.getURI());
		method.setURI(uri);
		RequestConfig config = RequestConfig.custom().setRedirectsEnabled(false)
				.setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
		method.setConfig(config);

		for (String key : httpInfo.getHeaders().keySet()) {
			String value = httpInfo.getHeaders().get(key);
			log.debug(key);
			if (stripHeadersIn.contains(key))
				continue;
			method.addHeader(new BasicHeader(key, value));
		}

		HttpClient client = clients.getClient(httpInfo.getURI());

		HttpResponse response = client.execute(method);
		//connection.shutdownInput();
		OutputStream os = connection.getOutputStream();
		os.write(response.getStatusLine().toString().getBytes());
		os.write("\r\n".getBytes());
		log.debug(response.getStatusLine().toString());
		Header[] headers = response.getAllHeaders();
		for (int i = 0; i < headers.length; i++) {
			if (stripHeadersOut.contains(headers[i]))
				continue;
			os.write(headers[i].toString().getBytes());
			log.debug(headers[i].toString());
		}

		InputStream is = response.getEntity().getContent();
		if (is != null) {
			os.write("\r\n".getBytes());
			new Piper(is, os).run();
		}
	}
}