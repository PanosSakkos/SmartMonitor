package di.kdd.smartmonitor.protocol.exceptions;

public class NotMasterException extends Exception {
	@Override
	public String getMessage() {
		return "This node is not the Master";
	}
}
