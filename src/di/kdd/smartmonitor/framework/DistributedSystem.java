package di.kdd.smartmonitor.framework;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.lang.Byte;

import android.os.Environment;
import android.util.Log;

import di.kdd.smartmonitor.framework.Acceleration.AccelerationAxis;
import di.kdd.smartmonitor.framework.ConnectAsyncTask.ConnectionStatus;
import di.kdd.smartmonitor.framework.exceptions.ConnectException;
import di.kdd.smartmonitor.framework.exceptions.DatabaseException;
import di.kdd.smartmonitor.framework.exceptions.MasterException;
import di.kdd.smartmonitor.framework.exceptions.SamplerException;

public class DistributedSystem implements ISmartMonitor, IObservable, IObserver {
	private ISampler sampler;
	private List<IObserver> observers = new ArrayList<IObserver>();

	private Node node;
	
	private long startSamplingTimestamp;
	private long stopSamplingTimestamp;
	
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
		
		node = new MasterNode(this); 
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
	public void removeSampler() {
		this.sampler = null;
	}
	
	@Override
	public void startSampling() throws MasterException, ConnectException, SamplerException {		
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
		isSampling = true;
		startSamplingTimestamp = System.currentTimeMillis();
		
		notify("Started sampling");
	}

	@Override
	public void stopSampling() throws MasterException, ConnectException, SamplerException {
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
		isSampling = false;
		stopSamplingTimestamp = System.currentTimeMillis();

		notify("Stoped sampling");
	}
	
	@Override
	public boolean isSampling() {
		return isSampling;
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
		db.deleteDatabase();
		
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
		db.dumpToFile();
		
		notify("Dumped database");		
	}
	
	@Override
	public void computeModalFrequencies() throws MasterException, ConnectException {
		if(node == null) {
			throw new ConnectException();
		}

		if(node.isMaster() == false) {
			throw new MasterException();
		}
		
		((MasterNode) node).computeModalFrequencies();
		
		notify("Computing modal frequencies");
	}

	public void computeModalFrequencies(long from, long to) throws MasterException, ConnectException {
		if(node == null) {
			throw new ConnectException();
		}

		if(node.isMaster() == false) {
			throw new MasterException();
		}
		
		((MasterNode) node).computeModalFrequencies(from, to);
		
		notify("Computing modal frequencies");
	}

	
	@Override
	public List<Float> getModalFrequencies(AccelerationAxis axis) {
		return node.getAxisFrequencies(axis);
	}
	
	@Override
	public boolean isMaster() {
		return (node != null && node.isMaster());
	}

	@Override
	public String getMasterIP() {
		return (node != null) ? "None" : node.getLowestIP();
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

			node = new MasterNode(this);		
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
		if(sampler != null) {
			sampler.startSamplingService();
			isSampling = true;
			startSamplingTimestamp = System.currentTimeMillis();
			
			notify("Started sampling");
		}
	}
	
	protected void stopSamplingCommand() {
		if(sampler != null && isSampling) {
			sampler.stopSamplingService();
			isSampling = false;
			stopSamplingTimestamp = System.currentTimeMillis();
			
			notify("Stoped sampling");
		}
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
	
	private float computeSamplingFrequencyInSample(long from, long to, List<Acceleration> accelerations) {		
		return (float) accelerations.size() / ((float) (to - from) / 1000);		
	}
	
	private List<Float> computeModalFrequenciesInAxis(long from, long to, AccelerationAxis axis) {		
		List<Float> modalFrequencies = new ArrayList<Float>();

		try {
			int fftLength;
			float samplingFrequency;
			Double[] output;
			Complex[] fftResults;

			List<Acceleration> accelerations = db.getAccelerationsIn(from, to, axis);
			
			/* Find the closest power of 2 to the size of accelerations 
			 * (FFT must run on data with size being a power of 2)
			 */

			int closestPowerOfTwo = 1;
			
			while(closestPowerOfTwo <= accelerations.size()) {
				closestPowerOfTwo *= 2;
			}

			fftLength = closestPowerOfTwo / 2;
			
			Log.i(TAG, "FFT length: " + fftLength);
			
			/* Keep only the useful accelerations */
			
			while(accelerations.size() > fftLength) {
				accelerations.remove(accelerations.size() - 1);
			}
			
			fftResults = new Complex[fftLength];
			output = new Double[fftLength / 2];
			
			/* Create FFT's input (Complex number with Real part the acceleration and zero Imaginary part */

			for(int i = 0; i < fftLength; i++) {
				fftResults[i] = new Complex(accelerations.get(i).getAcceleration(), 0);
			}
			
			/* Compute the FFT output */
			
			fftResults = FFT.fft(fftResults);

			for(int i = 0; i < fftLength / 2; i++) {
				output[i] = Math.sqrt(Math.pow(fftResults[i].re(), 2) + Math.pow(fftResults[i].im(), 2));
			}
			
			/* Ignore first output value... */
			
			output[0] = 0.0;
			
			Log.i(TAG, "Ran FFT on timeseries");
			
			if(ISmartMonitor.DUMP) {
				Log.i(TAG, "Dumping FFT output for axis " + axis.toString());
				
				switch(axis) {
				case X:
					dumpListToFile(Arrays.asList(output), ISmartMonitor.DUMP_X_FREQUENCIES_FILENAME);
				case Y:
					dumpListToFile(Arrays.asList(output), ISmartMonitor.DUMP_Y_FREQUENCIES_FILENAME);
				case Z:
					dumpListToFile(Arrays.asList(output), ISmartMonitor.DUMP_Z_FREQUENCIES_FILENAME);					
				}				
			}
			
			/* Find peaks in the FFT's output, within each window */
			
			int peakPeekingWindow = output.length / NO_WINDOWS;
			List<Integer> maxIndices = new ArrayList<Integer>();
			
			Log.i(TAG, "Looking for peaks in FFT's output (Number of Windows: " + Integer.toString(NO_WINDOWS) 
					+ " PP Window: " + Integer.toString(peakPeekingWindow) + ")");
			
			for(int i = 0; i < NO_WINDOWS; i++) {

				/* Find the index of the window's max value */
				
				int windowStart = peakPeekingWindow * i;
				int windowEnd = windowStart + peakPeekingWindow;
				
				int windowMaxIndex = 0;
				double windowMaxValue = 0.0f;
				
				for(int j = windowStart; j <= windowEnd; j++) {
					if(output[j] > windowMaxValue) {
						windowMaxIndex = j;
						windowMaxValue = output[j];
					}
				}

				maxIndices.add(windowMaxIndex);
			}
			
			/* Keep the top NO_PEAKS indices */
			
			Collections.sort(maxIndices);
			
			while(maxIndices.size() > NO_PEAKS) {
				maxIndices.remove(0);
			}
			
			Log.i(TAG, "Found peaks at: ");
			
			for(Integer i : maxIndices) {
				Log.i(TAG, Integer.toString(i));
			}
			
			/* Convert index values of the peaks to frequency bins */
			
			samplingFrequency = computeSamplingFrequencyInSample(from, to, accelerations);

			Log.i(TAG, "Sampling frequency: " + Float.toString(samplingFrequency));
			
			for(Integer index : maxIndices) {
				modalFrequencies.add((float) (index * samplingFrequency) / fftLength);
			}
						
			Log.i(TAG, "Converted indexes to frequency bins:");
			
			for(Float frequency : modalFrequencies) {
				Log.i(TAG, Float.toString(frequency));
			}

			Log.i(TAG, "Computed modal frequencies of axis " + axis.toString());
			
		} catch (Exception e) {
			Log.e(TAG, "Error while computing modal frequencies of axis " + axis.toString() + 
					" between timestamps: " + Long.toString(from) + " and " + Long.toString(to) + ": " + e.getMessage());
			e.printStackTrace();
		}
		
		return modalFrequencies;
	}

	protected List<Float> computeModalFrequenciesCommand() throws DatabaseException {
		ArrayList<Float> modalFrequencies = new ArrayList<Float>();
		
		if(db == null) {
			notify("Cannot compute modal frequencies, database is not set");
			return null;
		}

		/* Compute the modal frequencies of each axis, within the period of sampling */

		modalFrequencies.addAll(computeModalFrequenciesInAxis(startSamplingTimestamp, stopSamplingTimestamp, AccelerationAxis.X));
		modalFrequencies.addAll(computeModalFrequenciesInAxis(startSamplingTimestamp, stopSamplingTimestamp, AccelerationAxis.Y));
		modalFrequencies.addAll(computeModalFrequenciesInAxis(startSamplingTimestamp, stopSamplingTimestamp, AccelerationAxis.Z));

		notify("Computed modal frequencies");

		if(node.isMaster()) {
			((MasterNode) node).aggregatePeaks();
			notify("Aggregating peaks");
		}
		
		return modalFrequencies;
	}
		
	public List<Float> computeModalFrequenciesCommand(long from, long to) {
		ArrayList<Float> modalFrequencies = new ArrayList<Float>();
		
		if(db == null) {
			notify("Cannot compute modal frequencies, database is not set");
			return null;
		}

		/* Compute the modal frequencies of each axis, within the given times */

		modalFrequencies.addAll(computeModalFrequenciesInAxis(from, to, AccelerationAxis.X));
		modalFrequencies.addAll(computeModalFrequenciesInAxis(from, to, AccelerationAxis.Y));
		modalFrequencies.addAll(computeModalFrequenciesInAxis(from, to, AccelerationAxis.Z));

		notify("Computed modal frequencies");

		if(node.isMaster()) {
			((MasterNode) node).aggregatePeaks();
			notify("Aggregating peaks");
		}
		
		return modalFrequencies;
	}

	
	/***
	 * In case the Master node is offline, find the new Master node
	 * and connect.
	 */
	
	protected void disconnectAndRecover() {
		
		/* Master node is down, forget his IP address */
		
		node.forgetMasterIP();		
		
		if(node.getLowestIP() == node.getNodeIP()) {
			
			/* This node has to be the new Master */

			connectAsMaster();
		}
		else {
			
			/* Connect at the lowest IP address */
			
			connectAt(node.getLowestIP());
		}
	}	

	private static final String SMART_MONITOR_FOLDER = "SmartMonitor";

	/***
	 * Creates the SMART_MONITOR_FOLDER, if it doesn't exist
	 */
	
	private void createSmartMonitorFolder() {
		File folder = new File(Environment.getExternalStoragePublicDirectory(""), SMART_MONITOR_FOLDER);
		
		if(!folder.exists()) {
			folder.mkdirs();
			
			Log.i(TAG, "Created " + SMART_MONITOR_FOLDER + " folder");
		}			
	}	
	
	private void dumpListToFile(List<Double> list, String filename) {
		File dumpFile;
		PrintWriter printWriter;

		createSmartMonitorFolder();
		
		try {
			
			dumpFile = new File(Environment.getExternalStoragePublicDirectory(SMART_MONITOR_FOLDER), filename);
			printWriter = new PrintWriter(new FileWriter(dumpFile, false));

			for(Double data : list) {
				printWriter.println(data);
			}

			printWriter.close();
		}
		catch(Exception e) {
			Log.e(TAG, "Failed to dump list to file " + filename);
			e.printStackTrace();
		}
	}
}
