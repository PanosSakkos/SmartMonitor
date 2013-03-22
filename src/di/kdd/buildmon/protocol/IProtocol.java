package di.kdd.buildmon.protocol;

import java.io.IOException;
import java.util.Date;

import di.kdd.buildmon.protocol.exceptions.NotCaptainException;

public interface IProtocol {
	
	/* The period of the heartbeat messages (in seconds) */
	
	static final int HEARTBEAT_PERIOD = 5;
	
	/* The port that the knock knock messages are sent */
	
	static final int KNOCK_KNOCK_PORT = 4631;

	/* The port which the heartbeat messages are sent */

	static final int HEARBEATS_PORT = 4632;
	
	/* The port which the Captain sends commands 
	 * (including the captain's heartbeats 
	 */

	static final int COMMAND_PORT = 4633;
	
	/* The (maximum?) number of peaks that each peers returns to the Captain */
	
	static final int NO_PEAKS = 5;
	
	/* Message tags */
	
	public enum Tag { KNOCK_KNOCK, NEW_PEER, HEARTBEAT, TIME_SYNC, AGGREGATE_PEAKS };
		
	/***
	 * Entry point of the protocol. Creates a new thread that is running the protocol.
	 */
	
	public void start();
		
	/***
	 * If the message receiver is the Captain node, asks for the peaks of the peers
	 * and computes the building's signature.
	 * @param from Starting sample time
	 * @param to Ending sample time
	 * @throws NotCaptainException If the asked node is not the Captain node
	 */
	
	public void computeBuildingSignature(Date from, Date to) throws NotCaptainException, IOException;
	
	/***
	 * Returns if the node is Captain of the distributed network.
	 * @return if it's captain or not
	 */
	
	public boolean isCaptain();
	
	/***
	 * Returns the IP address of the Captain node
	 * @return Captain's IP address
	 */
	
	public String getCaptainIP();
		
	/***
	 * Abandon the distributed network.
	 */
	
	public void end();
}
