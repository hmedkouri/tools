package io.anaxo.http.ntlmproxy.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.lang3.StringUtils;

public class InetUtils {

  public static String getHostName() {
    try {
      String result = InetAddress.getLocalHost().getHostName();
      if (StringUtils.isNotEmpty(result))
        return result;
    } catch (UnknownHostException e) {
      // failed; try alternate means.
    }

    String host = System.getenv("COMPUTERNAME");
    if (host != null)
      return host;
    host = System.getenv("HOSTNAME");
    if (host != null)
      return host;

    // undetermined.
    return null;
  }

}
