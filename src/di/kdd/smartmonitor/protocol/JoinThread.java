package di.kdd.smartmonitor.protocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;
import di.kdd.smartmonitor.middlewareServices.TimeSynchronizationMessage;
import di.kdd.smartmonitor.protocol.ISmartMonitor.Tag;

public class JoinThread extends Thread {
	private PeerData peerData;
	ServerSocket joinSocket;

	private static final String TAG = "JOIN listener";
	
	public JoinThread(PeerData peerData) {
		this.peerData = peerData;
	}
		
	@Override
	public void run() {
		Message message;
		Socket connectionSocket = null;
		
		android.os.Debug.waitForDebugger();
		
		try {
			joinSocket = new ServerSocket(ISmartMonitor.JOIN_PORT);
		} 
		catch (IOException e) {
			Log.e(TAG, "Could not bind socket at the knock knock port");
			e.printStackTrace();
			
			return;
		}

		Log.i(TAG, "Accepting on " + Integer.toString(ISmartMonitor.JOIN_PORT));
		
		while(true) {
			try {
				connectionSocket = joinSocket.accept();
				
				Log.i(TAG, "Accepted socket");
				
				DistributedSystemNode.receive(connectionSocket);

				/* Send the peer data to the node that wants to join the distributed network */
				
				message = new Message(Tag.JOIN, peerData.toString());
				DistributedSystemNode.send(connectionSocket, message);
				
				/* Update the peer data with the new IP address */
				
				peerData.addPeerIP(connectionSocket.getRemoteSocketAddress().toString());
				
				/* Send synchronization message */
				
				DistributedSystemNode.send(connectionSocket, new TimeSynchronizationMessage());
			}
			catch(IOException e) {
				Log.e(TAG, "Error while communicating with a peer");
				e.printStackTrace();
			}
			finally {
				try {
					if(connectionSocket != null) {
						connectionSocket.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		joinSocket.close();
		super.finalize();
	}
}
