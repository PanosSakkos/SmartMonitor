package di.kdd.smartmonitor.protocol;

public interface IObserver {
	
	/***
	 * Show a toast notification in the activity that is interested in 
	 * events of the system.
	 * @param message The message to show
	 */
	
	public void update(String message);
}
