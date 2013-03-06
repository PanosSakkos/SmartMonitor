package di.kdd.buildmon;

public class Acceleration {

	public enum AccelerationAxis {X, Y, Z};	
	
	private long timestamp;
	private float acceleration;

	/***
	 * @return The timestamp that the Acceleration was sampled
	 */
	
	public long getTimestamp() {
		return timestamp;
	}
	
	/***
	 * @return The values of Acceleration that was sampled
	 */
	
	public float getAcceleration() {
		return acceleration;
	}
		
	/***
	 * Initializes the Acceleration instance
	 * @param acceleration The numeric value of the sampled acceleration
	 * @param timestamp The time when the acceleration was sampled
	 */
	
	public Acceleration(float acceleration, long timestamp){

		this.timestamp = timestamp;

		this.acceleration = acceleration;
	}

}
