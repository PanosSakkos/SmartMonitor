package di.kdd.smartmonitor.protocol;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import di.kdd.smartmonitor.Acceleration.AccelerationAxis;

import android.os.AsyncTask;
import android.util.Log;

public class DataAggregatorAsyncTask extends AsyncTask {
	private List<Socket> sockets;
	MasterNode master;
	
	private static final String TAG = "data aggregator";
	
	public DataAggregatorAsyncTask(List<Socket> sockets, MasterNode master) {
		this.sockets = sockets;
		this.master = master;
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

		/* Parse peaks from messages */
		
		List<Float> xAxisPeaks = new ArrayList<Float>();
		List<Float> yAxisPeaks = new ArrayList<Float>();
		List<Float> zAxisPeaks = new ArrayList<Float>();
		
		for(Message message : peakMessages) {

			/* Parse peaks for each Axis (X, Y and Z) */
			
			for(int i = 0; i < ISmartMonitor.NO_PEAKS; i++) {
				xAxisPeaks.add(Float.parseFloat(message.getPayloadAt(i)));
			}
			
			for(int i = ISmartMonitor.NO_PEAKS; i < 2 * ISmartMonitor.NO_PEAKS; i++) {
				yAxisPeaks.add(Float.parseFloat(message.getPayloadAt(i)));
			}
			
			for(int i = 0; i < 3 * ISmartMonitor.NO_PEAKS; i++) {
				zAxisPeaks.add(Float.parseFloat(message.getPayloadAt(i)));
			}
			
		}
		
		FrequencyClustering frequencyClustering; 

		frequencyClustering = new FrequencyClustering(ISmartMonitor.OUTPUT_PEAKS, xAxisPeaks);
		frequencyClustering.cluster();
		master.setModalFrequencies(AccelerationAxis.X, frequencyClustering.getMeans());
		
		frequencyClustering = new FrequencyClustering(ISmartMonitor.OUTPUT_PEAKS, yAxisPeaks);
		frequencyClustering.cluster();
		master.setModalFrequencies(AccelerationAxis.Y, frequencyClustering.getMeans());
		
		frequencyClustering = new FrequencyClustering(ISmartMonitor.OUTPUT_PEAKS, zAxisPeaks);
		frequencyClustering.cluster();
		master.setModalFrequencies(AccelerationAxis.Z, frequencyClustering.getMeans());
				
		return null;
	}	
}
