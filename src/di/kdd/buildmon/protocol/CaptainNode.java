package di.kdd.buildmon.protocol;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.util.Log;
import di.kdd.buildmon.protocol.IProtocol.Tag;
import di.kdd.buildmon.protocol.exceptions.NotCaptainException;

public final class CaptainNode extends DistributedSystemNode {
	/* List of open sockets with the peers, in order to send them commands */
	
	private List<Socket> commandSockets = new ArrayList<Socket>();
	
	/* This thread is used only from the Captain, in order to accept new 
	 * nodes in the system.
	 */
	
	private KnockKnockThread knockKnockThread;

	private static final String TAG = "captain";	
	
	public CaptainNode() {
		commandThread = new Thread(this);

		/* Start accepting nodes that want to join the distributed system */
		
		knockKnockThread = new KnockKnockThread(peerData);

		commandThread.start();
		knockKnockThread.start();
	}

	/***
	 * Sends a message to each connected peer
	 * @param message The message to broadcast
	 * @throws IOException
	 */
	
	private void broadcast(Message message) throws IOException {
		for(Socket peer : commandSockets) {
			DistributedSystemNode.send(peer, message);
		}		
	}
	
	/***
	 * Called by the PeerData instance that this class holds,
	 * when a new IP is added by the KnockKnockThread.
	 * @param ip The IP address of the node that joined the network.
	 */
	
	protected void newPeerAdded(String ip) {
		try {
			/* Connect to the peer, in order to have a communication channel for commands */
			
			Socket commandSocket = new Socket(ip, IProtocol.COMMAND_PORT);
			commandSockets.add(commandSocket);
			
			/* Notify peers about the new peer that joined the network */
			
			Message message = new Message(Tag.NEW_PEER, ip);						
			broadcast(message);
		}
		catch (Exception e) {
			Log.d(TAG, "Failed to connect to " + ip);
			peerData.removePeerIP(ip);
		}
	}
	
	@Override
	public void disconnect() {
		if(knockKnockThread != null) {
			knockKnockThread.interrupt();
		}
		
		if(commandThread != null) {
			commandThread.interrupt();
		}		
	}
	
	@Override
	public boolean isCaptain() {
		return true;
	}

	public void computeBuildingSignature(Date from, Date to) throws NotCaptainException, IOException {
		Message message = new Message(Tag.AGGREGATE_PEAKS, 
								Long.toString(from.getTime()) + "\n" + 
								Long.toString(to.getTime()));
		broadcast(message);
		
		/* Gather each peer's peaks */
		
		for(Socket socket : commandSockets) {
			BufferedReader in = receive(socket);
			
			message = new Message(in);
			
			//TODO
		}
	}	
}
