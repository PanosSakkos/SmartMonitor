package di.kdd.smartmonitor.framework;

import java.util.List;

import di.kdd.smartmonitor.framework.Acceleration.AccelerationAxis;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class FlushBufferThread extends Thread {

	private AccelerationsSQLiteHelper db;
	private AccelerationAxis axis;
	private List<Acceleration> buffer;
	
	private static final String TAG = "buffer flusher";
	
	public FlushBufferThread(AccelerationsSQLiteHelper db, AccelerationAxis axis, List<Acceleration> buffer) {
		this.db = db;
		this.axis = axis;
		this.buffer = buffer;
	}
	
	@Override
	public void run() {
		SQLiteDatabase wdb = db.getWritableDatabase();

		switch(axis) {
		case X:
			if(buffer.isEmpty() == false) {
				Log.i(TAG, "Flushing buffer of X axis to database");
			}
			
			/* Flush Accelerations buffer for X axis */

			for(Acceleration acceleration : buffer) {
				wdb.execSQL("INSERT INTO " + db.TABLE_X_ACCELERATIONS + " (" + db.COLUMN_TIMESTAMP + ", " + db.COLUMN_ACCELERATION + ")" +
						" VALUES (" + Long.toString(acceleration.getTimestamp()) + ", " + Double.toString(acceleration.getAcceleration()) + ")");			
			}

			Log.i(TAG, "Flushed buffer of X axis to database");
			break;
		case Y:
			if(buffer.isEmpty() == false) {
				Log.i(TAG, "Flushing buffer of Y axis to database");
			}
			
			/* Flush Accelerations buffer for Y axis */

			for(Acceleration acceleration : buffer) {
				wdb.execSQL("INSERT INTO " + db.TABLE_Y_ACCELERATIONS + " (" + db.COLUMN_TIMESTAMP + ", " + db.COLUMN_ACCELERATION + ")" +
						" VALUES (" + Long.toString(acceleration.getTimestamp()) + ", " + Double.toString(acceleration.getAcceleration()) + ")");			
			}
			
			Log.i(TAG, "Flushed buffer of Y axis to database");
			break;
		case Z:
			if(buffer.isEmpty() == false) {
				Log.i(TAG, "Flushing buffer of Z axis to database");
			}
			
			/* Flush Accelerations buffer for Z axis */

			for(Acceleration acceleration : buffer) {
				wdb.execSQL("INSERT INTO " + db.TABLE_Z_ACCELERATIONS + " (" + db.COLUMN_TIMESTAMP + ", " + db.COLUMN_ACCELERATION + ")" +
						" VALUES (" + Long.toString(acceleration.getTimestamp()) + ", " + Double.toString(acceleration.getAcceleration()) + ")");			
			}
			
			Log.i(TAG, "Flushed buffer of Z axis to database");
			break;
		}
	}

}
