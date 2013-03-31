package di.kdd.smartmonitor.protocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;
import di.kdd.smartmonitor.middlewareServices.TimeSynchronizationMessage;
import di.kdd.smartmonitor.protocol.IProtocol.Tag;

public class KnockKnockThread extends Thread {
	private PeerData peerData;
	private ServerSocket welcomeSocket;
	
	private static final String TAG = "knock knock listener";
	
	public KnockKnockThread(PeerData peerData) {
		this.peerData = peerData;
	}
	
	@Override
	public void run() {
		Message message;
				
		android.os.Debug.waitForDebugger();
		
		try {
			welcomeSocket = new ServerSocket(IProtocol.JOIN_PORT);
		} 
		catch (IOException e) {
			Log.d(TAG, "Could not bind socket at the knock knock port");
			e.printStackTrace();
		}

		try {
			while(true) {
					Socket connectionSocket = welcomeSocket.accept();
					DistributedSystemNode.receive(connectionSocket);

					/* Send the peer data to the node that wants to join the distributed network */
					
					message = new Message(Tag.JOIN, peerData.toString());
					DistributedSystemNode.send(connectionSocket, message);
					
					/* Update the peer data with the new IP address */
					
					//TODO REVIEW
					peerData.addPeerIP(connectionSocket.getRemoteSocketAddress().toString());
					Log.d(TAG, "Added " + connectionSocket.getRemoteSocketAddress().toString() + "to peer data");
					
					/* Send synchronization message */
					
					DistributedSystemNode.send(connectionSocket, new TimeSynchronizationMessage());
			}
		}
		catch(IOException e) {
			Log.d(TAG, "Error while communicating with a peer");
			e.printStackTrace();
		}
	}

}
