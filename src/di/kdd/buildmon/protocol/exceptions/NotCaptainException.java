package di.kdd.buildmon.protocol.exceptions;

public class NotCaptainException extends Exception {
	@Override
	public String getMessage() {
		return "This node is not the Captain";
	}
}
