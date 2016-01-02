package io.anaxo.net.ntlmproxy.http;


import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpParser extends InputStream {

	private static Logger log = LoggerFactory.getLogger(HttpParser.class);

	private static List<String> methods = Arrays.asList(new String[] {"GET", "HEAD", "POST"});

	private final InputStream is;
	private final byte[] buffer = new byte[4096];
	private int index;
	private String method, uri, protocol;
	private int bodyIndex;
	private int contentLength;
	private String contentType;
	private Header[] headers;

	public HttpParser(InputStream is) {
		this.is = is;
	}

	@Override
	public int read() throws IOException {
		if (bodyIndex < index)
			return buffer[bodyIndex++];
		return is.read();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (bodyIndex < index) {
			int toCopy = Math.min(len, index - bodyIndex);
			System.arraycopy(buffer, bodyIndex, b, off, toCopy);
			bodyIndex += toCopy;
			return toCopy;
		}
		return is.read(b, off, len);
	}

	public boolean parse() throws IOException, ParseException {
		log.debug("hee");
		index += is.read(buffer, index, buffer.length - index);
		String line = new String(buffer);
		log.debug(line);
		int splitAt = line.indexOf("\r\n\r\n");
		if (splitAt == -1)
			return false;
		bodyIndex = splitAt + 4;

		line = line.substring(0, splitAt);
		if (line.length() == 0)
			throw new IOException("Bad HTTP header");
		String[] headerLines = line.split("\r\n");
		if (headerLines.length == 0)
			throw new IOException("Bad HTTP header");
		String[] httpStuff = headerLines[0].split(" ");
		if (httpStuff.length != 3)
			throw new IOException("Bad HTTP header: " + httpStuff.length);

		method = httpStuff[0];
		uri = httpStuff[1];
		protocol = httpStuff[2];
		log.debug(method + " " + uri + " " + protocol);

		headers = new Header[0];

		headers = new Header[headerLines.length - 1];
		for (int i = 1; i < headerLines.length; i++) {
			String[] header = headerLines[i].split(": ", 2);
			if (header.length != 2)
				throw new IOException("Bad Header:" + headerLines[i]);
			Header h = headers[i - 1] = new BasicHeader(header[0], header[1]);
			log.debug(h.getName() + " - " + h.getValue());
			if (h.getName().equals("Content-Type"))
				this.contentType = h.getValue();
			else if (h.getName().equals("Content-Length"))
				this.contentLength =
						NumberFormat.getIntegerInstance().parse(h.getValue()).intValue();

		}

		return true;
	}


	public String getMethod() {
		return method;
	}

	public String getUri() {
		return uri;
	}

	public String getProtocol() {
		return protocol;
	}

	public int getLength() {
		return buffer.length;
	}

	public int getBodyIndex() {
		return bodyIndex;
	}

	public void setBodyIndex(int i) {
		this.bodyIndex = i;
	}


	public int getContentLength() {
		return contentLength;
	}

	public String getContentType() {
		return contentType;
	}

	public Header[] getHeaders() {
		return headers;
	}

	@Override
	public void close() throws IOException {
		is.close();
	}

	public byte[] getBuffer() {
		return buffer;
	}

	public int getIndex() {
		return index;
	}
}
