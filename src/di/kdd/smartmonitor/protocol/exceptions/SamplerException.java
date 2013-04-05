package di.kdd.smartmonitor.protocol.exceptions;

public class SamplerException extends Exception {
	@Override
	public String getMessage() {
		return "No Sampler is specified";
	}
}
