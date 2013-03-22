package di.kdd.buildmon.middlewareServices;

import di.kdd.buildmon.protocol.Message;
import di.kdd.buildmon.protocol.IProtocol.Tag;

public class TimeSynchronizationMessage extends Message {
	
	public TimeSynchronizationMessage() {
		super(Tag.TIME_SYNC, Long.toString(System.currentTimeMillis()));
	}
}
