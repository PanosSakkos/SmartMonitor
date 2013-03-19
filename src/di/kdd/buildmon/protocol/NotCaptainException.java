package di.kdd.buildmon.protocol;

public class NotCaptainException extends Exception {
	@Override
	public String getMessage() {
		return "I am not the Captain node";
	}
}
