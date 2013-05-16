package di.kdd.smartmonitor.protocol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import di.kdd.smartmonitor.protocol.ISmartMonitor.Tag;
import di.kdd.smartmonitor.protocol.exceptions.TagException;

import android.util.Log;

public abstract class Node extends Thread {
	protected PeerData peerData = new PeerData();
	
	private static final String TAG = "node";
	
	public abstract void disconnect();
	
	public abstract boolean isMaster();

	public String getLowestIP() {
		return peerData.getLowestIP();
	}
	
	public String getNodeIP() {
		return peerData.getNodeIP();
	}
	
	public void forgetMasterIP() {
		peerData.forgetMasterIP();
	}
	
	/***
	 * Given an open socket and a message, sends the message
	 * @param socket The open socket to send the message 
	 * @param message The message to send
	 * @throws IOException When the socket is not open
	 */
	
	protected static void send(Socket socket, Message message) throws IOException {
		Log.i(TAG, "Sending: " + message.toString());

		ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		out.writeObject(message);
		out.flush();
	}
	
	/***
	 * Give an initialized socket, it returns its input stream
	 * @param socket The socket to get the input stream of
	 * @return The socket's input stream
	 * @throws IOException
	 */
	
	protected static Message receive(Socket socket) throws IOException, ClassNotFoundException {
		Log.i(TAG, "Receiving from " + socket.getInetAddress());
		
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		return (Message) in.readObject();
	}
	
	/***
	 * Receives a message from a connected socket and checks the tag of the message
	 * @param tag The desired tag
	 * @param socket The connected socket
	 * @return The message that was read from the socket
	 * @throws IOException Socket failure
	 * @throws TagException When the desired tag is not the same with the tag of 
	 * the received message
	 */
	
	protected static Message receive(Socket socket, Tag tag) throws IOException, TagException, ClassNotFoundException {
		Message message;
		ObjectInputStream in;
		
		Log.i(TAG, "Receiving from " + socket.getInetAddress() + " with desired Tag: " + tag.toString());
		
		in = new ObjectInputStream(socket.getInputStream());
		message = (Message) in.readObject();

		/* Check received message Tag */
		
		if(message.getTag() != tag) {
			throw new TagException();
		}
		
		return message;
	}

}
