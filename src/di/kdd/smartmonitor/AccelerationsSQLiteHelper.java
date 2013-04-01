package di.kdd.smartmonitor;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import di.kdd.smartmonitor.Acceleration.AccelerationAxis;
import di.kdd.smartmonitor.exceptions.AxisException;

public class AccelerationsSQLiteHelper extends SQLiteOpenHelper {
	
	private static final int DATABASE_VERSION = 18;

	private static final String DATABASE_NAME = "accelerations.db";

	/* Three tables, on for each acceleration axis, with the acceleration and the timestamp */
	
	private static final String TABLE_X_ACCELERATIONS = "x_accelerations";
	private static final String TABLE_Y_ACCELERATIONS = "y_accelerations";
	private static final String TABLE_Z_ACCELERATIONS = "z_accelerations";

	private static final String COLUMN_ID = "id";
	private static final String COLUMN_ACCELERATION = "acceleration";
	private static final String COLUMN_TIMESTAMP = "timestamp";

	/* SQLite commands to create each Table of the database */
	
	private static final String TABLE_X_ACCELERATIONS_CREATE = "create table " + TABLE_X_ACCELERATIONS + 
													" (" + COLUMN_ID + " integer primary key autoincrement" + 
													", " + COLUMN_ACCELERATION + " float not null" +
													", " + COLUMN_TIMESTAMP + " long not null);";

	private static final String TABLE_Y_ACCELERATIONS_CREATE = "create table " + TABLE_Y_ACCELERATIONS + 
													" (" + COLUMN_ID + " integer primary key autoincrement" + 
													", " + COLUMN_ACCELERATION + " float not null" +
													", " + COLUMN_TIMESTAMP + " long not null);";

	private static final String TABLE_Z_ACCELERATIONS_CREATE = "create table " + TABLE_Z_ACCELERATIONS + 
													" (" + COLUMN_ID + " integer primary key autoincrement" + 
													", " + COLUMN_ACCELERATION + " float not null" +
													", " + COLUMN_TIMESTAMP + " long not null);";

	/* Buffers for the Accelerations, dump to database for every 
	 * BUFFER_THRESHOLD Accelerations captured 
	 * */
	
	private static final int BUFFER_THRESHOLD = 5;
	
	private List<Acceleration> xAccelerationsBuffer = new ArrayList<Acceleration>();
	private List<Acceleration> yAccelerationsBuffer = new ArrayList<Acceleration>();
	private List<Acceleration> zAccelerationsBuffer = new ArrayList<Acceleration>();

	/* The view to send UI notifications to */
	
	private MainActivity view;
	
	private Context context;
	
	private static final String TAG = "database";
	
	/***
	 * Initializes the AccelerationsSQLiteHelper
	 * @param context The context of the Application that the SQLite Database belongs to
	 */
	
	public AccelerationsSQLiteHelper(MainActivity view, Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.view = view;
		this.context = context;
		
		Log.i(TAG, "Created " + DATABASE_NAME + " v" + DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {	
		Log.i(TAG, "Creating database");
		
		db.execSQL(TABLE_X_ACCELERATIONS_CREATE);
		db.execSQL(TABLE_Y_ACCELERATIONS_CREATE);
		db.execSQL(TABLE_Z_ACCELERATIONS_CREATE);
	}

	/***
	 * Drops all the tables and dispatches the onCreate method to create the upgraded tables
	 */
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(TAG, "Upgrading database");
		
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_X_ACCELERATIONS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_Y_ACCELERATIONS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_Z_ACCELERATIONS);

		onCreate(db);
	}
		
	/***
	 * Stores the acceleration values that are in the buffer for axis X
	 */
	
	private void dumpXAccelerationsBuffer() {
		SQLiteDatabase db = this.getWritableDatabase();

		Log.i(TAG, "Flushing buffer of X axis to database");
		
		/* Dump Accelerations buffer for X axis */
		
		for(Acceleration acceleration : xAccelerationsBuffer) {
			db.execSQL("INSERT INTO " + TABLE_X_ACCELERATIONS + " (" + COLUMN_TIMESTAMP + ", " + COLUMN_ACCELERATION + ")" +
						" VALUES (" + Long.toString(acceleration.getTimestamp()) + ", " + Double.toString(acceleration.getAcceleration()) + ")");			
		}

		xAccelerationsBuffer.clear();		
		db.close();		
	}
	
	/***
	 * Stores the acceleration values that are in the buffer for axis Y
	 */

	private void dumpYAccelerationsBuffer() {
		SQLiteDatabase db = this.getWritableDatabase();

		Log.i(TAG, "Flushing buffer of Y axis to database");

		/* Dump Accelerations buffer for Y axis */

		for(Acceleration acceleration : yAccelerationsBuffer) {
			db.execSQL("INSERT INTO " + TABLE_Y_ACCELERATIONS + " (" + COLUMN_TIMESTAMP + ", " + COLUMN_ACCELERATION + ")" +
					" VALUES (" + Long.toString(acceleration.getTimestamp()) + ", " + Double.toString(acceleration.getAcceleration()) + ")");			
		}

		yAccelerationsBuffer.clear();		
		db.close();		
	}
	
	/***
	 * Stores the acceleration values that are in the buffer for axis Z
	 */

	private void dumpZAccelerationsBuffer() {
		SQLiteDatabase db = this.getWritableDatabase();

		Log.i(TAG, "Flushing buffer of Z axis to database");

		/* Dump Accelerations buffer for Z axis */		
		
		for(Acceleration acceleration : zAccelerationsBuffer) {
			db.execSQL("INSERT INTO " + TABLE_Z_ACCELERATIONS + " (" + COLUMN_TIMESTAMP + ", " + COLUMN_ACCELERATION + ")" +
					" VALUES (" + Long.toString(acceleration.getTimestamp()) + ", " + Double.toString(acceleration.getAcceleration()) + ")");			
		}

		zAccelerationsBuffer.clear();				
		db.close();		
	}
	
	/***
	 *  Dumps the accelerations that are not stored to the SQLite Database. 
	 */
	
	public void dumpAccelerationBuffers() {
		dumpXAccelerationsBuffer();
		dumpYAccelerationsBuffer();
		dumpZAccelerationsBuffer();
	}
	
	/***
	 * 
	 * @param acceleration The Acceleration instance to store in the SQLite database
	 * @param axis The Axis that this Acceleration belongs to
	 * @throws Exception If the Axis given is not valid
	 */
	
	public void storeAcceleration(Acceleration newAcceleration, AccelerationAxis axis) throws Exception {		
		switch(axis) {
		case X:
			if(xAccelerationsBuffer.size() == BUFFER_THRESHOLD) {
				dumpXAccelerationsBuffer();
			}
				
			xAccelerationsBuffer.add(newAcceleration);			
			break;
		case Y:
			if(yAccelerationsBuffer.size() == BUFFER_THRESHOLD) {
				dumpYAccelerationsBuffer();
			}
				
			yAccelerationsBuffer.add(newAcceleration);			
			break;
		case Z:
			if(zAccelerationsBuffer.size() == BUFFER_THRESHOLD) {
				dumpZAccelerationsBuffer();
			}
				
			zAccelerationsBuffer.add(newAcceleration);			
			break;
		default:
			throw new AxisException();
		}
	}

	/***
	 * Queries the SLQite database for an Acceleration at a given time
	 * 
	 * @param timestamp The time that the database is asked for an Acceleration
	 * @param axis The Axis of the Acceleration
	 * @return The selected Acceleration, or null if it was not found
	 * @throws Exception If the rawQuery fails
	 */
	
	public Acceleration getAccelerationAt(long timestamp, AccelerationAxis axis) throws Exception {
		Cursor cursor;
		Acceleration acceleration;
		SQLiteDatabase db = this.getReadableDatabase();
		
		switch(axis)
		{
			case X:
				cursor = db.rawQuery("SELECT " + COLUMN_ACCELERATION + 
									" FROM " + TABLE_X_ACCELERATIONS + 
									" WHERE " + COLUMN_TIMESTAMP + " = " + Long.toString(timestamp), null);
				break;
			case Y:
				cursor = db.rawQuery("SELECT " + COLUMN_ACCELERATION + 
									" FROM " + TABLE_Y_ACCELERATIONS + 
									" WHERE " + COLUMN_TIMESTAMP + " = " + Long.toString(timestamp), null);
				break;
			case Z:
				cursor = db.rawQuery("SELECT " + COLUMN_ACCELERATION + 
									" FROM " + TABLE_Z_ACCELERATIONS + 
									" WHERE " + COLUMN_TIMESTAMP + " = " + Long.toString(timestamp), null);
				break;
			default:
				throw new AxisException();
		}
		
		if(cursor == null) { 
			Log.i(TAG, "No results found");
			db.close();

			return null;
		}
		
		cursor.moveToFirst();
		
		acceleration = new Acceleration(cursor.getFloat(1), timestamp);
		
		db.close();
		
		return acceleration;
	}

	/***
	 * Queries the database for all the Accelerations of an Axis
	 * @param axis The Axis that is queried
	 * @return A list of all the stored Accelerations of the given Axis
	 * @throws Exception If the rawQuery fails
	 */
	
	public List<Acceleration> getAllAccelerations(AccelerationAxis axis) throws Exception {		
		Cursor cursor;
		SQLiteDatabase db = this.getReadableDatabase();
		List<Acceleration> accelerations = new ArrayList<Acceleration>();
		
		switch(axis) {
			case X:
				cursor = db.rawQuery("SELECT " + COLUMN_ACCELERATION + ", " + COLUMN_TIMESTAMP +  
											" FROM " + TABLE_X_ACCELERATIONS, null);
				break;
			case Y:
				cursor = db.rawQuery("SELECT " + COLUMN_ACCELERATION + ", " + COLUMN_TIMESTAMP +  
											" FROM " + TABLE_Y_ACCELERATIONS, null);
				break;
			case Z:
				cursor = db.rawQuery("SELECT " + COLUMN_ACCELERATION + ", " + COLUMN_TIMESTAMP +  
											" FROM " + TABLE_Z_ACCELERATIONS, null);
				break;
			default:
				throw new AxisException();
		}
		
		if(cursor == null) {
			Log.i(TAG, "No results found");
			db.close();

			return null;
		}
		
		cursor.moveToFirst();
		
		while(cursor.moveToNext()) {
			accelerations.add(new Acceleration(cursor.getFloat(0), cursor.getLong(1)));
		}
		
		db.close();
		
		return accelerations;
	}
	
	/***
	 * Queries the Database for Accelerations of an Axis, between two timestamps
	 * @param from The starting timestamp
	 * @param to The ending timestamp
	 * @param axis The Axis of the queried Accelerations
	 * @return A list of all the found Accelerations
	 * @throws Exception If the rawQuery fails
	 */
	
	public List<Acceleration> getAccelerationsIn(long from, long to, AccelerationAxis axis) throws Exception {
		Cursor cursor;
		SQLiteDatabase db = this.getReadableDatabase();
		List<Acceleration> accelerations = new ArrayList<Acceleration>();
				
		switch(axis) {
			case X:
				cursor = db.rawQuery("SELECT " + COLUMN_ACCELERATION + ", " + COLUMN_TIMESTAMP +  
									" FROM " + TABLE_X_ACCELERATIONS + 
									" WHERE " + COLUMN_TIMESTAMP + " > " + Long.toString(from) + 
									" AND " + COLUMN_TIMESTAMP + " < " + Long.toString(to), null);
				break;
			case Y:
				cursor = db.rawQuery("SELECT " + COLUMN_ACCELERATION + ", " + COLUMN_TIMESTAMP +  
									" FROM " + TABLE_Y_ACCELERATIONS + 
									" WHERE " + COLUMN_TIMESTAMP + " > " + Long.toString(from) + 
									" AND " + COLUMN_TIMESTAMP + " < " + Long.toString(to), null);
				break;
			case Z:
				cursor = db.rawQuery("SELECT " + COLUMN_ACCELERATION + ", " + COLUMN_TIMESTAMP +  
									" FROM " + TABLE_Z_ACCELERATIONS + 
									" WHERE " + COLUMN_TIMESTAMP + " > " + Long.toString(from) + 
									" AND " + COLUMN_TIMESTAMP + " < " + Long.toString(to), null);
				break;
			default:
				throw new AxisException();
		}
		
		if(cursor == null) {
			Log.i(TAG, "No results found");
			db.close();

			return null;
		}
		
		cursor.moveToFirst();
		
		while(cursor.moveToNext()) {
			accelerations.add(new Acceleration(cursor.getFloat(0), cursor.getLong(1)));
		}
		
		db.close();
		
		return accelerations;
	}
	
	/***
	 * Dumps all the Accelerations of the three Axis into 3 files, 
	 * on for each Axis (DUMP_X_FILENAME, DUMP_Y_FILENAME, DUMP_Z_FILENAME)
	 * @throws Exception thrown from the IO operations
	 */
	
	public void dumpToFile() {		
		Log.i(TAG, "Dumping database to filesystem");

		DumpDatabaseTask dumper = new DumpDatabaseTask(view);
		dumper.execute();
	}
	
	/***
	 * Deletes the database
	 */
	
	public void deleteDatabase() {
		Log.i(TAG, "Deleted database");

		context.deleteDatabase(DATABASE_NAME);		
	}
}
