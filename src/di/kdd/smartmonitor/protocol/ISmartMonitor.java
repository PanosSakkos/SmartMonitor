package di.kdd.smartmonitor.protocol;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import di.kdd.smartmonitor.Acceleration.AccelerationAxis;
import di.kdd.smartmonitor.ISampler;
import di.kdd.smartmonitor.protocol.exceptions.ConnectException;
import di.kdd.smartmonitor.protocol.exceptions.MasterException;
import di.kdd.smartmonitor.protocol.exceptions.SamplerException;

public interface ISmartMonitor {
	
	/* The period of the heartbeat messages (in milliseconds) */
	
	static final int HEARTBEATS_PERIOD = 50000;
	
	/* Period of time synchronizing (in milliseconds) */
	
	static final int TIME_SYNC_PERIOD = 50000;
	
	/* The port that the knock knock messages are sent */
	
	static final int JOIN_PORT = 4631;

	/* The port which the heartbeat messages are sent */

	static final int HEARBEATS_PORT = 4632;
	
	/* The port which the Master sends commands 
	 * (including the Master's heartbeats 
	 */

	static final int COMMAND_PORT = 4633;
	
	/* Time (in milliseconds) of acceleration sampling */
	
	static final int SAMPLING_TIME_SPAN = 10000;
	
	/* The (maximum) number of peaks that each peers returns to the Master */
	
	static final int NO_PEAKS = 5;
	
	/* The number of the modal frequencies that the system will report after the computation */
	
	static final int OUTPUT_PEAKS = 5;
		
	/* The number of windows that the peaks will be found in*/
	
	static final int NO_WINDOWS = 10;

	/* If set, dumps the acceleration timeseries and the frequencies */ 
	
	static final boolean DUMP = true;
	
	static final String DUMP_X_FREQUENCIES_FILENAME = "x_frequencies_dump.txt";
	static final String DUMP_Y_FREQUENCIES_FILENAME = "y_frequencies_dump.txt";
	static final String DUMP_Z_FREQUENCIES_FILENAME = "z_frequencies_dump.txt";
	
	/* Message tags */
	
	public enum Tag { JOIN, PEER_DATA, NEW_PEER, SYNC, HEARTBEAT, START_SAMPLING, STOP_SAMPLING, SEND_PEAKS , 
												AGGREGATE_PEAKS, MODAL_FREQUENCIES, DELETE_DATA, DUMP_DATA };
		
	/***
	 * Connects to the distributed system.
	 */
	
	public void connect();
		
	/***
	 * Forces the node to connect to the system as Master.
	 */
	
	public void connectAsMaster();
	
	/***
	 * Forces the node to connect to the system as Peer.
	 * @param ip The Master's node IP address
	 */
	
	public void connectAt(String ip);
	
	
	/***
	 * Asks if the node is connected to the distributed system
	 * @return boolean indicating if the node is connected to the system
	 */
	
	public boolean isConnected();
	
	/***
	 * Disconnect from the system.
	 */
	
	public void disconnect();

	/* Sets the ISampler that will start and stop the sampling process in the node */
	
	public void setSampler(ISampler sampler);
	
	/***
	 * Broadcast a START_SAMPLING command to all the peers and 
	 * starts sampling itself. 
	 * @throws MasterException When the asked node is not the Master node
	 * @throws ConnectException When the node is not connected to the system
	 * @throws SamplerException If no Sampler is specified
	 * @throws IOException When a socket communication fails
	 */
	
	public void startSampling() throws MasterException, ConnectException, SamplerException, IOException;

	/***
	 * Broadcast a STOP_SAMPLING command to all the peers and 
	 * stops sampling itself. 
	 * @throws MasterException When the asked node is not the Master node
	 * @throws ConnectException When the node is not connected to the system
	 * @throws SamplerException If no Sampler is specified
	 * @throws IOException When a socket communication fails
	 */
	
	public void stopSampling() throws MasterException, ConnectException, SamplerException, IOException;

	/***
	 * Asks the node if is in sampling state
	 * @return if the node is sampling or not
	 */
	
	public boolean isSampling();
	
	/***
	 * If the message receiver is the Master node, asks for the peaks of the peers
	 * and computes the building's signature.
	 * @throws MasterException If the asked node is not the Master node
	 * @throws ConnectException When the node is not connected to the system
	 */
	
	public void computeModalFrequencies() throws MasterException, ConnectException;

	/***
	 * Returns the modal frequencies of the requested axis (if they are first computed
	 * @param axis Axis to return its modal frequencies
	 * @return Modal frequencies of the target Axis
	 */
	
	public List<Float> getModalFrequencies(AccelerationAxis axis);	
	
	/***
	 * Broadcasts command to all nodes, to delete their databases
	 * @throws MasterException If the asked node is not the Master node
	 * @throws ConnectException When the node is not connected to the system
	 */	
	
	public void deleteDatabase() throws MasterException, ConnectException;
	
	/***
	 * Broadcasts command to all nodes, to dump their databases
	 * @throws MasterException If the asked node is not the Master node
	 * @throws ConnectException When the node is not connected to the system
	 */
	
	public void dumpDatabase() throws MasterException, ConnectException;
	
	/***
	 * Returns if the node is Master of the distributed network.
	 * @return if it's Master or not
	 */
	
	public boolean isMaster();
	
	/***
	 * Returns the IP address of the Master node
	 * @return Master's IP address
	 */
	
	public String getMasterIP();	
}
