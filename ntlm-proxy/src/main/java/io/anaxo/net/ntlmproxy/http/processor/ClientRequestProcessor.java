package io.anaxo.net.ntlmproxy.http.processor;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.anaxo.net.ntlmproxy.http.Clients;
import io.anaxo.net.ntlmproxy.http.connection.Connection;
import io.anaxo.net.ntlmproxy.http.message.HttpInfo;
import io.anaxo.net.ntlmproxy.http.message.HttpMethod;
import io.anaxo.net.ntlmproxy.http.message.StreamingRequestEntity;
import io.anaxo.net.ntlmproxy.utils.Piper;

public class ClientRequestProcessor extends AbstractClientProcessor {

	private static final Logger log = LoggerFactory.getLogger(ClientRequestProcessor.class);

	private static List<String> stripHeadersIn = Arrays
			.asList(new String[] { "Content-Type", "Content-Length", "Proxy-Connection" });

	private static List<String> stripHeadersOut = Arrays
			.asList(new String[] { "Proxy-Authentication", "Proxy-Authorization" });

	private Connection connection;
	private Clients clients;

	public ClientRequestProcessor(Connection connection, Clients clients) {
		super(connection);
		this.clients = clients;
	}

	public void execute(HttpInfo httpInfo) throws Exception {

		HttpRequestBase method = getHttpRequestBase(httpInfo);
		if (httpInfo.hasPayload()) {
			((HttpEntityEnclosingRequestBase) method).setEntity(new StreamingRequestEntity(httpInfo));
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
		connection.shutdownInput();
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

	private HttpRequestBase getHttpRequestBase(HttpInfo httpInfo) throws Exception {
		HttpMethod method = httpInfo.getMethod();
		switch (method) {
		case GET:
			return new HttpGet();
		case POST:
			return new HttpPost();
		case PUT:
			return new HttpPut();
		case DELETE:
			return new HttpDelete();
		case HEAD:
			return new HttpHead();
		default:
			throw new Exception("Unknown http request method: " + method);
		}
	}
}