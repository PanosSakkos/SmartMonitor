package di.kdd.smartmonitor.framework.exceptions;

public class TagException extends Exception {

	private static final long serialVersionUID = 1L;

	@Override
	public String getMessage() {
		return "Invalid tag";
	}
}
