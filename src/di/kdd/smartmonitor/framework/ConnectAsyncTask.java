package di.kdd.smartmonitor.framework;

import java.io.IOException;
import java.net.Socket;


import android.os.AsyncTask;
import android.util.Log;

public class ConnectAsyncTask extends AsyncTask<Void, Void, Socket> implements IObservable {

	String ip;
	IObserver ds;
	
	private static final String TAG = "connect task";
	
	enum ConnectionStatus { ConnectedAsMaster, ConnectedAsPeer, FailedToConnect };
	
	public ConnectAsyncTask() {		
	}
	
	/***
	 * Force connection at specified IP address 
	 * @param ip IP address to connect to
	 */
	
	public ConnectAsyncTask(String ip) {
		this.ip = ip;
	}
	
	/* IObservable implementation */
	
	@Override
	public void subscribe(IObserver observer) {
		this.ds = observer;
	}

	@Override
	public void unsubscribe(IObserver observer) {
		this.ds = null;
	}

	@Override
	public void notify(String message) {
		if(ds != null) {
			ds.update(message);
		}
	}

	/***
	 * Sends JOIN messages to the first 255 local IP addresses and
	 * according to if it will get a response or not, the node becomes
	 * a peer or a Master respectively 
	 */

	@Override
	protected Socket doInBackground(Void... params) {
		Socket socket;
		String ipPrefix = "192.168.1."; //TODO FIXME
		
		android.os.Debug.waitForDebugger();

		if(ip == null) {		
			/* Look for the Master in the first 255 local IP addresses */
			//TODO parallelize it
			for(int i = 1; i < 256; ++i) {
				String tempIP = ipPrefix + Integer.toString(i);

				try{
					Log.i(TAG, "Trying to connect to :" + ipPrefix + Integer.toString(i));

					socket = new Socket(tempIP, ISmartMonitor.JOIN_PORT);
				}
				catch(IOException e) {
					Log.i(TAG, "Failed to connect at " + tempIP);
					continue;
				}
	
				/* Master found */
				
				return socket;
			}
			
			/* No response, this is the first node of the distributed system and the Master */
	
			return null;
		}
		else {
			/* Send JOIN request at specific IP address */

			try {
				socket = new Socket(ip, ISmartMonitor.JOIN_PORT);
				
				return socket;
			}
			catch(IOException e) {
				Log.e(TAG, "Failed to connect at " + ip);
				e.printStackTrace();

				return null;
			}
		}
	}	

	@Override
	protected void onPostExecute(Socket result) {
		if(result == null && ip == null) {
			notify(ConnectionStatus.ConnectedAsMaster.toString());
		}
		else if (result == null && ip != null){
			notify(ConnectionStatus.FailedToConnect.toString());
		}
		else {
			((DistributedSystem) ds).setPeerJoinSocket(result);
			notify(ConnectionStatus.ConnectedAsPeer.toString());
		}
	}
}
