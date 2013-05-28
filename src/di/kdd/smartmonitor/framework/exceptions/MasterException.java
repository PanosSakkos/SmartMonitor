package di.kdd.smartmonitor.framework.exceptions;

public class MasterException extends Exception {

	private static final long serialVersionUID = 1L;

	@Override
	public String getMessage() {
		return "This node is not the Master";
	}
}
