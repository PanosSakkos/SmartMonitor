package di.kdd.smartmonitor.protocol;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;

import android.os.AsyncTask;
import android.util.Log;

import di.kdd.smartmonitor.MainActivity;
import di.kdd.smartmonitor.protocol.exceptions.ConnectException;
import di.kdd.smartmonitor.protocol.exceptions.MasterException;

public class DistributedSystem implements IProtocol {
	/* The view that is interested to our events */
	
	private MainActivity view;

	private DistributedSystemNode node;
	
	private long samplingStarted, samplingEnded;
	
	/* States */
	
	private boolean isConnected;
	private boolean isSampling;
	
	private static final String TAG = "distributed system";
		
	public DistributedSystem(MainActivity view) {
		this.view = view;
	}

	/***
	 * Handler to be called from the ConnectTask, if the node didn't get a JOIN response
	 */
	
	protected void connectedAsMaster() {
		Log.i(TAG, "Connected as Master");

		node = new MasterNode();		
		isConnected = true;
		view.showMessage("Connected as Master");		
	}

	/***
	 * Handler to be called from the ConnectTask, if the node got repsonse to JOIN message
	 * @param socket The connected to the Master node socket
	 */
	
	protected void connectedAsPeer(Socket socket) {
		Log.i(TAG, "Connected as Peer");

		node = new PeerNode(socket);
		isConnected = true;
		
		view.showMessage("Connected as Peer");		
	}
	
	@Override
	public void connect() {
		Log.i(TAG, "Connecting");
		
		ConnectTask connectTask = new ConnectTask(this);
		connectTask.execute();
	}

	@Override
	public void connectAsMaster() {
		Log.i(TAG, "Connect as Master");
		
		node = new MasterNode(); 
		isConnected = true;
		
		view.showMessage("Connected as Master");			
	}

	@Override
	public void connectAt(String ip) {
		Socket socket;

		Log.i(TAG, "Connecting as Peer at " + ip);
		
		try{
			socket = new Socket(ip, IProtocol.JOIN_PORT);
			node = new PeerNode(socket);
			isConnected = true;
		
			view.showMessage("Connected as Peer");						
		}
		catch(IOException e) {
			view.showMessage("Failed to connect as Peer");
		}		
	}	
	
	@Override
	public boolean isConnected() {
		return isConnected;
	}
	
	@Override
	public void disconnect() {
		Log.i(TAG, "Disconnecting");
		
		node.disconnect();
		isConnected = false;
	}

	@Override
	public void startSampling() throws MasterException, IOException, ConnectException {		
		if(node == null) {
			throw new ConnectException();
		}

		if(node.isMaster() == false) {
			throw new MasterException();
		}
		
		((MasterNode) node).startSampling();			
		
		view.startSamplingService();
		samplingStarted = System.currentTimeMillis();		
		isSampling = true;
	}

	@Override
	public void stopSampling() throws MasterException, IOException, ConnectException {
		if(node == null) {
			throw new ConnectException();
		}

		if(node.isMaster() == false) {
			throw new MasterException();
		}
		
		((MasterNode) node).stopSampling();			
		
		view.stopSamplingService();
		samplingEnded = System.currentTimeMillis();		
		isSampling = false;
	}
	
	public boolean isSampling() {
		return isSampling;
	}
		
	@Override
	public void computeModalFrequencies(Date from, Date to) throws MasterException, IOException, ConnectException {
		if(node == null) {
			throw new ConnectException();
		}

		if(node.isMaster() == false) {
			throw new MasterException();
		}
		
		((MasterNode) node).computeModalFrequencies(from, to);
	}

	@Override
	public boolean isMaster() {
		return (node !=null && node.isMaster());
	}

	@Override
	public String getMasterIP() {
		return (node != null) ? "None" : node.getMasterIP();
	}
}
