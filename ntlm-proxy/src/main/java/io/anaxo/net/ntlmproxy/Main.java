package io.anaxo.net.ntlmproxy;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	public static String PROPS_FILE = "ntlm-proxy.properties";

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

			Properties props = new Properties();
			props.load(Main.class.getResourceAsStream("/" + PROPS_FILE));

			if (props.getProperty(Main.PROXY_LOG_DISABLE, "false").equals("true")) {
				changeLogLevel(Level.OFF);
			} else {
				System.out.println("Logging to ntlm-proxy.log");
				log.info("Logging to ntlm-proxy.log");
			}
			if (props.getProperty(Main.PROXY_LOG_WIRE, "false").equals("true")) {
				changeLogLevel(Level.DEBUG, "httpclient.wire");
			}

			String noForward = props.getProperty(Main.PROXY_NO_DELEGATE);
			if (noForward != null) {
				log.info("No delegate for: " + noForward);
				noForwardPattern = Pattern.compile(noForward);
			}

			String forwardString = props.getProperty(Main.PROXY_FORWARD);
			new HttpForwarder(props, Integer.parseInt(props.getProperty(Main.PROXY_PORT))).start();

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
					new Forwarder(props, Integer.parseInt(m.group(1)), m.group(2),
							Integer.parseInt(m.group(3))).start();
				}
			}
			System.out.println("Running...");
			log.info("Running...");
		} catch (Throwable e) {
			log.error(e.getMessage());
			System.err.println("FATAL: " + e.getMessage());
			e.printStackTrace();
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
}