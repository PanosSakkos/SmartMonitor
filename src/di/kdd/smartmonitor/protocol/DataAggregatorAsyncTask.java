package di.kdd.smartmonitor.protocol;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import di.kdd.smartmonitor.Acceleration.AccelerationAxis;
import di.kdd.smartmonitor.protocol.ISmartMonitor.Tag;

import android.os.AsyncTask;
import android.util.Log;

public class DataAggregatorAsyncTask extends AsyncTask {
	private List<Socket> peerSockets;
	MasterNode master;
	
	private static final String TAG = "data aggregator";
	
	public DataAggregatorAsyncTask(List<Socket> sockets, MasterNode master) {
		this.peerSockets = sockets;
		this.master = master;
	}
	
	@Override
	protected Object doInBackground(Object... arg0) {
		List<Message> peakMessages = new ArrayList<Message>();
		
		Log.i(TAG, "Aggregating peaks");
		
		/* Receive peaks from each node */
		
		for(Socket peerSocket : peerSockets) {
			try {
				peakMessages.add(Node.receive(peerSocket));
			} 
			catch (IOException e) {
				Log.e(TAG, "Failed to receive peaks of a node: " + e.getMessage());
				e.printStackTrace();
			} 
			catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		/* Parse peaks from messages */
		
		List<Float> xAxisPeaks = new ArrayList<Float>();
		List<Float> yAxisPeaks = new ArrayList<Float>();
		List<Float> zAxisPeaks = new ArrayList<Float>();
		
		for(Message message : peakMessages) {
			
			/* Parse peaks for each Axis (X, Y and Z) */
			
			int i;
			
			for(i = 0; i < ISmartMonitor.NO_PEAKS; i++) {
				xAxisPeaks.add(Float.parseFloat(message.getPayloadAt(i)));
			}
			
			for(; i < 2 * ISmartMonitor.NO_PEAKS; i++) {
				yAxisPeaks.add(Float.parseFloat(message.getPayloadAt(i)));
			}
			
			for(; i < 3 * ISmartMonitor.NO_PEAKS; i++) {
				zAxisPeaks.add(Float.parseFloat(message.getPayloadAt(i)));
			}			
		}
		
		/* Add this node's peaks */

		int i;

		for(i = 0; i < ISmartMonitor.NO_PEAKS; i++) {
			xAxisPeaks.add(master.getModalFrequencies().get(i));
		}

		for(; i < 2 * ISmartMonitor.NO_PEAKS; i++) {
			yAxisPeaks.add(master.getModalFrequencies().get(i));
		}

		for(; i < 3 * ISmartMonitor.NO_PEAKS; i++) {
			zAxisPeaks.add(master.getModalFrequencies().get(i));
		}

		List<Float> xGlobalModalFrequencies;
		List<Float> yGlobalModalFrequencies;
		List<Float> zGlobalModalFrequencies;
		
		FrequencyClustering.clusterFrequencies(ISmartMonitor.OUTPUT_PEAKS, xAxisPeaks);
		xGlobalModalFrequencies = FrequencyClustering.getMeans();
		master.setModalFrequencies(AccelerationAxis.X, xGlobalModalFrequencies);
		
		FrequencyClustering.clusterFrequencies(ISmartMonitor.OUTPUT_PEAKS, yAxisPeaks);
		yGlobalModalFrequencies = FrequencyClustering.getMeans();
		master.setModalFrequencies(AccelerationAxis.Y, yGlobalModalFrequencies);

		FrequencyClustering.clusterFrequencies(ISmartMonitor.OUTPUT_PEAKS, zAxisPeaks);
		zGlobalModalFrequencies = FrequencyClustering.getMeans();
		master.setModalFrequencies(AccelerationAxis.Z, zGlobalModalFrequencies);

		/* Replicate modal frequencies to the rest of the system's nodes */
		
		Message modalFrequenciesMessage = new Message(Tag.MODAL_FREQUENCIES);
		
		for(Float frequency : xGlobalModalFrequencies) {
			modalFrequenciesMessage.addToPaylod(Float.toString(frequency));
		}
		
		for(Float frequency : yGlobalModalFrequencies) {
			modalFrequenciesMessage.addToPaylod(Float.toString(frequency));
		}
		
		for(Float frequency : zGlobalModalFrequencies) {
			modalFrequenciesMessage.addToPaylod(Float.toString(frequency));
		}
		
		for(Socket peerSocket : peerSockets) {
			try {
				Node.send(peerSocket, modalFrequenciesMessage);
			} 
			catch (IOException e) {
				Log.e(TAG, "Failed to replicate modal frequencies to a peer node");
				e.printStackTrace();
			}
		}
		
		return null;
	}	
}
