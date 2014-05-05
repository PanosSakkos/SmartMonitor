package di.kdd.smartmonitor.framework;

import java.util.List;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.database.sqlite.*;

public class FlushBufferThread extends Thread {

	private AccelerationsSQLiteHelper db;

	private List<Acceleration> xbuffer;
	private List<Acceleration> ybuffer;
	private List<Acceleration> zbuffer;
	private static final String TAG = "buffer flusher";

	final static String INSERT_QUERY_X =  createInsert(AccelerationsSQLiteHelper.TABLE_X_ACCELERATIONS, 
			new String[]{AccelerationsSQLiteHelper.COLUMN_TIMESTAMP, AccelerationsSQLiteHelper.COLUMN_ACCELERATION});
	final static String INSERT_QUERY_Y =  createInsert(AccelerationsSQLiteHelper.TABLE_Y_ACCELERATIONS, 
			new String[]{AccelerationsSQLiteHelper.COLUMN_TIMESTAMP, AccelerationsSQLiteHelper.COLUMN_ACCELERATION});
	final static String INSERT_QUERY_Z =  createInsert(AccelerationsSQLiteHelper.TABLE_Z_ACCELERATIONS, 
			new String[]{AccelerationsSQLiteHelper.COLUMN_TIMESTAMP, AccelerationsSQLiteHelper.COLUMN_ACCELERATION});

	public FlushBufferThread(AccelerationsSQLiteHelper db, List<Acceleration> xbuffer, List<Acceleration> ybuffer, List<Acceleration> zbuffer) {
		this.db = db;
		this.xbuffer = xbuffer;
		this.ybuffer = ybuffer;
		this.zbuffer = zbuffer;
	}

	@Override
	public void run() {
		SQLiteDatabase wdb = db.getWritableDatabase();

		final SQLiteStatement x_statement = wdb.compileStatement(INSERT_QUERY_X);
		final SQLiteStatement y_statement = wdb.compileStatement(INSERT_QUERY_Y);
		final SQLiteStatement z_statement = wdb.compileStatement(INSERT_QUERY_Z);

		/* Flush Accelerations buffer for X axis */
		wdb.beginTransaction();

		if(xbuffer!=null && !xbuffer.isEmpty()) {
			Log.i(TAG, "Flushing buffer of X axis to database");
		}

		try {
			for(Acceleration acceleration : xbuffer){
				x_statement.clearBindings();
				x_statement.bindLong(1, acceleration.getTimestamp());
				x_statement.bindDouble(2,acceleration.getAcceleration());
				x_statement.execute(); 
			}
			wdb.setTransactionSuccessful();
		} finally {
			wdb.endTransaction();
		}
		Log.i(TAG, "Flushed buffer of X axis to database");


		/* Flush Accelerations buffer for Y axis */
		wdb.beginTransaction();
		if(ybuffer!=null && !ybuffer.isEmpty()) {
			Log.i(TAG, "Flushing buffer of Y axis to database");
		}

		try {
			for(Acceleration acceleration : ybuffer){
				y_statement.clearBindings();
				y_statement.bindLong(1, acceleration.getTimestamp());
				y_statement.bindDouble(2,acceleration.getAcceleration());
				y_statement.execute();
			}
			wdb.setTransactionSuccessful();
		} finally {
			wdb.endTransaction();
		}
		Log.i(TAG, "Flushed buffer of Y axis to database");

		wdb.beginTransaction();
		if(zbuffer!=null && !zbuffer.isEmpty()) {
			Log.i(TAG, "Flushing buffer of Z axis to database");
		}

		/* Flush Accelerations buffer for Z axis */
		try {
			for(Acceleration acceleration : zbuffer){
				z_statement.clearBindings();
				z_statement.bindLong(1, acceleration.getTimestamp());
				z_statement.bindDouble(2,acceleration.getAcceleration());
				z_statement.execute(); 
			}
			wdb.setTransactionSuccessful();
		} finally {
			wdb.endTransaction();
		}
		Log.i(TAG, "Flushed buffer of Z axis to database");

	}



	static public String createInsert(final String tableName, final String[] columnNames) {
		if (tableName == null || columnNames == null || columnNames.length == 0) {
			throw new IllegalArgumentException();
		}
		final StringBuilder s = new StringBuilder();
		s.append("INSERT INTO ").append(tableName).append(" (");
		for (String column : columnNames) {
			s.append(column).append(" ,");
		}
		int length = s.length();
		s.delete(length - 2, length);
		s.append(") VALUES( ");
		for (int i = 0; i < columnNames.length; i++) {
			s.append(" ? ,");
		}
		length = s.length();
		s.delete(length - 2, length);
		s.append(")");
		return s.toString();
	}

}
