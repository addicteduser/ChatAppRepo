package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Hashtable;

import chatApp.Driver;

/**
 * A multithreaded chat room server.  When a client connects the
 * server requests a screen name by sending the client the
 * text "SUBMITNAME", and keeps requesting a name until
 * a unique one is received.  After a client submits a unique
 * name, the server acknowledges with "NAMEACCEPTED".  Then
 * all messages from that client will be broadcast to all other
 * clients that have submitted a unique screen name.  The
 * broadcast messages are prefixed with "MESSAGE ".
 *
 * Because this is just a teaching example to illustrate a simple
 * chat server, there are a few features that have been left out.
 * Two are very useful and belong in production code:
 *
 *     1. The protocol should be enhanced so that the client can
 *        send clean disconnect messages to the server.
 *
 *     2. The server should do some logging.
 */
public class ChatServer extends Thread {
	/**
	 * Set of all clients. Checked to see if names are unique.
	 */
	//private static HashSet<String> names = new HashSet<String>();
	/**
	 * Set of all PrintWriter for each client.
	 */
	//private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
	
	// A Dictionary for tracking the uniquely named clients and their PrintWriters.
	private static Hashtable<String, PrintWriter> clients = new Hashtable<String, PrintWriter>();
	
	public static void main(String[] args) throws Exception {
        System.out.println("[SERVER] The chat server is running.");
        ServerSocket listener = new ServerSocket(Driver.getPort());
        try {
        	// Listen for client connections
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }
	
	/**
     * A handler thread class.  Handlers are spawned from the listening
     * loop and are responsible for a dealing with a single client
     * and broadcasting its messages.
     */
    private static class Handler extends Thread {
        private String name;
        private Socket connection;
        private BufferedReader in;
        private PrintWriter out;

        /**
         * Constructs a handler thread, squirreling away the socket.
         * All the interesting work is done in the run method.
         */
        public Handler(Socket socket) {
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

                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                out = new PrintWriter(connection.getOutputStream(), true);

                // Request a name from this client.  Keep requesting until
                // a name is submitted that is not already used.  Note that
                // checking for the existence of a name and adding the name
                // must be done while locking the set of names.
                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null) {
                        continue;
                    }
                    
                    if(name.length() == 0)
                    	continue;
                    
                    // Force the name not to contain white spaces.
                    if(name.indexOf(" ") > -1)
                    	continue;
                    
                    /*
                    synchronized (names) {
                        if (!names.contains(name)) {
                            names.add(name);
                            break;
                        }
                    }
                    */
                    
                    // Now that a successful name has been chosen, add the
                    // socket's print writer to the set of all writers so
                    // this client can receive messages.
                    synchronized(clients){
                    	if(!clients.containsKey(name)){
                            out.println("NAMEACCEPTED");
                    		clients.put(name, out);
                    		break;
                    	}
                    }
                }

                //out.println("NAMEACCEPTED");
                //writers.add(out);

                // Accept messages from this client and broadcast them.
                // Ignore other clients that cannot be broadcasted to.
                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        continue;
                    }

                    // Check for a private message.
                    boolean isPrivateMessage = false;
                    String targetName = "!!!!!!!!!!";
                	try {
	                    if(input.substring(0, 1).equals("@")){
                        	int firstSpace = input.indexOf(" ");
                        	if(firstSpace < 0){
                        		synchronized(clients){
                        			clients.get(name).println("ERROR " + "ERROR: There must be a space between the name and the message.");
                        		}
                        		
                        		continue;
                        	} else{
	                        	targetName = input.substring(1, firstSpace);
	                        	
	                        	synchronized(clients){
	                        		if(!clients.containsKey(targetName)){
	                        			clients.get(name).println("ERROR " + "ERROR: User @" + targetName + " does not exist!");
	                        			continue;
	                        		}
	                        	}
	                        	
	                        	isPrivateMessage = true;
                        	}
	                    } 
                	} catch(Exception e){
                		System.out.println(e.getMessage());
                	}
                	
                	// Send the messages to the targeted clients.
                    synchronized(clients){
	                    for (String ckey : clients.keySet()) {
	                    	if(!isPrivateMessage){
	                    		clients.get(ckey).println("MESSAGE " + name + ": " + input);
	                    		continue;
	                    	}
	                    	
	                    	// Print the message both to the sender and the target.
	                    	if(ckey.equals(name)){
	                    		clients.get(ckey).println("PRIVATEMESSAGESENDER " + name + ": " + input);
	                    	} else if(ckey.equals(targetName)){
	                    		clients.get(ckey).println("PRIVATEMESSAGETARGET " + name + ": " + input);
	                    	}
	                    }
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
                
            } finally {
                // This client is going down!  Remove its name and its print
                // writer from the sets, and close its socket.
            	if(name != null){
            		synchronized(clients){
            			clients.remove(name);
            		}
            	}
            	
                try {
                    connection.close();
                } catch (IOException e) {
                	System.out.println(e.getMessage());
                }
            }
        }
    }
}
