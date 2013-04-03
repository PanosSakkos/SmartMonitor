package di.kdd.smartmonitor;

import java.io.IOException;

import di.kdd.buildmon.R;
import di.kdd.smartmonitor.protocol.DistributedSystem;
import di.kdd.smartmonitor.protocol.IObserver;
import di.kdd.smartmonitor.protocol.exceptions.ConnectException;
import di.kdd.smartmonitor.protocol.exceptions.MasterException;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements IObserver, ISampler {	
	private AccelerationsSQLiteHelper accelerationsDb;
	private DistributedSystem distributedSystem;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
			
		distributedSystem = new DistributedSystem();
		distributedSystem.subscribe(this);

		accelerationsDb = new AccelerationsSQLiteHelper(this, this.getApplicationContext());
	}

	@Override
	protected void onDestroy() {
		accelerationsDb.flushAccelerationBuffers();
	}
	
	/* IObserver implementation */
	
	@Override
	public void showToastNotification(String message) {		
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
	/* ISampler implementation */
	
	@Override
	public void startSamplingService() {
		if(distributedSystem.isSampling() == false) {
			startService(new Intent(this, SamplingService.class));		
		}
	}	
	
	@Override
	public void stopSamplingService() {
		if(distributedSystem.isSampling()) {		
			stopService(new Intent(this, SamplingService.class));		
		}
	}
	
	/* Button handlers */
	
	/**
	 * Handler of the startSampling button. 
	 * Registers the listener of the Accelerometer
	 * @param view
	 */
	
	public void startSampling(View _) {		
		try {			
			if(distributedSystem.isSampling() == false) {
				distributedSystem.startSampling();
				
				Toast.makeText(this, "Accelerometer Listener Service Created", Toast.LENGTH_LONG).show();		
			}
			else {
				Toast.makeText(this, "Sampling Service is already running!", Toast.LENGTH_LONG).show();
			}
		}
		catch(IOException e) {
			Toast.makeText(this, "Failed to command nodes to start sampling", Toast.LENGTH_LONG).show();						
		}
		catch(ConnectException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();									
		}
		catch(MasterException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();			
		}
	}	
		
	/**
	 * Handler of the stopSampling button. 
	 * Unregisters the listener of the Accelerometer
	 * @param ignored
	 */	
	
	public void stopSampling(View _) {
		try {
			if(distributedSystem.isSampling()) {
				distributedSystem.stopSampling();

				Toast.makeText(this, "Accelerometer Listener Service Destroyed", Toast.LENGTH_LONG).show();
			}
			else {
				Toast.makeText(this, "Node is not sampling!", Toast.LENGTH_LONG).show();
			}
		}
		catch(IOException e) {
			Toast.makeText(this, "Failed to command nodes to start sampling", Toast.LENGTH_LONG).show();						
		}
		catch(ConnectException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();									
		}
		catch(MasterException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();			
		}
	}	
	
	
	/***
	 * Handler for the connect button. 
	 * @param ignored
	 */
	
	public void connect(View _) {
		if(distributedSystem.isConnected() == false) {
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
	
	public void exportToFile(View _) {
		if(distributedSystem.isSampling() == false) {
				accelerationsDb.flushAccelerationBuffers();
				accelerationsDb.dumpToFile();
		}
		else {
			Toast.makeText(this, "The node is sampling!", Toast.LENGTH_LONG).show();
		}
	}	
	
	public void connectAsMaster(View _) {
		distributedSystem.connectAsMaster();
	}
	
	public void connectAt(View _) {
		EditText editText;
		
		editText = (EditText) findViewById(R.id.ipText);
		distributedSystem.connectAt(editText.getText().toString());
	}
	
	public void deleteDatabase(View _) {
		accelerationsDb.deleteDatabase();
		Toast.makeText(this, "Deleted database", Toast.LENGTH_LONG).show();
	}
}
