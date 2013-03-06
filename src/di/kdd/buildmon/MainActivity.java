package di.kdd.buildmon;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

	private boolean samplingRunning;
	private AccelerationsSQLiteHelper accelerationsDb;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		accelerationsDb = new AccelerationsSQLiteHelper(this.getApplicationContext());
	}

	/**
	 * Handler of the startSampling button. 
	 * Registers the listener of the Accelerometer
	 * @param view
	 */
	
	public void startSampling(View view) {		
		if(!samplingRunning) {
			startService(new Intent(this, AccelerometerListenerService.class));
			samplingRunning = true;
		}
		else {
			Toast.makeText(getApplicationContext(), "Sampling Service is already running!", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Handler of the stopSampling button. 
	 * Unregisters the listener of the Accelerometer
	 * @param view
	 */	
	
	public void stopSampling(View view)	{
		if(samplingRunning) {
			stopService(new Intent(this, AccelerometerListenerService.class));
			samplingRunning = false;
		}
		else {
			Toast.makeText(this, "Sampling Service is not running!", Toast.LENGTH_LONG).show();
		}
	}	
	
	/***
	 * Dumps the stored Accelerations into 3 files, one for each Axis
	 * @param view
	 * @throws Exception
	 */
	
	public void plotGraphs(View view) throws Exception {
		if(!samplingRunning) {
			accelerationsDb.dumpToFile();
		}
		else {
			Toast.makeText(this, "Stop Sampling!", Toast.LENGTH_LONG).show();

		}
	}	
}
