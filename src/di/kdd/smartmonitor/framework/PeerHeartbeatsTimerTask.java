package di.kdd.smartmonitor.framework;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.TimerTask;

import di.kdd.smartmonitor.framework.ISmartMonitor.Tag;

import android.util.Log;

public class PeerHeartbeatsTimerTask extends TimerTask implements IObservable {
	
	private String masterIP;
	private Socket heartbeatSocket;
	
	private static final String TAG = "PeerHeartBeatsTimerTask";
	
	public PeerHeartbeatsTimerTask(String masterIP) {

		if(masterIP.charAt(0) == '/') {
			masterIP = masterIP.substring(1);
		}		

		this.masterIP = masterIP;

		Log.i(TAG, "Connected at " + masterIP);
	}
	
	@Override
	public void run() {
		
		/* Try to write to the heartbeat socket,
		 * in case of failure, Master is down */
				
		try {
			heartbeatSocket = new Socket(masterIP, ISmartMonitor.HEARBEATS_PORT);

			Node.send(heartbeatSocket,new Message(Tag.HEARTBEAT));
		}
		catch (IOException e) {
			
			/* Master (probably) is down */
			
			Log.i(TAG, "Master is down");

			if(observer != null) {
				observer.update("Master is down");
			}
			
			this.cancel();
		}
	}

	/* IObservable implementation */

	private IObserver observer;
	
	@Override
	public void subscribe(IObserver observer) {
		this.observer = observer;
	}

	@Override
	public void unsubscribe(IObserver observer) {
		this.observer = null;
	}

	@Override
	public void notify(String message) {
		observer.update(message);
	}
}
