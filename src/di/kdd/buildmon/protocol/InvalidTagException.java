package di.kdd.buildmon.protocol;

public class InvalidTagException extends Exception {
	@Override
	public String getMessage() {
		return "Invalid tag";
	}

}
