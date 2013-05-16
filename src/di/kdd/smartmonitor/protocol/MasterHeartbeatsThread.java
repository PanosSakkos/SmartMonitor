package di.kdd.smartmonitor.protocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import di.kdd.smartmonitor.protocol.ISmartMonitor.Tag;
import di.kdd.smartmonitor.protocol.exceptions.TagException;

import android.util.Log;

public class MasterHeartbeatsThread extends Thread {
	
	private PeerData peerData;
	
	private static final String TAG = "master heartbeats thread";
	
	public MasterHeartbeatsThread(PeerData peerData) {
		this.peerData = peerData;			
	}
	
	public class HeartbeatsThread extends Thread {

		private Socket heartbeatsSocket;
		private PeerData peerData;
		
		public HeartbeatsThread(Socket heartbeatsSocket, PeerData peerData) {
			this.heartbeatsSocket = heartbeatsSocket;
			this.peerData = peerData;
		}
		
		@Override
		public void run() {
			android.os.Debug.waitForDebugger();

			while(!this.isInterrupted()) {
				try {
					Node.receive(heartbeatsSocket, Tag.HEARTBEAT);
					
					Log.i(TAG, "Received heartbeat from " + heartbeatsSocket.getInetAddress().toString());
				} 
				catch (IOException e) {

					/* Node failed to send a heartbeat 
					 * and it's considered fallen 
					 */
					
					Log.i(TAG, "Failed to receive heartbeat from " + heartbeatsSocket.getInetAddress().toString());
					
					peerData.removePeerIP(heartbeatsSocket.getInetAddress().toString());
					return;
				} 
				catch (TagException e) {
					Log.e(TAG, "Received invalid Message Tag");
					e.printStackTrace();
				}
				catch (ClassNotFoundException e) {
					e.printStackTrace();
				} 
			}
 		}
	}
	
	@Override
	public void run() {		
		android.os.Debug.waitForDebugger();

		ServerSocket heartbeatsServerSocket;
		
		/* Accept heartbeat connections and issue a new 
		 * thread to listen to them 
		 */
		
		try {
			heartbeatsServerSocket = new ServerSocket(ISmartMonitor.HEARBEATS_PORT);
			heartbeatsServerSocket.setReuseAddress(true);
		}
		catch (IOException e) {
			Log.e(TAG, "Could not bind socket at the heartbeats port");
			e.printStackTrace();

			return;
		} 
		
		Log.i(TAG, "Listening for heartbeats connections");
		
		while(!this.isInterrupted()) {
			try {
				Socket heartbeatsSocket = heartbeatsServerSocket.accept();
				
				Log.i(TAG, "Accepted heartbeats connection from " + heartbeatsServerSocket.getInetAddress().toString());
				
				HeartbeatsThread heartbeatsThread = new HeartbeatsThread(heartbeatsSocket, peerData);
				heartbeatsThread.start();
			} 
			catch (IOException e) {
				Log.e(TAG, "Error while accepting heartbeats socket");
				e.printStackTrace();
			}
		}
	}
}
