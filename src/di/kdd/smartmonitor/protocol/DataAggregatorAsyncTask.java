package di.kdd.smartmonitor.protocol;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

public class DataAggregatorAsyncTask extends AsyncTask {
	private List<Socket> sockets;
	
	private static final String TAG = "data aggregator";
	
	public DataAggregatorAsyncTask(List<Socket> sockets, MasterNode master) {
		this.sockets = sockets;
	}
	
	@Override
	protected Object doInBackground(Object... arg0) {
		List<Message> peakMessages = new ArrayList<Message>();
		
		/* Receive peaks from each node */
		
		for(Socket socket : sockets) {
			try {
				peakMessages.add(Node.receive(socket));
			} catch (IOException e) {
				Log.e(TAG, "Failed to receive peaks of a node: " + e.getMessage());
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		/* linear K-means on Axis X */
		
		
		
		/* linear K-means on Axis Y */
		
			
		/* linear K-means on Axis Z */
		
		
		return null;
	}	
}
