package io.anaxo.net.ntlmproxy.http.message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.entity.InputStreamEntity;

public class StreamingRequestEntity extends InputStreamEntity {

	public StreamingRequestEntity(HttpInfo httpInfo) throws IOException {
		super(new ByteArrayInputStream(httpInfo.getPayload()), httpInfo.getContentLength());
		setContentType(httpInfo.getContentType());
	}

}
