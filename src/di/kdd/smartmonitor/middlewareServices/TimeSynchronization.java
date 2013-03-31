package di.kdd.smartmonitor.middlewareServices;

public class TimeSynchronization {
	private long timeDifference;
	
	public TimeSynchronization() {
		timeDifference = 0;
	}
	
	public void timeReference(long time) {
		timeDifference = System.currentTimeMillis() - time;
	}
	
	public long getTime() {
		return System.currentTimeMillis() + timeDifference;
	}
}
