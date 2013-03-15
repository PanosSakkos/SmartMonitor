package di.kdd.buildmon.protocol;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

import di.kdd.buildmon.protocol.IProtocol.Tag;

public class KnockKnockThread extends Thread {
	private PeerData peerData;
	private ServerSocket welcomeSocket;
	
	private static final String DEBUG_TAG = "knock knock listener";
	
	public KnockKnockThread(PeerData peerData) throws IOException {
		this.peerData = peerData;
		welcomeSocket = new ServerSocket(IProtocol.KNOCK_KNOCK_PORT);
	}
	
	@Override
	public void run() {
		Tag tag;
		BufferedReader in;
		DataOutputStream out;
		
		try {
			while(true) {
					Socket connectionSocket = welcomeSocket.accept();
					in = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

					/* Assert thqt the tag of the message is valid */
					
					tag = Tag.valueOf(in.readLine());

					if(tag != Tag.KNOCK_KNOCK) {
						Log.d(DEBUG_TAG, "Invalid tag");
						continue;
					}
			
					/* Send the peer data to the node that wants to join the distributed network */
					
					out = new DataOutputStream(connectionSocket.getOutputStream());
					out.writeChars(tag.name() + '\n' + peerData.toString());
					
					//TODO time synchronization
					
					peerData.addPeerIP(connectionSocket.getRemoteSocketAddress().toString());
					Log.d(DEBUG_TAG, "Added " + connectionSocket.getRemoteSocketAddress().toString() + "to peer data");
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
