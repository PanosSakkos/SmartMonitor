package di.kdd.smartmonitor.protocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import android.util.Log;
import di.kdd.smartmonitor.IObserver;
import di.kdd.smartmonitor.protocol.ISmartMonitor.Tag;

public final class PeerNode extends Node implements Runnable, IObserver {
	private Socket joinSocket;
	private ServerSocket commandsServerSocket;
	private PeerHeartbeatsTimerTask heartbeatsTimerTask;
	private Timer timer;
	
	private TimeSynchronization timeSync = new TimeSynchronization();

	private DistributedSystem ds;
	
	private static final String TAG = "peer";

	/***
	 * Sends a JOIN message to the Master node and if it gets accepted,
	 * starts a thread that accepts commands from the Master
	 * @param joinSocket The connected to the Master node socket
	 */

	public PeerNode(DistributedSystem ds, Socket joinSocket) {		
		this.ds = ds;
		this.joinSocket = joinSocket;		

		/* Start command-serving thread */

		new Thread(this).start();
	}

	/***
	 * Accepts a socket connection on the COMMAND_PORT and waits for commands from the Master
	 */

	@Override
	public void run() {
		Message message;
		Socket masterSocket;

		android.os.Debug.waitForDebugger();

		/* Start heartbeats timer task */

		try {
			heartbeatsTimerTask = new PeerHeartbeatsTimerTask(joinSocket.getInetAddress().toString());
			heartbeatsTimerTask.subscribe(this);
			timer = new Timer();
			timer.schedule(heartbeatsTimerTask, new Date(), ISmartMonitor.HEARTBEAT_PERIOD);	
			
			Log.i(TAG, "Started heartbeats");
		}
		catch (Exception e) {
			Log.e(TAG, "Failed to connect at heartbeats port at " + joinSocket.getInetAddress().toString());
			e.printStackTrace();
			
			return;
		}
		
		try {
			commandsServerSocket = new ServerSocket(ISmartMonitor.COMMAND_PORT);		
			commandsServerSocket.setReuseAddress(true);
		}
		catch(IOException e) {
			Log.e(TAG, "Failed to bind command server socket");
			e.printStackTrace();

			return;
		}

		/* Keep Master's IP address */
		
		peerData.setMasterIP(joinSocket.getInetAddress().toString());
		
		Log.i(TAG, "Joining the system");
		
		try {
			/* The Master node was found, send the JOIN message */
	
			message = new Message(Tag.JOIN);
			send(joinSocket, message);	
	
			/* Receive PEER_DATA */
			
			message = receive(joinSocket, Tag.PEER_DATA);
			
			/* Receive TIME_SYNC */
	
			message = receive(joinSocket, Tag.SYNC);
		}
		catch(Exception e) {
			Log.e(TAG, "Failed to join the system");
			e.printStackTrace();
			
			return;
		}
		finally {
			try {
				joinSocket.close();
			}
			catch(Exception e) {				
			}
		}
		
		Log.i(TAG, "Starting serving commands");

		try {
			masterSocket = commandsServerSocket.accept();
		}
		catch(IOException e) {
			Log.e(TAG, "Failed to accept socket for serving commands");
			e.printStackTrace();
			
			return;
		}
		
		Log.i(TAG, "Accepted command socket from " + masterSocket.getInetAddress().toString());

		/* Listen on MasterSocket for incoming commands from the Master */

		Log.i(TAG, "Listening for commands from the Master node");

		while(!this.isInterrupted()) {
			try {		
				message = receive(masterSocket);

				switch(message.getTag()) {
				case PEER_DATA:
					Log.i(TAG, "Received PEER_DATA command");

					peerData.addPeersFromMessage(message);					
					break;
				case SYNC:
					Log.i(TAG, "Received SYNC command");

					timeSync.timeReference(Long.parseLong(message.getPayload()));
					break;
				case NEW_PEER:
					Log.i(TAG, "Received NEW_PEER command");	

					peerData.addPeerIP(message.getPayload());
					break;
				case START_SAMPLING:
					Log.i(TAG, "Received START_SAMPLING command");

					ds.startSamplngCommand();
					break;
				case STOP_SAMPLING:
					Log.i(TAG, "Received STOP_SAMPLING command");

					ds.stopSamplingCommand();
					break;
				case SEND_PEAKS:
					List<Float> modalFrequencies;

					Message peaksMessage = new Message(Tag.AGGREGATE_PEAKS);

					Log.i(TAG, "Received SEND_PEAKS command");
										
					modalFrequencies = ds.computeModalFrequenciesCommand();
					
					for(Float frequency : modalFrequencies) {
						peaksMessage.addToPaylod(Float.toString(frequency));
					}

					Node.send(masterSocket, peaksMessage);
					
					Log.i(TAG, "Sent modal frequencies to Master node");
					break;
				case DELETE_DATA:
					Log.i(TAG, "Received DELETE_DATA command");
					
					ds.deleteDatabaseCommand();
					break;
				case DUMP_DATA:
					Log.i(TAG, "Received DUMP_DATA command");
					
					ds.dumpDatabaseCommand();
					break;
				default:
					Log.e(TAG, "Not implemented Tag handling: " + message.getTag().toString());
					break;
				}
			}
			catch(IOException e) {
				Log.e(TAG, "Error while listening to commands from Master node");
				e.printStackTrace();
			}
			catch(ClassNotFoundException e) {
				Log.e(TAG, "Error while receiving data");
				e.printStackTrace();
			}
			catch (Exception e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Override
	public void disconnect() {
		this.interrupt();

		try {
			commandsServerSocket.close();			
		}
		catch(IOException e) {			
		}
	}

	@Override
	public boolean isMaster() {
		return false;
	}

	@Override
	public void update(String message) {
		
		/* Master failed, disconnect and recover */
		
		ds.disconnectAndRecover();
	}
}