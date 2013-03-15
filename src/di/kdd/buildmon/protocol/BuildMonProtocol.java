package di.kdd.buildmon.protocol;

public class BuildMonProtocol implements IProtocol , Runnable {	
	private boolean isCaptain;
	private PeerData peerData = new PeerData();
	
	private Thread protocolThread;
	private KnockKnockThread knockKnockThread;
	
	@Override
	public void start() {
		protocolThread = new Thread(this);
		protocolThread.start();
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
		if(knockKnockThread != null) {
			knockKnockThread.interrupt();
		}
		
		protocolThread.interrupt();
	}

	/***
	 * This method is dispatched in a new thread and is called form the start method.
	 * DO NOT dispatch this method.
	 */
	
	@Override
	public void run() {
		//TODO try to enter the distributed network		
	}
}
