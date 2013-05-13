package di.kdd.smartmonitor.middleware;

import di.kdd.smartmonitor.protocol.Message;
import di.kdd.smartmonitor.protocol.ISmartMonitor.Tag;

public class TimeSynchronizationMessage extends Message {
	
	private static final long serialVersionUID = 1L;

	public TimeSynchronizationMessage() {
		super(Tag.SYNC, Long.toString(System.currentTimeMillis()));
	}
}
