package di.kdd.smartmonitor.protocol;

import java.io.IOException;
import java.net.Socket;

import android.os.AsyncTask;
import android.util.Log;

public class ConnectTask extends AsyncTask<Void, Void, Socket> {

	Socket socket;
	DistributedSystem ds;
	
	private static final String TAG = "connect task";
	
	public ConnectTask(DistributedSystem ds) {
		this.ds = ds;
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

		/* Look for the Master in the first 255 local IP addresses */
		//TODO parallelize it
		for(int i = 1; i < 256; ++i) {
			try{
				Log.i(TAG, "Trying to connect to :" + ipPrefix + Integer.toString(i));
				socket = new Socket(ipPrefix + Integer.toString(i), ISmartMonitor.JOIN_PORT);
			}
			catch(IOException e) {
				continue;
			}

			/* Master found */
			
			return socket;
		}
		
		/* No response, this is the first node of the distributed system and the Master */

		return null;
	}
	
	@Override
	protected void onPostExecute(Socket result) {
		if(result == null) {
			ds.connectedAsMaster();
		}
		else {
			ds.connectedAsPeer(socket);
		}
	}
}
