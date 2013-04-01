package di.kdd.smartmonitor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import di.kdd.smartmonitor.Acceleration.AccelerationAxis;

public class SamplingService extends Service implements SensorEventListener {
	
	private Sensor accelerometer;
	private SensorManager sensorManager;

	private long accelerationsCounted = 0;

	private AccelerationsSQLiteHelper accelerationsDb;

	private static final String TAG = "listener service";
	
	/**
	 * @return Counted events of the accelerometer sensor
	 */
	
	public long getAccelerationsCounted() {
		return accelerationsCounted;
	}
			
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub		
	}

	/***
	 * Handler of the accelerometer values.
	 * Stores the non-zero values of acceleration into the SQLite database
	 */
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		long timestamp = System.currentTimeMillis();

		try {
		
			/* Ignore 0 values */
			
			if(event.values[0] != 0) {
				Acceleration xAcceleration = new Acceleration(event.values[0], timestamp);
				accelerationsDb.storeAcceleration(xAcceleration, AccelerationAxis.X);				
			}
	
			if(event.values[1] != 0) {
				Acceleration yAcceleration = new Acceleration(event.values[1], timestamp);
				accelerationsDb.storeAcceleration(yAcceleration, AccelerationAxis.Y);			
			}
	
			if(event.values[2] != 0) {
				Acceleration zAcceleration = new Acceleration(event.values[2], timestamp);
				accelerationsDb.storeAcceleration(zAcceleration, AccelerationAxis.Z);						
			}		
		}
		catch(Exception exception) {
			Log.e(TAG, exception.getMessage());			
		}

		accelerationsCounted++;
	}

	/***
	 * Registers the listener method for the accelerometer service and opens the 
	 * SQLite database ion order to store the handled acceleration events.
	 * Dispatched when the startService is invoked.
	 */
	
	@Override
	public void onCreate() {
		accelerationsDb = new AccelerationsSQLiteHelper(null, this.getApplicationContext());
		
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);	
	}
	
	/***
	 * Unregisters the acceleration event handler.
	 * Dispatched when the stopService method is invoked.
	 */
	
	@Override
	public void onDestroy() {
		accelerationsDb.close();
		sensorManager.unregisterListener(this);		
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		/* This is an unbound service */
		return null;
	}

}
