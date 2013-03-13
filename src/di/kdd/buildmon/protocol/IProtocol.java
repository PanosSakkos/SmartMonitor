package di.kdd.buildmon.protocol;

public interface IProtocol {

	static final int PORT_NUMBER = 4632;
	
	/* Message tags */
	
	static final String KNOCK_KNOCK_TAG = "KNOCK";
	static final String HEARTBEAT_TAG = "HEARTBEAT";
	static final String GET_PEAKS = "GET_PEAKS";
	
	/***
	 * Entry point of the protocol.
	 */
	
	void start();
}
