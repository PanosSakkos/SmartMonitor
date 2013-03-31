package di.kdd.smartmonitor.protocol.exceptions;

public class MasterException extends Exception {
	@Override
	public String getMessage() {
		return "This node is not the Master";
	}
}
