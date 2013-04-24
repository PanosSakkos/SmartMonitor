package di.kdd.smartmonitor.protocol;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.util.Log;

import di.kdd.smartmonitor.AccelerationsSQLiteHelper;
import di.kdd.smartmonitor.IObservable;
import di.kdd.smartmonitor.IObserver;
import di.kdd.smartmonitor.ISampler;
import di.kdd.smartmonitor.protocol.ConnectAsyncTask.ConnectionStatus;
import di.kdd.smartmonitor.protocol.exceptions.ConnectException;
import di.kdd.smartmonitor.protocol.exceptions.MasterException;
import di.kdd.smartmonitor.protocol.exceptions.SamplerException;

public class DistributedSystem implements ISmartMonitor, IObservable, IObserver {
	private ISampler sampler;
	private List<IObserver> observers = new ArrayList<IObserver>();

	private DistributedSystemNode node;
	
	private long samplingStarted, samplingEnded;
	
	/* States */
	
	private boolean isConnected;
	private boolean isSampling;
	
	private static final String TAG = "distributed system";
	
	private AccelerationsSQLiteHelper db;
	
	public void setDatabase(AccelerationsSQLiteHelper db) {
		this.db = db;
	}
	
	/* Singleton implementation */

	private static DistributedSystem ds;
	
	private DistributedSystem() {
	}
	
	public static DistributedSystem getInstance() {
		if(ds == null) {
			ds = new DistributedSystem();
		}
		
		return ds;
	}

	/* IObservable implementation */
	
	@Override
	public void unsubscribe(IObserver observer) {
		observers.remove(observer);
	}

	@Override
	public void subscribe(IObserver observer) {
		observers.add(observer);		
	}
	
	@Override
	public void notify(String message) {
		for(IObserver observer : observers) {
			observer.update(message);
		}
	}

	/* ISmartMonitor implementation */
		
	@Override
	public void connect() {
		Log.i(TAG, "Connecting");
		
		ConnectAsyncTask connectTask = new ConnectAsyncTask();
		connectTask.subscribe(this);
		connectTask.execute();
	}

	@Override
	public void connectAsMaster() {
		Log.i(TAG, "Connecting as Master");
		
		node = new MasterNode(); 
		isConnected = true;
		
		notify("Connected as Master");			
	}

	@Override
	public void connectAt(String ip) {
		Log.i(TAG, "Connecting as Peer at " + ip);

		ConnectAsyncTask connectTask = new ConnectAsyncTask(ip);
		connectTask.subscribe(this);
		connectTask.execute();
	}	
	
	@Override
	public boolean isConnected() {
		return isConnected;
	}
	
	@Override
	public void disconnect() {
		Log.i(TAG, "Disconnecting");
		
		node.disconnect();
		node = null;
		isConnected = false;
		
		notify("Disconnected");
	}

	@Override
	public void setSampler(ISampler sampler) {
		this.sampler = sampler;
	}
	
	@Override
	public void startSampling() throws MasterException, ConnectException, SamplerException, IOException {		
		if(node == null) {
			throw new ConnectException();
		}

		if(node.isMaster() == false) {
			throw new MasterException();
		}
		
		if(sampler == null) {
			throw new SamplerException();
		}

		((MasterNode) node).startSampling();			
		
		sampler.startSamplingService();
		samplingStarted = System.currentTimeMillis();		
		isSampling = true;
		
		notify("Started sampling");
	}

	@Override
	public void stopSampling() throws MasterException, ConnectException, SamplerException, IOException {
		if(node == null) {
			throw new ConnectException();
		}

		if(node.isMaster() == false) {
			throw new MasterException();
		}
		
		if(sampler == null) {
			throw new SamplerException();
		}
		
		((MasterNode) node).stopSampling();			
		
		sampler.stopSamplingService();
		samplingEnded = System.currentTimeMillis();		
		isSampling = false;

		notify("Stoped sampling");
	}
	
	@Override
	public void deleteDatabase() throws MasterException, ConnectException {
		if(node == null) {
			throw new ConnectException();
		}

		if(node.isMaster() == false) {
			throw new MasterException();
		}

		((MasterNode) node).deleteDatabase();

		notify("Deleted database");
	}
	
	@Override
	public void dumpDatabase() throws MasterException, ConnectException {
		if(node == null) {
			throw new ConnectException();
		}

		if(node.isMaster() == false) {
			throw new MasterException();
		}

		((MasterNode) node).dumpDatabase();

		notify("Dumped database");		
	}
	
	@Override
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
		
		notify("Computed modal frequencies");
	}

	@Override
	public boolean isMaster() {
		return (node !=null && node.isMaster());
	}

	@Override
	public String getMasterIP() {
		return (node != null) ? "None" : node.getMasterIP();
	}
	
	private Socket joinSocket;
	
	/***
	 * Allow to the ConnectAsyncTask to pass the connected socket
	 * to the master, in order to join the system
	 * @param joinSocket The connected to the JOIN port socket
	 */
	
	protected void setPeerJoinSocket(Socket joinSocket) {
		this.joinSocket = joinSocket;
	}

	/* IObserver implementation */
	
	@Override
	public void update(String message) {
		switch(ConnectionStatus.valueOf(message)) {
		case ConnectedAsMaster:
			Log.i(TAG, "Connected as Master");

			node = new MasterNode();		
			isConnected = true;
			
			notify("Connected as Master");		
			break;
		case ConnectedAsPeer:
			Log.i(TAG, "Connected as Peer");

			node = new PeerNode(this, joinSocket);
			isConnected = true;
			
			notify("Connected as Peer");		
			break;			
		case FailedToConnect:
			Log.e(TAG, "Failed to connect as Peer");
			notify("Failed to connect as Peer");
			break;
		default:				
		}		
	}
	
	/* Methods that should be called form the peer node, when he receives a command */
	
	protected void startSamplngCommand() {
		sampler.startSamplingService();
		samplingStarted = System.currentTimeMillis();		
		isSampling = true;
		
		notify("Started sampling");
	}
	
	protected void stopSamplingCommand() {
		sampler.stopSamplingService();
		samplingEnded = System.currentTimeMillis();		
		isSampling = false;
		
		notify("Stoped sampling");
	}	
	
	protected void deleteDatabaseCommand() {		
		if(db != null) {
			db.deleteDatabase();
			
			notify("Deleted database");
		}
	}
	
	protected void dumpDatabaseCommand() {
		if(db != null) {
			db.dumpToFile();
			
			notify("Dumped database");
		}		
	}
}
