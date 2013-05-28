package di.kdd.smartmonitor.framework;

public interface ISampler {
	
	/***
	 * Starts the sampling service
	 */
	
	public void startSamplingService();
	
	/***
	 * Stops the sampling service
	 */
	
	public void stopSamplingService();
}
