package chatApp;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

/**
 * A simple Swing-based client for the chat server.  Graphically
 * it is a frame with a text field for entering messages and a
 * textarea to see the whole dialog.
 *
 * The client follows the Chat Protocol which is as follows.
 * When the server sends "SUBMITNAME" the client replies with the
 * desired screen name.  The server will keep sending "SUBMITNAME"
 * requests as long as the client submits screen names that are
 * already in use.  When the server sends a line beginning
 * with "NAMEACCEPTED" the client is now allowed to start
 * sending the server arbitrary strings to be broadcast to all
 * chatters connected to the server.  When the server sends a
 * line beginning with "MESSAGE " then all characters following
 * this string should be displayed in its message area.
 */
public class ChatClient {
	private static ChatClientUI c;
	private BufferedReader in;
	private PrintWriter out;
	private Socket socket;
	private String tempName;

	/**
	 * Constructs the client by laying out the GUI and registering a
	 * listener with the textfield so that pressing Return in the
	 * listener sends the textfield contents to the server.  Note
	 * however that the textfield is initially NOT editable, and
	 * only becomes editable AFTER the client receives the NAMEACCEPTED
	 * message from the server.
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public ChatClient() throws UnknownHostException, IOException {
		c = new ChatClientUI();
		c.setVisible(true);
		c.addInputTextAreaActionListener(new ActionListener() {
			/**
			 * Responds to pressing the enter key in the textfield by sending
			 * the contents of the text field to the server.    Then clear
			 * the text area in preparation for the next message.
			 */
			public void actionPerformed(ActionEvent e) {
				out.println(c.getInputTextField().getText());
				c.getInputTextField().setText("");
			}
		});
		
		run();
	}

	/**
	 * Connects to the server then enters the processing loop.
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	private void run() throws UnknownHostException, IOException {
		initializeConnection();
		initializeStreams();
		protocolHandler();        
	}

	private void initializeConnection() throws UnknownHostException, IOException {
		socket = new Socket(Driver.getHost(), Driver.getPort());
	}

	private void initializeStreams() throws IOException {
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
	}

	private void protocolHandler() throws IOException {
		// Process all messages from server, according to the protocol.
		while (true) {
			String line = in.readLine();
			if (line.startsWith("SUBMITNAME")) {
				out.println(getName());
			} else if (line.startsWith("NAMEACCEPTED")) {
				c.getInputTextField().setEditable(true);
				c.setTitle("ChatApp   ||   @"+tempName);
			} else if (line.startsWith("MESSAGE")) {
				c.getMessageTextArea().append(line.substring(8) + "\n");
			} else if (line.startsWith("PM")) {
				String[] temp = line.split("|");
				// 0 -- PM
				// 1 -- name
				// 2 -- input
				if(tempName.equalsIgnoreCase(temp[1])) {
					c.getMessageTextArea().append(temp[1]+": "+temp[2]+"\n");
				}
			}
		}
	}
	
	/**
	 * Prompt for and return the desired screen name.
	 */
	private String getName() {
		tempName = JOptionPane.showInputDialog(
				c,
				"ChatApp   ||   Choose a screen name:",
				"Screen name selection",
				JOptionPane.PLAIN_MESSAGE);
		return tempName;
	}

	/**
	 * Runs the client as an application with a closeable frame.
	 */
	public static void main(String[] args) throws Exception {
		new ChatClient();
	}
}
