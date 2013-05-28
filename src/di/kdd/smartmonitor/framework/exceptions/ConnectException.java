package di.kdd.smartmonitor.framework.exceptions;

public class ConnectException extends Exception {
	
	private static final long serialVersionUID = 1L;

	@Override
	public String getMessage() {
		return "Not connected to the system";
	}

}
