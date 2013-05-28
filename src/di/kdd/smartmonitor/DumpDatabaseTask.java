package di.kdd.smartmonitor;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import di.kdd.smartmonitor.Acceleration.AccelerationAxis;
import di.kdd.smartmonitor.framework.IObserver;

public class DumpDatabaseTask extends AsyncTask<Void, Void, Boolean> {
	/* The folder that the dump files are placed under */
	
	private static final String SMART_MONITOR_FOLDER = "SmartMonitor";
	
	/* Names of the dump files */
	
	private static final String DUMP_X_FILENAME = "x_accelerations_dump.txt";
	private static final String DUMP_Y_FILENAME = "y_accelerations_dump.txt";
	private static final String DUMP_Z_FILENAME = "z_accelerations_dump.txt";		

	private IObserver observer;
	private AccelerationsSQLiteHelper database;
	
	private static final String TAG = "db dumper";
			
	public DumpDatabaseTask(AccelerationsSQLiteHelper database, IObserver observer) {
		this.database = database;
		this.observer = observer;
	}
	
	/***
	 * Creates the SMART_MONITOR_FOLDER, if it doesn't exist
	 */
	
	private void createSmartMonitorFolder() {
		File folder = new File(Environment.getExternalStoragePublicDirectory(""), SMART_MONITOR_FOLDER);
		
		if(!folder.exists()) {
			folder.mkdirs();
			
			Log.i(TAG, "Created " + SMART_MONITOR_FOLDER + " folder");
		}			
	}	
	
	@Override
	protected Boolean doInBackground(Void ... _) {
		android.os.Debug.waitForDebugger();

		try {			
			File dumpFile;
			PrintWriter printWriter;
			List<Acceleration> accelerations;
			
			createSmartMonitorFolder();
			
			/* Dump X axis accelerations */
			
			dumpFile = new File(Environment.getExternalStoragePublicDirectory(SMART_MONITOR_FOLDER), DUMP_X_FILENAME);
			printWriter = new PrintWriter(new FileWriter(dumpFile, false));
			
			accelerations = database.getAllAccelerations(AccelerationAxis.X);
			
			for(Acceleration acceleration : accelerations) {
				printWriter.println(Long.toString(acceleration.getTimestamp()) + "\t" + Float.toString(acceleration.getAcceleration()));
			}
			
			printWriter.close();
			
			Log.i(TAG, "Wrote " + DUMP_X_FILENAME);
			
			/* Dump Y axis accelerations */
	
			dumpFile = new File(Environment.getExternalStoragePublicDirectory(SMART_MONITOR_FOLDER), DUMP_Y_FILENAME);
			printWriter = new PrintWriter(new FileWriter(dumpFile, false));
			
			accelerations = database.getAllAccelerations(AccelerationAxis.Y);
			
			for(Acceleration acceleration : accelerations) {
				printWriter.println(Long.toString(acceleration.getTimestamp()) + "\t" + Float.toString(acceleration.getAcceleration()));
			}
			
			printWriter.close();
	
			Log.i(TAG, "Wrote " + DUMP_Y_FILENAME);		
			
			/* Dump Z axis accelerations */
	
			dumpFile = new File(Environment.getExternalStoragePublicDirectory(SMART_MONITOR_FOLDER), DUMP_Z_FILENAME);
			printWriter = new PrintWriter(new FileWriter(dumpFile, false));
			
			accelerations =database. getAllAccelerations(AccelerationAxis.Z);
			
			for(Acceleration acceleration : accelerations) {
				printWriter.println(Long.toString(acceleration.getTimestamp()) + "\t" + Float.toString(acceleration.getAcceleration()));
			}
			
			printWriter.close();
	
			Log.i(TAG, "Wrote " + DUMP_Z_FILENAME);
						
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();

			return false;
		}
	}	
	
	@Override
	protected void onPostExecute(Boolean succcess) {
		database.close();

		if(succcess) {
			observer.update("Dumped accelerations to filesystem");
		}
		else {
			observer.update("Failed to dump acceleration to filesystem");
		}
	}
}
