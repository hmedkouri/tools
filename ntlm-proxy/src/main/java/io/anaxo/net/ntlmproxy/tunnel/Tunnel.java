package io.anaxo.net.ntlmproxy.tunnel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.auth.NTCredentials;
import org.apache.http.impl.client.ProxyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.anaxo.net.ntlmproxy.Main;
import io.anaxo.net.ntlmproxy.utils.InetUtils;
import io.anaxo.net.ntlmproxy.utils.Piper;

public class Tunnel extends Thread {

  private static final Logger log = LoggerFactory.getLogger(Tunnel.class);

  private final ServerSocket serverSocket;
  private final String remoteHost;
  private final int remotePort;
  private final Properties props;

  public Tunnel(Properties props, int localPort, String remoteHost, int remotePort)
      throws IOException {
    serverSocket = new ServerSocket(localPort);
    this.remotePort = remotePort;
    this.remoteHost = remoteHost;
    this.props = props;
  }

  class Handler extends Thread {

    private final Socket localSocket;

    public Handler(Socket localSocket) {
      this.localSocket = localSocket;
    }

    @Override
    public void run() {
      ProxyClient client = new ProxyClient();

      String proxyHost = props.getProperty(Main.PROXY_DELEGATE_HOST_NAME);
      int proxyPort = Integer.parseInt(props.getProperty(Main.PROXY_DELEGATE_HOST_PORT));
      HttpHost proxy = new HttpHost(proxyHost, proxyPort);
      HttpHost target = new HttpHost(remoteHost, remotePort);

      String userName = props.getProperty(Main.PROXY_DELEGATE_USERNAME);
      String password = props.getProperty(Main.PROXY_DELEGATE_PASSWORD);
      String hostName = InetUtils.getHostName();
      String domain = props.getProperty(Main.PROXY_DELEGATE_DOMAIN);
      NTCredentials credentials = new NTCredentials(userName, password, hostName, domain);

      try (Socket remoteSocket = client.tunnel(proxy, target, credentials)) {
        new Thread(new Piper(localSocket.getInputStream(), remoteSocket.getOutputStream())).start();
        new Piper(remoteSocket.getInputStream(), localSocket.getOutputStream()).run();
      } catch (IOException | HttpException e) {
        log.error(e.getMessage(), e);
      }
    }
  }

  @Override
  public void run() {
    while (true) {
      try {
        Socket s = serverSocket.accept();
        new Handler(s).start();
      } catch (IOException e) {
        log.error(e.getMessage(), e);
        break;
      }
    }
  }

  public void close() throws IOException {
    serverSocket.close();
  }
}
