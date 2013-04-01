package di.kdd.smartmonitor.exceptions;

public class AxisException extends RuntimeException {
	@Override
	public String getMessage() {
		return "Not a valid Axis";
	}
}
