package io.anaxo.http.ntlmproxy.message;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.anaxo.http.ntlmproxy.connection.Connection;
import io.anaxo.http.ntlmproxy.utils.LineIterator;

public class HttpInfo {

	private static final Logger log = LoggerFactory.getLogger(HttpInfo.class);
	
	private HttpMethod method;
	private String uri;
	private String version;
	private Map<String, String> headers;
	private byte[] data;
	private int contentLength;
	private Header contentType;
	
	public HttpInfo(Connection connection) throws IOException {
		parse(connection);
	}
	
	public HttpInfo(String uri) throws IOException {
		this.uri = uri;
		this.method = HttpMethod.CONNECT;
		this.version = "HTTP/1.1";
	}
	
	public String getURI() {
		return uri;
	}

	public String getHttpVersion() {
		return version;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}
	
	public int getContentLength() {
		return contentLength;
	}

	public Header getContentType() {
		return contentType;
	}

	public byte[] getPayload() {
		return data;
	}

	public HttpMethod getMethod() {
		return method;
	}
	
	public boolean hasPayload() {
		return data != null;
	}

	public void setURI(String uri) {
		this.uri = uri;
	}
	
	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public void setHttpVersion(String version) {
		this.version = version;
	}

	public void setRequestHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public void setPayload(byte[] data) {
		this.data = data;
	}

	public int setContentLength(int contentLength) {
		return contentLength;
	}

	public Header setContentType(Header contentType) {
		return contentType;
	}
	
	private void parse(Connection connection) throws IOException {
		log.info("Extract request from connection");

		String line = connection.readLine();
		LineIterator lineIterator = new LineIterator(line);
		this.setMethod(HttpMethod.valueOf(lineIterator.next()));
		this.setURI(lineIterator.next());
		this.setHttpVersion(lineIterator.next());

		Map<String, String> headers = new HashMap<String, String>();
		String nextLine = "";
		while (!(nextLine = connection.readLine()).equals("")) {
			String values[] = nextLine.split(":", 2);
			headers.put(values[0], values[1].trim());
		}
		this.setRequestHeaders(headers);

		if (headers.containsKey(HTTP.CONTENT_LENGTH)) {
			int size = Integer.parseInt(headers.get(HTTP.CONTENT_LENGTH));
			byte[] data = new byte[size];
			int n;
			for (int i = 0; i < size && (n = connection.read()) != -1; i++) {
				data[i] = (byte) n;
			}
			this.setPayload(data);
		}
	}
}