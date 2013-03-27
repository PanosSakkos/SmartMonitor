package di.kdd.buildmon.protocol;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;

import android.os.AsyncTask;
import android.util.Log;

import di.kdd.buildmon.protocol.exceptions.NotCaptainException;

public class DistributedSystem extends AsyncTask<Void, Void, Void> implements IProtocol {
	private DistributedSystemNode node;
	
	private static final String TAG = "distributed system";
		
	public void connect() {
		this.execute();
	}
	
	/***
	 * Sends Knock-Knock messages to the first 255 local IP addresses and
	 * according to if it will get a response or not, the node becomes
	 * a peer or a Captain respectively.
	 */

	@Override
	protected Void doInBackground(Void... arg0) {
		Socket socket;
		String ipPrefix = "192.168.1."; //TODO FIXME
		
		android.os.Debug.waitForDebugger();

		/* Look for the Captain in the first 255 local IP addresses */
		
		for(int i = 1; i < 256; ++i) {
			try{
				Log.d(TAG, "Trying to connect to :" + ipPrefix + Integer.toString(i));			//TODO REVIEW timeout
				socket = new Socket(ipPrefix + Integer.toString(i), IProtocol.KNOCK_KNOCK_PORT);
			}
			catch(IOException e) {
				continue;
			}

			/* Captain found */
			
			node = new PeerNode(socket);
			Log.d(TAG, "Peer!");
			return null;
		}
		
		/* No response, I am the first node of the distributed system and the Captain */
		
		node = new CaptainNode(); 
		Log.d(TAG, "Captain!");

		return null;
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
