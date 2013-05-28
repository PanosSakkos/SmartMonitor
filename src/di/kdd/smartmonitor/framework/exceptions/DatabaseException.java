package di.kdd.smartmonitor.framework.exceptions;

public class DatabaseException extends Exception {

	private static final long serialVersionUID = 1L;

	@Override
	public String getMessage() {
		return "There is not database set";
	}
}
