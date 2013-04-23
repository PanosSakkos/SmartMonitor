package di.kdd.smartmonitor;

public interface IObservable {
		
	/***
	 * Subscribes to events
	 * @param observer The observer that is interested in the events
	 */
	
	public void subscribe(IObserver observer);
	
	/***
	 * Unsubscribes from the events
	 * @param observer The observer to unsubscribe
	 */
	
	public void unsubscribe(IObserver observer);
	
	/***
	 * Notifies the subscribed observers about an event
	 * @param message The event that happened
	 */
	
	public void notify(String message);
}
