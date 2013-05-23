package di.kdd.smartmonitor.protocol;

import android.util.Log;

public class TimeSynchronization {
	private long timeDifference;
	
	private static final String TAG = "time synchronization";
	
	public TimeSynchronization() {
		timeDifference = 0;
	}
	
	public void timeReference(long time) {
		timeDifference = System.currentTimeMillis() - time;
	
		Log.i(TAG, "New time difference " + Long.toString(timeDifference));
	}
	
	public long getTime() {
		return System.currentTimeMillis() + timeDifference;
	}
}
