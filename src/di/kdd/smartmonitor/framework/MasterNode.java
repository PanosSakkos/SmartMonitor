package di.kdd.smartmonitor.framework;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;
import di.kdd.smartmonitor.Acceleration.AccelerationAxis;
import di.kdd.smartmonitor.framework.ISmartMonitor.Tag;
import di.kdd.smartmonitor.framework.exceptions.ConnectException;
import di.kdd.smartmonitor.framework.exceptions.MasterException;
import di.kdd.smartmonitor.framework.exceptions.SamplerException;

public final class MasterNode extends Node implements IObserver {	

	private JoinThread joinThread;
	private MasterHeartbeatsThread heartbeatsThread;
	
	private Timer timeSyncTimer;
	private TimeSynchronizationTimerTask timeSynchronizationTimerTask;

	private List<Socket> commandSockets = new ArrayList<Socket>();

	private List<Float> modalFrequencies;
	
	private static final String TAG = "master";		
		
	public List<Float> getModalFrequencies() {
		return modalFrequencies;
	}
	
	public MasterNode(DistributedSystem ds) {
		peerData.subscribe(this);
		this.ds = ds;

		joinThread = new JoinThread(peerData);
		joinThread.start();
		
		heartbeatsThread = new MasterHeartbeatsThread(peerData);
		heartbeatsThread.start();
		
		timeSynchronizationTimerTask = new TimeSynchronizationTimerTask(commandSockets);
		timeSyncTimer = new Timer();
		timeSyncTimer.schedule(timeSynchronizationTimerTask, new Date(), ISmartMonitor.TIME_SYNC_PERIOD);
	}

	/* IObserver implementation */
	
	/***
	 * Handler to be called by the PeerData instance that this class holds,
	 * when a new IP is added by the Join thread. Sends NEW_PEER commands
	 * to the peers to notify them for the new peer that joined the system
	 * @param ip The IP address of the node that joined the network.
	 */
	
	@Override
	public void update(String ip) {
		Log.i(TAG, "New peer added: " + ip);
		
		try {			
			/* Connect to the peer, in order to establish communication channel for commands */
			
			Socket commandSocket = new Socket(ip, ISmartMonitor.COMMAND_PORT);
			commandSockets.add(commandSocket);
			
			/* Notify peers about the new peer that joined the network */
			
			Message message = new Message(Tag.NEW_PEER, ip);			
			broadcastCommand(message);
		}
		catch (Exception e) {
			Log.e(TAG, "Failed to connect to " + ip);
			peerData.removePeerIP(ip);
		}
	}	
	
	/***
	 * Sends a message to each connected peer
	 * @param message The message to broadcast
	 */
	
	private void broadcastCommand(Message message) {
		Log.i(TAG, "Broadcasting " + message.toString());
		
		BroadcastAsyncTask broadcastAsyncTask = new BroadcastAsyncTask(commandSockets, message);
		broadcastAsyncTask.execute();
	}
	
	class StopSamplingTimerTask extends TimerTask {

		Timer timer = new Timer();
		
		public StopSamplingTimerTask() {
			timer.schedule(this, ISmartMonitor.SAMPLING_TIME_SPAN);
		}
		
		@Override
		public void run() {			
			try {
				if(ds.isSampling()) {
					ds.stopSampling();
				}
			} 
			catch (MasterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			catch (ConnectException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			catch (SamplerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 

			timer.cancel();
		}		
	}
	
	/***
	 * Broadcasts START_SAMPLING command to the peer nodes
	 */
	
	public void startSampling() {
		broadcastCommand(new Message(Tag.START_SAMPLING));
		
		StopSamplingTimerTask stopSamplingTimerTask = new StopSamplingTimerTask();
	}

	/***
	 * Broadcasts STOP_SAMPLING command to the peer nodes
	 */

	public void stopSampling() {
		broadcastCommand(new Message(Tag.STOP_SAMPLING));
	}
	
	/***
	 * Broadcasts SEND_PEAKS command to the peers, gather their frequency peaks and
	 * computes the modal frequencies from all the peaks
	 */
	
	public void computeModalFrequencies() {
		broadcastCommand(new Message(Tag.SEND_RESULTS));

		try {
			modalFrequencies = ds.computeModalFrequenciesCommand();
		}
		catch (Exception e) {
			Log.e(TAG, "Error while computing modal frequencies: " + e.getMessage());
			e.printStackTrace();
		}				
	}	

	public void computeModalFrequencies(long from, long to) {
		Message computeModalFrequenciesMessage = new Message(Tag.SEND_RESULTS);
		computeModalFrequenciesMessage.addToPaylod(Long.toString(from));
		computeModalFrequenciesMessage.addToPaylod(Long.toString(to));
		
		broadcastCommand(computeModalFrequenciesMessage);

		try {
			modalFrequencies = ds.computeModalFrequenciesCommand(from, to);
		}
		catch (Exception e) {
			Log.e(TAG, "Error while computing modal frequencies: " + e.getMessage());
			e.printStackTrace();
		}				

	}	
	
	public void deleteDatabase() {
		broadcastCommand(new Message(Tag.DELETE_DATA));
	}
	
	public void dumpDatabase() {
		broadcastCommand(new Message(Tag.DUMP_DATA));
	}
	
	public void aggregatePeaks() {
		DataAggregatorAsyncTask dataAggregator = new DataAggregatorAsyncTask(commandSockets, this);
		dataAggregator.execute();
	}
	
	@Override
	public void disconnect() {
		Log.i(TAG, "Disconnecting");
		
		if(joinThread != null) {
			joinThread.interrupt();
		}
		
		if(heartbeatsThread != null) {
			heartbeatsThread.interrupt();
		}
		
		timeSyncTimer.cancel();
		
		for(Socket commandSocket : commandSockets) {
			try {
				commandSocket.close();
			} 
			catch (IOException e) {
			}
		}
		
		peerData.unsubscribe(this);
	}
	
	@Override
	public boolean isMaster() {
		return true;
	}
}
