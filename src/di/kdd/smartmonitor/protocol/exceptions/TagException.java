package di.kdd.smartmonitor.protocol.exceptions;

public class TagException extends Exception {

	@Override
	public String getMessage() {
		return "Invalid tag";
	}
}
