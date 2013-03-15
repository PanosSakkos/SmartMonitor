package di.kdd.buildmon.protocol;

public class BuildMonProtocol implements IProtocol , Runnable {
	private boolean isCaptain;
	private Thread commandThread;	
	private PeerData peerData = new PeerData();
	
	@Override
	public void start() {
		commandThread = new Thread(this);
		commandThread.start();
	}

	@Override
	public boolean isCaptain() {
		return isCaptain;
	}
	
	@Override
	public String getCaptain() {
		
		/* Captain is the peer with the lowest IP address */
		
		return peerData.getLowestIP();
	}
	
	@Override
	public void end() {
		commandThread.interrupt();
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
