package di.kdd.buildmon.protocol;

public class BuildMonProtocol implements IProtocol , Runnable {
	private PeerData peerData = new PeerData();
	
	@Override
	public void start() {
		new Thread(this).start();
	}

	@Override
	public void end() {
		//TODO 
	}

	/***
	 * This method is dispatched in a new thread and is called form the start method.
	 * DO NOT dispatch this method.
	 */
	
	@Override
	public void run() {
		//TODO		
	}

}
