package io.anaxo.http.ntlmproxy.connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionManager {

	private static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);
	
	private ServerSocket serverSocket;
    
    private int socketTimeout;

    public ConnectionManager(int port, int socketTimeoutInMilliseconds) throws IOException {
        serverSocket = new ServerSocket(port);
        socketTimeout = socketTimeoutInMilliseconds;
    }

    public Connection awaitClient() throws IOException {
        Socket socket = serverSocket.accept();
        return ConnectionBuilder.buildConnection(socket,socketTimeout);
    }

    public void shutDown() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}