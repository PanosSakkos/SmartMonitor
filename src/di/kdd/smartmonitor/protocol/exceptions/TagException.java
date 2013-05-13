package di.kdd.smartmonitor.protocol.exceptions;

public class TagException extends Exception {

	private static final long serialVersionUID = 1L;

	@Override
	public String getMessage() {
		return "Invalid tag";
	}
}
