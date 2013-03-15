package di.kdd.buildmon.protocol;

public class BuildMonProtocol implements IProtocol , Runnable {
	private boolean isCaptain;
	
	/* Threads */
	
	private Thread commandThread;
	private Thread knockKnockListenerThread;
	
	private PeerData peerData = new PeerData();
	private KnockKnockListener knockKnockListener;
	
	@Override
	public void start() {
		commandThread = new Thread(this);
		commandThread.start();

		knockKnockListener = new KnockKnockListener(peerData);
		knockKnockListenerThread = new Thread(knockKnockListener);
		knockKnockListenerThread.start();
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
		knockKnockListenerThread.interrupt();
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
