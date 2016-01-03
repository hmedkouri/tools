package io.anaxo.net.ntlmproxy.http.processor;

import java.util.Map;

import org.apache.http.Header;
import org.apache.http.entity.ContentType;

import io.anaxo.net.ntlmproxy.http.message.HttpMethod;

public class HttpInfo {

	private HttpMethod method;
	private String uri;
	private String version;
	private Map<String, String> headers;
	private byte[] data;
	private int contentLength;
	private Header contentType;
	
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
}