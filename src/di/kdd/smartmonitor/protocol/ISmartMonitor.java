package di.kdd.smartmonitor.protocol;

import java.io.IOException;
import java.util.Date;

import di.kdd.smartmonitor.ISampler;
import di.kdd.smartmonitor.protocol.exceptions.ConnectException;
import di.kdd.smartmonitor.protocol.exceptions.MasterException;
import di.kdd.smartmonitor.protocol.exceptions.SamplerException;

public interface ISmartMonitor {
	
	/* The period of the heartbeat messages (in seconds) */
	
	static final int HEARTBEAT_PERIOD = 5;
	
	/* The port that the knock knock messages are sent */
	
	static final int JOIN_PORT = 4631;

	/* The port which the heartbeat messages are sent */

	static final int HEARBEATS_PORT = 4632;
	
	/* The port which the Master sends commands 
	 * (including the Master's heartbeats 
	 */

	static final int COMMAND_PORT = 4633;
	
	/* The (maximum) number of peaks that each peers returns to the Master */
	
	static final int NO_PEAKS = 5;
	
	/* Message tags */
	
	public enum Tag { JOIN, PEER_DATA, NEW_PEER, SYNC, HEARTBEAT, START_SAMPLING, STOP_SAMPLING, SEND_PEAKS , DELETE_DATA };
		
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
	 * @param from Starting sample time
	 * @param to Ending sample time
	 * @throws MasterException If the asked node is not the Master node
	 * @throws ConnectException When the node is not connected to the system
	 */
	
	public void computeModalFrequencies(Date from, Date to) throws MasterException, IOException, ConnectException;
	
	/***
	 * Broadcasts command to all nodes, to delete the databases
	 * @throws MasterException If the asked node is not the Master node
	 * @throws ConnectException When the node is not connected to the system
	 */
	
	public void deleteDatabase() throws MasterException, ConnectException;
	
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
