package chatApp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class ChatClientUI extends JFrame {
	private JScrollPane inputScrollPane;
	private JScrollPane messageScrollPane;
	private JTextArea messageTextArea;
	private JTextField inputTextField;

	/**
	 * Create the frame.
	 */
	public ChatClientUI() {
		initGUI();
	}
	
	/**
	 * @return the messageTextArea
	 */
	public JTextArea getMessageTextArea() {
		return messageTextArea;
	}

	/**
	 * @param messageTextArea the messageTextArea to set
	 */
	public void setMessageTextArea(JTextArea messageTextArea) {
		this.messageTextArea = messageTextArea;
	}

	/**
	 * @return the inputTextField
	 */
	public JTextField getInputTextField() {
		return inputTextField;
	}

	/**
	 * @param inputTextField the inputTextField to set
	 */
	public void setInputTextField(JTextField inputTextField) {
		this.inputTextField = inputTextField;
	}

	public void addInputTextAreaActionListener(ActionListener l) {
		inputTextField.addActionListener(l);
	}
	
	private void initGUI() {
		setTitle("ChatApp");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 320);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		messageScrollPane = new JScrollPane();
		messageScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		getContentPane().add(messageScrollPane, BorderLayout.CENTER);
		
		messageTextArea = new JTextArea();
		messageTextArea.setMargin(new Insets(5, 5, 5, 5));
		messageTextArea.setEditable(false);
		messageTextArea.setRows(8);
		messageTextArea.setLineWrap(true);
		messageTextArea.setWrapStyleWord(true);
		messageScrollPane.setViewportView(messageTextArea);
		
		inputScrollPane = new JScrollPane();
		inputScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		getContentPane().add(inputScrollPane, BorderLayout.SOUTH);
		
		inputTextField = new JTextField();
		inputTextField.setPreferredSize(new Dimension(6, 50));
		inputScrollPane.setViewportView(inputTextField);
	}
}
