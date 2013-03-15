package di.kdd.buildmon.protocol;

public interface IProtocol {
	
	static final String DEBUG_TAG = "protocol";
	
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
	
	/* Message tags */
	
	public enum Tag { KNOCK_KNOCK, HEARTBEAT, TIME_SYNC, AGGREGATE_PEAKS };
		
	/***
	 * Entry point of the protocol. Creates a new thread that is running the protocol.
	 */
	
	public void start();
	
	/***
	 * Abandon the distributed network.
	 */
	
	public void end();
}
