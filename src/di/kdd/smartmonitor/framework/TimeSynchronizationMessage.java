package di.kdd.smartmonitor.framework;

import di.kdd.smartmonitor.framework.ISmartMonitor.Tag;


public class TimeSynchronizationMessage extends Message {
	
	private static final long serialVersionUID = 1L;

	public TimeSynchronizationMessage() {
		super(Tag.SYNC, Long.toString(System.currentTimeMillis()));
	}
}
