package io.anaxo.http.ntlmproxy.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Connection {

	private static final Logger log = LoggerFactory.getLogger(Connection.class);
	
	private Socket socket;

    private boolean open;

    private BufferedReader bufferedReader;

    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    public void disconnect(){
        this.open = false;
        try {
            socket.close();
            bufferedReader.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public boolean isOpen(){
        return open;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public void setBufferedReader(BufferedReader bufferedReader) {
        this.bufferedReader = bufferedReader;
    }

    public BufferedReader getBufferedReader() {
        return bufferedReader;
    }

    public String readLine() throws IOException {
        return  bufferedReader.readLine();
    }

    public int read() throws IOException {
        return bufferedReader.read();
    }

	public void shutdownInput() {
		try {
			socket.shutdownInput();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}
}