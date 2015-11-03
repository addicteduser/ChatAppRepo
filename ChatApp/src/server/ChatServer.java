package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

import chatApp.Driver;

/**
 * A multithreaded chat room server. 
 * When a client connects, the server requests a
 * screen name by sending the client the text "SUBMITNAME",
 * and keeps requesting a name until a unique one is received.
 * After a client submits a unique name, the server
 * acknowledges with "NAMEACCEPTED". Then all messages from
 * that client will be broadcast to all other clients that
 * have submitted a unique screen name. The broadcast messages
 * are prefixed with "MESSAGE".
 */
public class ChatServer extends Thread {
	/**
	 * Set of all clients. Checked to see if names are unique.
	 */
	private static HashSet<String> names = new HashSet<String>();
	/**
	 * Set of all PrintWriter for each client.
	 */
	private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

	public ChatServer() throws IOException {
		System.out.println("[SERVER] The chat server is running.");
		ServerSocket listener = new ServerSocket(Driver.getPort());

		try {
			// Listen for client connections
			while (true) {
				new ClientManager(listener.accept()).start();
			}
		} finally {
			listener.close();
		}
	}

	/**
	 * A client manager thread class. 
	 * Client managers are spawned from the listening loop of the server
	 * and are responsible for a dealing with a single client and broadcasting its messages.
	 */
	private static class ClientManager extends Thread {
		private String name;
		private Socket connection;
		private BufferedReader in;
		private PrintWriter out;

		/**
		 * Constructs a client manager thread, squirreling away the socket.
		 */
		public ClientManager(Socket socket) {
			System.out.println("[SERVER] New client connection.");
			this.connection = socket;
		}

		/**
		 * Services this thread's client by repeatedly requesting a
		 * screen name until a unique one has been submitted, then
		 * acknowledges the name and registers the output stream for
		 * the client in a global set, then repeatedly gets inputs and
		 * broadcasts them.
		 */
		public void run() {
			try {
				initializeIOStreams();
				requestClientName();
				acceptClient();
				sendMessages();
				// TODO: put code handling private messaging in SEND MESSAGE				
			} catch (IOException e) {
				System.err.println("[SERVER] ERROR: " + e);
			} finally {
				closeClient();
			}
		}

		/**
		 * Create character streams for the socket.
		 */
		private void initializeIOStreams() throws IOException {
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			out = new PrintWriter(connection.getOutputStream(), true);
		}

		/**
		 * Request a name from this client.  Keep requesting until
		 * a name is submitted that is not already used.  Note that
		 * checking for the existence of a name and adding the name
		 * must be done while locking the set of names.
		 * @throws IOException
		 */
		private void requestClientName() throws IOException {
			boolean isNameTaken = true;
			
			do {
				out.println("SUBMITNAME");
				name = in.readLine();

				// empty string
				if (name == null) {
					isNameTaken = true;
				}
				
				// check if name is taken
				synchronized (names) {
					if (!names.contains(name)) {
						names.add(name);
						isNameTaken = false;
					} else
						isNameTaken = true;
				}
			} while(isNameTaken);
		}
		
		/**
		 * Now that a successful name has been chosen, add the
		 * socket's print writer to the set of all writers so
		 * this client can receive broadcast messages.
		 */
		private void acceptClient() {
			out.println("NAMEACCEPTED");
			writers.add(out);
		}

		/**
		 *  Accept messages from this client and broadcast them.
		 *  Ignore other clients that cannot be broadcasted to.
		 * @throws IOException
		 */
		private void sendMessages() throws IOException {
			while (true) {
				String input = in.readLine();
				
				// if empty
				if (input == null) {
					return;
				}
				
				// broadcast message
				for (PrintWriter writer : writers) {
					writer.println("MESSAGE " + name + ": " + input);
				}
			}
		}
	
		/**
		 * This client is going down!  Remove its name and its print
		 * writer from the sets, and close its socket.
		 */
		private void closeClient() { 
			if (name != null) {
				names.remove(name);
			}
			
			if (out != null) {
				writers.remove(out);
			}
			
			try {
				connection.close();
			} catch (IOException e) { }
		}
	}
}
