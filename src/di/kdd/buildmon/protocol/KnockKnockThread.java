package di.kdd.buildmon.protocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;
import di.kdd.buildmon.middlewareServices.TimeSynchronizationMessage;
import di.kdd.buildmon.protocol.IProtocol.Tag;

public class KnockKnockThread extends Thread {
	private PeerData peerData;
	private ServerSocket welcomeSocket;
	
	private static final String TAG = "knock knock listener";
	
	public KnockKnockThread(PeerData peerData) throws IOException {
		this.peerData = peerData;
		welcomeSocket = new ServerSocket(IProtocol.KNOCK_KNOCK_PORT);
	}
	
	@Override
	public void run() {
		Message message;
		
		try {
			while(true) {
					Socket connectionSocket = welcomeSocket.accept();
					DistributedSystemNode.receive(connectionSocket);

					/* Send the peer data to the node that wants to join the distributed network */
					
					message = new Message(Tag.KNOCK_KNOCK, peerData.toString());
					DistributedSystemNode.send(connectionSocket, message);
					
					/* Update the peer data with the new IP address */
					
					//TODO REVIEW
					peerData.addPeerIP(connectionSocket.getRemoteSocketAddress().toString());
					Log.d(TAG, "Added " + connectionSocket.getRemoteSocketAddress().toString() + "to peer data");
					
					/* Send synchronization message */
					
					DistributedSystemNode.send(connectionSocket, new TimeSynchronizationMessage());
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
