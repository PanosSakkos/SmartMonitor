package di.kdd.smartmonitor.protocol;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;

import android.os.AsyncTask;
import android.util.Log;

import di.kdd.smartmonitor.MainActivity;
import di.kdd.smartmonitor.protocol.exceptions.MasterException;

public class DistributedSystem extends AsyncTask<Void, Void, Boolean> implements IProtocol {
	private MainActivity view;
	private DistributedSystemNode node;
	
	private static final String TAG = "distributed system";
		
	public DistributedSystem(MainActivity view) {
		this.view = view;
	}
	
	public void connect() {
		this.execute();
	}

	@Override
	public void connectAsMaster() {
		node = new MasterNode(); 
		
		view.showMessage("Connected as Master");			
	}

	@Override
	public void connectAt(String ip){
		Socket socket;

		try{
			socket = new Socket(ip, IProtocol.JOIN_PORT);
			node = new PeerNode(socket);
			
			view.showMessage("Connected as Peer");						
		}
		catch(IOException e) {
			view.showMessage("Failed to connect as Peer");						
		}		
	}	
	
	@Override
	public void disconnect() {
		node.disconnect();
	}

	public void startSampling() throws MasterException {
		if(node.isMaster() == false) {
			throw new MasterException();
		}

		//TODO
	}

	/***
	 * Broadcast a STOP_SAMPLING command to all the peers and 
	 * stops sampling itself. 
	 * @throws MasterException If the asked node is not the Master node
	 */
	
	public void stopSampling() throws MasterException {
		if(node.isMaster() == false) {
			throw new MasterException();
		}

		//TODO
	}
	

	/***
	 * Sends Knock-Knock messages to the first 255 local IP addresses and
	 * according to if it will get a response or not, the node becomes
	 * a peer or a Master respectively 
	 */

	@Override
	protected Boolean doInBackground(Void... arg0) {
		Socket socket;
		String ipPrefix = "192.168.1."; //TODO FIXME
		
		android.os.Debug.waitForDebugger();

		/* Look for the Master in the first 255 local IP addresses */
		//TODO parallelize it
		for(int i = 1; i < 256; ++i) {
			try{
				Log.d(TAG, "Trying to connect to :" + ipPrefix + Integer.toString(i));
				socket = new Socket(ipPrefix + Integer.toString(i), IProtocol.JOIN_PORT);
			}
			catch(IOException e) {
				continue;
			}

			/* Master found */
			
			node = new PeerNode(socket);
			
			return true;
		}
		
		/* No response, I am the first node of the distributed system and the Master */
		
		node = new MasterNode(); 

		return false;
	}
	
	/***
	 * Runs after the doInBackground methods returns. The became peer
	 * parameter indicates if the node connected as a Master or as a Peer and
	 * sends a notification to the subscribed view.
	 */
	
	@Override
	protected void onPostExecute(Boolean becamePeer) {
		if(becamePeer) {
			view.showMessage("Connected as Peer");		
		}
		else {
			view.showMessage("Connected as Master");			
		}
	}
	
	@Override
	public void computeBuildingSignature(Date from, Date to) throws MasterException, IOException {
		if(node.isMaster() == false) {
			throw new MasterException();
		}
		
		((MasterNode) node).computeBuildingSignature(from, to);
	}

	@Override
	public boolean isMaster() {
		return node.isMaster();
	}

	@Override
	public String getMasterIP() {
		return node.getMasterIP();
	}
}