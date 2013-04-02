package di.kdd.smartmonitor.protocol.exceptions;

public class ConnectException extends Exception {
	
	@Override
	public String getMessage() {
		return "Not connected to the system";
	}

}
