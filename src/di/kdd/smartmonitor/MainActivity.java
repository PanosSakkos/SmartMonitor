package di.kdd.smartmonitor;

import di.kdd.buildmon.R;
import di.kdd.smartmonitor.protocol.DistributedSystem;
import di.kdd.smartmonitor.protocol.exceptions.MasterException;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {	
	private AccelerationsSQLiteHelper accelerationsDb;
	private DistributedSystem distributedSystem;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
			
		accelerationsDb = new AccelerationsSQLiteHelper(this.getApplicationContext());
	}

	@Override
	protected void onDestroy() {
		accelerationsDb.dumpAccelerationBuffers();
	}
	
	public void showMessage(String message) {		
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}
	
	/**
	 * Handler of the startSampling button. 
	 * Registers the listener of the Accelerometer
	 * @param view
	 */
	
	public void startSampling(View _) {		
		try {			
			if(distributedSystem.isSampling() == false) {
				distributedSystem.startSampling();
			}
			else {
				Toast.makeText(getApplicationContext(), "Sampling Service is already running!", Toast.LENGTH_LONG).show();
			}
		}
		catch(MasterException e) {
			Toast.makeText(getApplicationContext(), "This action can be done only from the Master node!", Toast.LENGTH_LONG).show();			
		}
	}	
	
	public void startSamplingService() {
		startService(new Intent(this, AccelerometerListenerService.class));		
	}	
	
	/**
	 * Handler of the stopSampling button. 
	 * Unregisters the listener of the Accelerometer
	 * @param ignored
	 */	
	
	public void stopSampling(View _)	{
		try {			
			if(distributedSystem.isSampling()) {
				distributedSystem.stopSampling();
			}
			else {
				Toast.makeText(this, "Node is not sampling!", Toast.LENGTH_LONG).show();
			}
		}
		catch(MasterException e) {
			Toast.makeText(getApplicationContext(), "This action can be done only from the Master node!", Toast.LENGTH_LONG).show();			
		}
	}	
	
	public void stopSamplingService() {
		stopService(new Intent(this, AccelerometerListenerService.class));		
	}
	
	/***
	 * Handler for the connect button. 
	 * @param ignored
	 */
	
	public void connect(View _) {
		if(distributedSystem.isConnected() == false) {
				/* Reallocate instance, because AsyncTask can be run only once =/ */
			
				distributedSystem = new DistributedSystem(this);
				distributedSystem.connect();				
		}
		else {			
			Toast.makeText(this, "Already connected!", Toast.LENGTH_LONG).show();
		}
	}
	
	/***
	 * Handler for the disconnect button. 
	 * @param ignored
	 */
	
	public void disconnect(View _) {
		if(distributedSystem.isConnected()) {
			distributedSystem.disconnect();
		}
		else {			
			Toast.makeText(this, "Not connected!", Toast.LENGTH_LONG).show();
		}
	}
		
	/***
	 * Dumps the stored Accelerations into 3 files, one for each Axis
	 * @param ignored
	 * @throws Exception
	 */
	
	public void exportToFile(View _) throws Exception {
		if(distributedSystem.isSampling() == false) {
			accelerationsDb.dumpAccelerationBuffers();
			accelerationsDb.dumpToFile();
		}
		else {
			Toast.makeText(this, "The node is sampling!", Toast.LENGTH_LONG).show();
		}
	}	
}
