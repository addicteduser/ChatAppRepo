package chatApp;

import java.io.IOException;

import server.ChatServer;

/**
 * SOURCE: http://cs.lmu.edu/~ray/notes/javanetexamples/#chat
 */
public class Driver {
	private final static String host = "localhost";
	private final static int port = 8080;
	
	public static void main(String[] args) throws IOException {
		ChatServer server = new ChatServer();
		server.run();

	}
	
	/**
	 * @return the host
	 */
	public static String getHost() {
		return host;
	}

	/**
	 * @return the port
	 */
	public static int getPort() {
		return port;
	}
}
