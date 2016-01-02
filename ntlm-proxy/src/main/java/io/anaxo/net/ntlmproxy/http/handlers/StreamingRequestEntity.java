package io.anaxo.net.ntlmproxy.http.handlers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.entity.InputStreamEntity;

import io.anaxo.net.ntlmproxy.http.HttpParser;

public class StreamingRequestEntity extends InputStreamEntity {

  public StreamingRequestEntity(HttpParser parser) throws IOException {
    super(getStream(parser), parser.getContentLength());
    setContentType(parser.getContentType());
  }

  private static InputStream getStream(HttpParser parser) throws IOException {
    byte[] repeatable = null;
    if (repeatable == null) {
      long length = parser.getContentLength();
      repeatable = new byte[(int) length];
      for (int i = 0; i < length; i++)
        repeatable[i] = (byte) parser.read();
    }
    ByteArrayInputStream stream = new ByteArrayInputStream(repeatable);
    return stream;
  }
}
