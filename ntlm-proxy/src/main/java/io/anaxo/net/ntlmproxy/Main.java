package io.anaxo.net.ntlmproxy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.anaxo.net.ntlmproxy.http.Proxy;
import io.anaxo.net.ntlmproxy.tunnel.Tunnel;

public class Main {

	public static String PROPS_FILE = "/ntlm-proxy.properties";

	public static String PROXY_PORT = "proxy.port";

	public static String PROXY_DELEGATE_HOST_NAME = "delegate.host.name";

	public static String PROXY_DELEGATE_HOST_PORT = "delegate.host.port";

	public static String PROXY_DELEGATE_USERNAME = "delegate.username";

	public static String PROXY_DELEGATE_PASSWORD = "delegate.password";

	public static String PROXY_DELEGATE_DOMAIN = "delegate.domain";

	public static String PROXY_FORWARD = "proxy.forward";

	public static String PROXY_NO_DELEGATE = "proxy.nodelegate";

	public static String PROXY_LOG_WIRE = "proxy.log.wire";

	public static String PROXY_LOG_DISABLE = "proxy.log.disable";

	static final Logger log = LoggerFactory.getLogger(Main.class);

	public static Pattern noForwardPattern;

	public static void main(String[] args) {
		System.out.println("Starting NTLM proxy");
		log.info("Starting NTLM proxy");
		try {

			Properties props = loadProperties();

			changeLogLevel(props);

			setNoForwardPattern(props);

			startHttpProxy(props);

			startTunnels(props);
			
			System.out.println("Running...");
			log.info("Running...");
		} catch (Throwable e) {
			log.error(e.getMessage());
			System.err.println("FATAL: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void setNoForwardPattern(Properties props) {
		String noForward = props.getProperty(Main.PROXY_NO_DELEGATE);
		if (noForward != null) {
			log.info("No delegate for: " + noForward);
			noForwardPattern = Pattern.compile(noForward);
		}
	}

	private static void startHttpProxy(Properties props) throws IOException {
		int proxyPort = Integer.parseInt(props.getProperty(Main.PROXY_PORT));
		Proxy proxy = new Proxy(props, proxyPort);
		proxy.start();
	}

	private static void startTunnels(Properties props) throws Exception, IOException {
		String forwardString = props.getProperty(Main.PROXY_FORWARD);
		if (forwardString != null) {
			String[] forwards = forwardString.split(",");
			Pattern pattern = Pattern.compile("(\\d+):([^:]+):(\\d+)");
			for (int i = 0; i < forwards.length; i++) {
				Matcher m = pattern.matcher(forwards[i]);
				if (!m.matches()) {
					log.error("Forward format is localport:remotehost:remoteport, got "
							+ forwards[i]);
					throw new Exception(
							"Forward format is localport:remotehost:remoteport, got "
									+ forwards[i]);
				}
				log.info("Forwarding port: " + forwards[i]);
				new Tunnel(props, Integer.parseInt(m.group(1)), m.group(2),
						Integer.parseInt(m.group(3))).start();
			}
		}
	}

	private static void changeLogLevel(Properties props) {
		if (props.getProperty(Main.PROXY_LOG_DISABLE, "false").equals("true")) {
			changeLogLevel(Level.OFF);
		} else {
			System.out.println("Logging to ntlm-proxy.log");
			log.info("Logging to ntlm-proxy.log");
		}
		if (props.getProperty(Main.PROXY_LOG_WIRE, "false").equals("true")) {
			changeLogLevel(Level.DEBUG, "httpclient.wire");
		}
	}

	public static void changeLogLevel(Level level) {
		changeLogLevel(level, LogManager.ROOT_LOGGER_NAME);
	}

	public static void changeLogLevel(Level level, String loggerName) {
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration conf = ctx.getConfiguration();
		conf.getLoggerConfig(loggerName).setLevel(level);
		ctx.updateLoggers(conf);
	}
	
	private static Properties loadProperties() {
        try {
        	Properties properties = new Properties();
            InputStream is = Main.class.getResourceAsStream(PROPS_FILE);
            properties.load(is);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
