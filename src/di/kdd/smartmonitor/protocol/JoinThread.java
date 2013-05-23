package di.kdd.smartmonitor.protocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;

import android.util.Log;
import di.kdd.smartmonitor.protocol.ISmartMonitor.Tag;
import di.kdd.smartmonitor.protocol.exceptions.TagException;

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
			joinSocket.setReuseAddress(true);
		} 
		catch (IOException e) {
			Log.e(TAG, "Could not bind socket at the join port");
			e.printStackTrace();
						
			return;
		}

		Log.i(TAG, "Listening on " + Integer.toString(ISmartMonitor.JOIN_PORT));
		
		while(!this.isInterrupted()) {
			try {
				connectionSocket = joinSocket.accept();
				
				/* Receive JOIN message */
				
				Node.receive(connectionSocket, Tag.JOIN);

				/* Send PEER_DATA to the node that wants to join the system */
				
				message = new Message(Tag.PEER_DATA, peerData.toString());
				Node.send(connectionSocket, message);
				
				/* Send TIME_SYNC message */
				
				Node.send(connectionSocket, new TimeSynchronizationMessage());

				/* Update the peer data with the IP address of the new node */
				
				peerData.addPeerIP(connectionSocket.getInetAddress().toString());
				
			}
			catch(IOException e) {
				Log.e(TAG, "Error while communicating with a peer");
				e.printStackTrace();
			}
			catch(TagException e) {
				Log.e(TAG, "Didn't receive JOIN tag");
			}
			catch(Exception e) {
				Log.e(TAG, e.getMessage());
			}
			finally {
				try {
					if(connectionSocket != null) {
						connectionSocket.close();
					}
				} catch (IOException e) {
				}
			}
		}
		
		/* Join thread was interrupted */
		
		try {
			joinSocket.close();
		}
		catch(IOException e) {			
		}

	}	
}
