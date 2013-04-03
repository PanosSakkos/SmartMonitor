package di.kdd.smartmonitor.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.util.Log;
import di.kdd.smartmonitor.protocol.ISmartMonitor.Tag;
import di.kdd.smartmonitor.protocol.exceptions.MasterException;

public final class MasterNode extends DistributedSystemNode {
	/* List of open sockets with the peers, in order to send them commands */
	
	private List<Socket> commandSockets = new ArrayList<Socket>();
	
	/* This thread is used only from the Master, in order to accept new 
	 * nodes in the system.
	 */
	
	private JoinThread joinThread;

	private static final String TAG = "master";	
	
	public MasterNode() {
		joinThread = new JoinThread(peerData);
		joinThread.start();
	}

	/***
	 * Sends a message to each connected peer
	 * @param message The message to broadcast
	 * @throws IOException
	 */
	
	private void broadcastCommand(Message message) throws IOException {
		Log.i(TAG, "Broadcasting " + message.toString());
		
		for(Socket peer : commandSockets) {
			DistributedSystemNode.send(peer, message);
		}		
	}
	
	/***
	 * Handler to be called by the PeerData instance that this class holds,
	 * when a new IP is added by the Join thread. Sends NEW_PEER commands
	 * to the peers to notify them for the new peer that joined the system
	 * @param ip The IP address of the node that joined the network.
	 */
	
	protected void newPeerAddedHandler(String ip) {
		Log.i(TAG, "New peer added: " + ip);
		
		try {			
			/* Connect to the peer, in order to have a communication channel for commands */
			
			Socket commandSocket = new Socket(ip, ISmartMonitor.COMMAND_PORT);
			commandSockets.add(commandSocket);
			
			/* Notify peers about the new peer that joined the network */
			
			Message message = new Message(Tag.NEW_PEER, ip);			
			broadcastCommand(message);
		}
		catch (Exception e) {
			Log.e(TAG, "Failed to connect to " + ip);
			peerData.removePeerIP(ip);
		}
	}
	
	/***
	 * Broadcasts START_SAMPLING command to the peer nodes
	 * @throws IOException Communication error
	 */
	
	public void startSampling() throws IOException {
		broadcastCommand(new Message(Tag.START_SAMPLING, ""));
	}

	/***
	 * Broadcasts STOP_SAMPLING command to the peer nodes
	 * @throws IOException
	 */

	public void stopSampling() throws IOException {
		broadcastCommand(new Message(Tag.STOP_SAMPLING, ""));
	}
	
	/***
	 * Broadcasts SEND_PEAKS command to the peers, gather their frequency peaks and
	 * computes the modal frequencies from all the peaks
	 * @param from First time of samples of interest
	 * @param to End time of samples of interest 
	 * @throws MasterException The node must be a Master node
	 * @throws IOException Communication error
	 */
	
	public void computeModalFrequencies(Date from, Date to) throws MasterException, IOException {
		Message message = new Message(Tag.SEND_PEAKS, 
								Long.toString(from.getTime()) + "\n" + 
								Long.toString(to.getTime()));
		broadcastCommand(message);
		
		/* Gather each peer's peaks */
		
		for(Socket socket : commandSockets) {
			BufferedReader in = receive(socket);
			
			message = new Message(in);
			
			//TODO Gather peaks
		}
		
		//TODO Find Master node's peaks, decide the global peaks and store them
	}	

	@Override
	public void disconnect() {
		Log.i(TAG, "Disconnecting");
		
		if(joinThread != null) {
			joinThread.interrupt();
		}
	}
	
	@Override
	public boolean isMaster() {
		return true;
	}
}
