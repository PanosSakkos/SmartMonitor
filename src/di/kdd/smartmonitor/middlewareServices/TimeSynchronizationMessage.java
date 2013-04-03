package di.kdd.smartmonitor.middlewareServices;

import di.kdd.smartmonitor.protocol.Message;
import di.kdd.smartmonitor.protocol.ISmartMonitor.Tag;

public class TimeSynchronizationMessage extends Message {
	
	public TimeSynchronizationMessage() {
		super(Tag.SYNC, Long.toString(System.currentTimeMillis()));
	}
}
