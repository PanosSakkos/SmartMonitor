package di.kdd.buildmon.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;
import di.kdd.buildmon.middlewareServices.TimeSynchronization;
import di.kdd.buildmon.protocol.IProtocol.Tag;

public final class PeerNode extends DistributedSystemNode {
	/* The socket that the peer holds in order to get commands from the Captain */
	
	private ServerSocket peerCommandSocket;

	private TimeSynchronization timeSync = new TimeSynchronization();

	private static final String TAG = "peer";

	public PeerNode(Socket socket) {		
		Message message;
		BufferedReader in;

		try {
			/* The Captain was found, send the knock-knock message */
			
			message = new Message(Tag.KNOCK_KNOCK, null);

			/* Send request to join the distributed network */

			send(socket, message);
			
			/* Receive peer data */

			in = receive(socket);
										
			/* Parse peer data */
			
			peerData.addPeersFromStream(in);
			socket.close();			
		}
		catch(Exception e) {
			//TODO
		}		

		commandThread = new Thread(this);
		commandThread.start();
	}
	
	/***
	 * Accepts a socket connection on the COMMAND_PORT and waits for commands from the Captain
	 */
	
	@Override
	public void run() {
		Socket captainSocket;
		BufferedReader in;

		android.os.Debug.waitForDebugger();
		
		try {
			peerCommandSocket = new ServerSocket(IProtocol.COMMAND_PORT);		
			captainSocket = peerCommandSocket.accept();
		}
		catch(IOException e) {
			Log.d(TAG, "Failed to accept command socket");
			return;
		}
		
		/* Listen on captainSocket for incoming commands from the Captain */
		try {		
			while(true) {
				Message message;
			
				in = receive(captainSocket);
				message = new Message(in);				
			
				switch(message.getTag()) {
					case NEW_PEER:
						/* A new peer was accepted form the Captain, get his IP address */										
	
						peerData.addPeerIP(message.getPayload());
						break;
					case TIME_SYNC:
						/* The Captain sent a message with its shipment timestamp */
						
						timeSync.timeReference(Long.parseLong(message.getPayload()));
						break;
					case AGGREGATE_PEAKS:
							
						break;
					default:
						Log.d(TAG, "Not implemented Tag handling: " + message.getTag().toString());
						break;
				}
			}
		}
		catch(IOException e) {
			//TODO REVIEW captain is down?
			e.printStackTrace();
		}		
	}
	
	@Override
	public boolean isCaptain() {
		return false;
	}

	@Override
	public String getCaptainIP() {
		return peerData.getLowestIP();
	}

	@Override
	public void end() {
		if(commandThread != null) {
			commandThread.interrupt();
		}
	}
}
