package di.kdd.buildmon.protocol;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;

import di.kdd.buildmon.protocol.exceptions.NotCaptainException;

import android.util.Log;

public class DistributedSystem implements IProtocol {
	private DistributedSystemNode node;
	
	private static final String TAG = "distributed system";
	
	/***
	 * Sends Knock-Knock messages to the first 255 local IP addresses and
	 * according to if it will get a response or not, the node becomes
	 * a peer or a Captain respectively.
	 */
	
	@Override
	public void start() {
		Socket socket;
		String ipPrefix = "192.168.1."; //TODO FIXME
		
		/* Look for the Captain in the first 255 local IP addresses */
		
		for(int i = 1; i < 255; ++i) {
			try{
				socket = new Socket(ipPrefix + Integer.toString(i), IProtocol.KNOCK_KNOCK_PORT);
			}
			catch(IOException e) {
				continue;
			}
			
			/* Captain found */
			
			node = new PeerNode(socket);
		}
		
		/* No response, I am the first node of the distributed system and the Captain */
		
		try {
			node = new CaptainNode();
		}
		catch(IOException e) {
			Log.d(TAG, "captain failed");
		}
	}

	@Override
	public void computeBuildingSignature(Date from, Date to) throws NotCaptainException, IOException {
		if(node.isCaptain() == false) {
			throw new NotCaptainException();
		}
		
		((CaptainNode) node).computeBuildingSignature(from, to);
	}

	@Override
	public boolean isCaptain() {
		return node.isCaptain();
	}

	@Override
	public String getCaptainIP() {
		return node.getCaptainIP();
	}

	@Override
	public void end() {
		node.end();
	}
}
