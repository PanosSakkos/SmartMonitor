package di.kdd.smartmonitor;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import di.kdd.smart.R;
import di.kdd.smartmonitor.framework.AccelerationsSQLiteHelper;
import di.kdd.smartmonitor.framework.ISmartMonitor;
import di.kdd.smartmonitor.framework.SamplingService;
import di.kdd.smartmonitor.framework.exceptions.ConnectException;
import di.kdd.smartmonitor.framework.exceptions.MasterException;
import di.kdd.smartmonitor.framework.exceptions.SamplerException;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MasterActivity extends NodeActivity {
	
	private Button samplingButton;
	private Button computeMFreqsButton;
	private Button plotButton;
		
	private boolean running = true;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.masteractivity);
		
		accelerationsDb = new AccelerationsSQLiteHelper(this.getApplicationContext());
		accelerationsDb.subscribe(this);
		
		distributedSystem.subscribe(this);
		distributedSystem.setSampler(this);
		distributedSystem.setDatabase(accelerationsDb);
		
		distributedSystem.connectAsMaster();
		
		computeMFreqsButton = (Button) findViewById(R.id.computeModalFreqsBtn);		
		plotButton = (Button) findViewById(R.id.plotBtn);

		hideButtons();
		
		plotButton.setEnabled(true);
		plotButton.setAlpha((float) 1);
		
		samplingButton = (Button) findViewById(R.id.samplingBtn);

		samplingButton.setText("Start Sampling");
		
		Thread uiUpdater = new Thread(new Runnable() { 
			public void run() {
				while(running) {
					if(distributedSystem.isSampling()) {
						runOnUiThread(new Runnable() {public void run() { hideButtons(); }});
					}
					else {
						runOnUiThread(new Runnable() {public void run() { revealButtons(); }});
					}
					
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}});

		uiUpdater.start();
	}
		
	/* Button handlers */
	
	/**
	 * Handler of the sampling button. 
	 * Registers the listener of the Accelerometer
	 * @param view
	 */
	
	public void samplingButtonHandler(View _) {		
		try {			
			if(distributedSystem.isSampling()) {
				distributedSystem.stopSampling();
				Toast.makeText(this, "Accelerometer Listener Service Destroyed!", Toast.LENGTH_LONG).show();
				
				/* Update UI elements */
				samplingButton.setText("Start Sampling");
				revealButtons();
			}
			else {
				distributedSystem.startSampling();
				Toast.makeText(this, "Accelerometer Listener Service Created", Toast.LENGTH_LONG).show();
				
				/* Update UI elements */
				samplingButton.setText("Stop Sampling");
				hideButtons();
			}
		}
		catch(SamplerException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();												
		}
		catch(ConnectException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();									
		}
		catch(MasterException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();			
		}		
	}	
		
	
	
	
	private void hideButtons() {
		computeMFreqsButton.setEnabled(false);
		computeMFreqsButton.setAlpha((float) 0.2);
		
		plotButton.setEnabled(false);
		plotButton.setAlpha((float) 0.2);
		
		Button exportBtn = (Button)findViewById(R.id.exportToFileBtn);
		exportBtn.setEnabled(false);
		exportBtn.setAlpha((float) 0.2);
		
		Button deleteDbBtn = (Button)findViewById(R.id.deleteDbBtn);
		deleteDbBtn.setEnabled(false);
		deleteDbBtn.setAlpha((float) 0.2);
	}
	
	private void revealButtons() {
		computeMFreqsButton.setEnabled(true);
		computeMFreqsButton.setAlpha((float) 1);
		
		plotButton.setEnabled(true);
		plotButton.setAlpha((float) 1);
		
		Button exportBtn = (Button)findViewById(R.id.exportToFileBtn);
		exportBtn.setEnabled(true);
		exportBtn.setAlpha((float) 1);
		
		Button deleteDbBtn = (Button)findViewById(R.id.deleteDbBtn);
		deleteDbBtn.setEnabled(true);
		deleteDbBtn.setAlpha((float) 1);
		
	}

	/**
	 * Handler of the computeModalFrequencies button. 
	 * @param ignored
	 */	
	
	public void computeModalFrequencies(View _) {
		try {
			if(!distributedSystem.isSampling()) {
				distributedSystem.computeModalFrequencies();
			}
			else {
				Toast.makeText(this, "Stop sampling before computing the modal frequencies!", Toast.LENGTH_LONG).show();
			}
		}
		catch(ConnectException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();									
		}
		catch(MasterException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();			
		}
	}	

	/**
	 * Handler of the plot button.
	 * Calls the plotting activity 
	 * @param ignored
	 */	
	
	public void plot(View _) {
		if(!distributedSystem.isSampling()) {
			
			Intent intent = new Intent(this, PlotActivity.class);
	        startActivity(intent);
		}
		else {
			Toast.makeText(this, "STOP SAMPLING must be invoked!", Toast.LENGTH_LONG).show();
		}
	}	
	
	/***
	 * Dumps the stored Accelerations into 3 files, one for each Axis
	 * @param ignored
	 * @throws Exception
	 */
	
	public void exportToFile(View _) throws MasterException {
		try {
			if (!distributedSystem.isSampling()) {
					distributedSystem.dumpDatabase();
			}
			else {
				Toast.makeText(this, "The node is sampling!", Toast.LENGTH_LONG).show();
			}
		}
		catch(ConnectException e) {
			Toast.makeText(this, "The node is not connected!", Toast.LENGTH_LONG).show();							
		}
	}	
	
	public void deleteDatabase(View _) throws MasterException {
		try {
			if (!distributedSystem.isSampling()) {
				distributedSystem.deleteDatabase();
				Toast.makeText(this, "Deleted database", Toast.LENGTH_LONG).show();
			}
			else {
				Toast.makeText(this, "The node is sampling!", Toast.LENGTH_LONG).show();
			}
		}
		catch(ConnectException e) {
			Toast.makeText(this, "The node is not connected!", Toast.LENGTH_LONG).show();			
		}
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
}
