package io.anaxo.http.ntlmproxy.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Piper implements Runnable {

  private static Logger log = LoggerFactory.getLogger(Piper.class);

  private InputStream is;
  private OutputStream os;
  private byte[] buffer = new byte[1500];

  public Piper(InputStream is, OutputStream os) {
    this.is = is;
    this.os = os;
  }

  public Piper() {}

  public void run() {
    try {
      while (true) {
        int read = is.read(buffer);
        if (read == -1)
          break;
        os.write(buffer, 0, read);
      }
    } catch (IOException e) {
      log.debug(e.getMessage(), e);
    }
    close();

  }

  public InputStream getIs() {
    return is;
  }

  public void setIs(InputStream is) {
    this.is = is;
  }

  public OutputStream getOs() {
    return os;
  }

  public void setOs(OutputStream os) {
    this.os = os;
  }

  public void close() {
    try {
      is.close();
    } catch (Exception ex) {
      log.debug(ex.getMessage(), ex);
    }
    try {
      os.close();
    } catch (Exception ex) {
      log.debug(ex.getMessage(), ex);
    }
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    Runtime rt = Runtime.getRuntime();
    // Start two processes: ps ax | grep rbe
    Process p1 = rt.exec("ps ax");
    // grep will wait for input on stdin
    Process p2 = rt.exec("grep rbe");
    // Create and start Piper
    Piper pipe = new Piper(p1.getInputStream(), p2.getOutputStream());
    new Thread(pipe).start();
    // Wait for second process to finish
    p2.waitFor();
    // Show output of second process
    BufferedReader r = new BufferedReader(new InputStreamReader(p2.getInputStream()));
    String s = null;
    while ((s = r.readLine()) != null) {
      System.out.println(s);
    }
  }

}
