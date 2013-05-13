package di.kdd.smartmonitor.protocol.exceptions;

public class SamplerException extends Exception {

	private static final long serialVersionUID = 1L;

	@Override
	public String getMessage() {
		return "No Sampler is specified";
	}
}
