package di.kdd.buildmon.protocol;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.util.Log;

public class Protocol extends Thread implements IProtocol {	
	private boolean isCaptain;
	private PeerData peerData = new PeerData(this);
	private List<Socket> commandSockets = new ArrayList<Socket>();
	
	private Thread commandThread;
	private KnockKnockThread knockKnockThread;

	private static final String DEBUG_TAG = "protocol";
		
	@Override
	public void start() {
		commandThread = new Thread(this);
		commandThread.start();
	}

	@Override
	public boolean isCaptain() {
		return isCaptain;
	}
	
	@Override
	public String getCaptain() {
		
		/* Captain is the peer with the lowest IP address */
		
		return peerData.getLowestIP();
	}
	
	@Override
	public void end() {
		if(knockKnockThread != null) {
			knockKnockThread.interrupt();
		}
		
		commandThread.interrupt();
	}

	/***
	 * This method is dispatched in a new thread and is called form the start method.
	 * DO NOT dispatch this method.
	 */
	
	@Override
	public void run() {
		Socket socket;
		BufferedReader in;
		Message message;
		String ipPrefix = "192.168.1."; //TODO FIXME
		String peerDataLine;
		
		/* Look for the Captain in the first 255 local IP addresses */
		
		for(int i = 1; i < 255; ++i) {
			try{
				socket = new Socket(ipPrefix + Integer.toString(i), IProtocol.KNOCK_KNOCK_PORT);
			}
			catch(IOException e) {
				continue;
			}
			
			/* I found the Captain, send the knock-knock message */
		
			message = new Message(Tag.KNOCK_KNOCK, null);
			
			try {
				Tag tag;
				
				/* Send request to join the distributed network */

				send(socket, message);
				
				/* Receive peer data */

				in = receive(socket);
								
				/* Assert message tag is a knock-knock tag */
				
				tag = Tag.valueOf(in.readLine());
				
				if(tag != Tag.KNOCK_KNOCK) {
					Log.d(DEBUG_TAG, "Captain responded without a knock-knock tag");
					return;
				}
				
				/* Parse peer data */
				
				while((peerDataLine = in.readLine()) != null) {
					peerData.addPeerIP(peerDataLine);
				}
				
				peer();
			}
			catch(IOException e) {
				Log.d(DEBUG_TAG, "Unable to join the network");
			}						
		}
		
		/* No response, I am the first node of the distributed system and the Captain */
		
		try {
			captain();
		}
		catch(IOException e) {
			Log.d(DEBUG_TAG, "captain failed");
		}
	}
	
	protected  static void send(Socket socket, Message message) throws IOException {
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		out.writeChars(message.toString());		
	}
	
	protected static BufferedReader receive(Socket socket) throws IOException {
		return new BufferedReader(new InputStreamReader(socket.getInputStream()));		
	}
	
	/***
	 * Sends a message to each connected peer
	 * @param message The message to broadcast
	 * @throws IOException
	 */
	
	private void broadcast(Message message) throws IOException {
		for(Socket peer : commandSockets) {
			DataOutputStream out = new DataOutputStream(peer.getOutputStream());
			out.writeChars(message.toString());				
		}		
	}
	
	/***
	 * Called by the PeerData instance that this class holds,
	 * when a new IP is added by the KnockKnockThread.
	 * @param ip The IP address of the node that joined the network.
	 */
	
	protected void newPeerAdded(String ip) {
		try {
			Socket commandSocket = new Socket(ip, IProtocol.COMMAND_PORT);
			commandSockets.add(commandSocket);
			
			/* Notify peers about the new peer that joined the network */
			
			Message message = new Message(Tag.NEW_PEER, ip);						
			broadcast(message);
		}
		catch (Exception e) {
			Log.d(DEBUG_TAG, "Failed to connect to " + ip);
			peerData.removePeerIP(ip);
		}
	}
	
	private void captain() throws IOException {
		isCaptain = true;

		/* Start accepting nodes that want to join the distributed system */
		
		knockKnockThread = new KnockKnockThread(peerData);
		knockKnockThread.start();
	}

	private void peer() {
		isCaptain = false;
		
		//TODO
	}

	@Override
	public void computeBuildingSignature(Date from, Date to) throws NotCaptainException, IOException {
		if(isCaptain() == false) {
			throw new NotCaptainException();
		}

		Message message = new Message(Tag.AGGREGATE_PEAKS, 
								Long.toString(from.getTime()) + Long.toString(to.getTime()));
		broadcast(message);
		
		//TODO gather peaks
	}
}
