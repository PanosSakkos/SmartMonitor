package di.kdd.buildmon;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import di.kdd.buildmon.Acceleration.AccelerationAxis;

public class AccelerationsSQLiteHelper extends SQLiteOpenHelper {
	
	private static final int DATABASE_VERSION = 4;

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

	private static final String DEBUG_TAG = "database";

	/* The folder that the dump files are placed under */
	
	private static final String BUILD_MON_FOLDER = "BuildMon";
	
	/* Names of the dump files */
	
	private static final String DUMP_X_FILENAME = "x_dump.txt";
	private static final String DUMP_Y_FILENAME = "y_dump.txt";
	private static final String DUMP_Z_FILENAME = "z_dump.txt";		
	
	/***
	 * Initializes the AccelerationsSQLiteHelper
	 * @param context The context of the Application that the SQLite Database belongs to
	 */
	
	public AccelerationsSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

		Log.d(DEBUG_TAG, "Constructor called with name " + DATABASE_NAME + " and version " + DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {	
		Log.d(DEBUG_TAG, "onCreate called");
		
		db.execSQL(TABLE_X_ACCELERATIONS_CREATE);
		Log.d(DEBUG_TAG, TABLE_X_ACCELERATIONS_CREATE);
		
		db.execSQL(TABLE_Y_ACCELERATIONS_CREATE);
		Log.d(DEBUG_TAG, TABLE_Y_ACCELERATIONS_CREATE);

		db.execSQL(TABLE_Z_ACCELERATIONS_CREATE);
		Log.d(DEBUG_TAG, TABLE_Z_ACCELERATIONS_CREATE);
	}

	/***
	 * Drops all the tables and dispatches the onCreate method to create the upgraded tables
	 */
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(DEBUG_TAG, "Dropping " + TABLE_X_ACCELERATIONS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_X_ACCELERATIONS);

		Log.d(DEBUG_TAG, "Dropping " + TABLE_Y_ACCELERATIONS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_Y_ACCELERATIONS);

		Log.d(DEBUG_TAG, "Dropping " + TABLE_Z_ACCELERATIONS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_Z_ACCELERATIONS);

		onCreate(db);
	}

	/***
	 * 
	 * @param acceleration The Acceleration instance to store in the SQLite database
	 * @param axis The Axis that this Acceleration belongs to
	 * @throws Exception If the Axis given is not valid
	 */
	
	public void storeAcceleration(Acceleration acceleration, AccelerationAxis axis) throws Exception {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		
		values.put(COLUMN_TIMESTAMP, acceleration.getTimestamp()); 
		values.put(COLUMN_ACCELERATION, acceleration.getAcceleration());

		switch(axis) {
			case X:
				db.insert(TABLE_X_ACCELERATIONS, null, values);
				break;
			case Y:
				db.insert(TABLE_Y_ACCELERATIONS, null, values);
				break;
			case Z:
				db.insert(TABLE_Z_ACCELERATIONS, null, values);
				break;
			default:
				throw new Exception("Not valid axis");
		}
		
		db.close();
		
		Log.d(DEBUG_TAG, "stored" + acceleration.getAcceleration() + " " + acceleration.getTimestamp());
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
				throw new Exception("Not valid axis");
		}
		
		if(cursor == null) { 
			Log.d(DEBUG_TAG, "No results found");

			return null;
		}
		
		cursor.moveToFirst();
		
		acceleration = new Acceleration(cursor.getFloat(1), timestamp);
		Log.d(DEBUG_TAG, "Selected " + acceleration.getAcceleration() + " " + acceleration.getTimestamp());
		
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
				throw new Exception("Not valid axis");
		}
		
		if(cursor == null) {
			Log.d(DEBUG_TAG, "No results found");

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
				throw new Exception("Not valid axis");
		}
		
		if(cursor == null) {
			Log.d(DEBUG_TAG, "No results found");

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
	 * Creates the BUILD_MON_FOLDER, if it doesn't exist
	 */
	
	private void createBuildMonFodler() {
		File folder = new File(Environment.getExternalStoragePublicDirectory(""), BUILD_MON_FOLDER);
		
		if(!folder.exists()) {
			folder.mkdirs();
		}			
	}	
	
	/***
	 * Dumps all the Accelerations of the three Axis into 3 files, 
	 * on for each Axis (DUMP_X_FILENAME, DUMP_Y_FILENAME, DUMP_Z_FILENAME)
	 * @throws Exception throwed from the IO operations
	 */
	
	public void dumpToFile() throws Exception {		
		File dumpFile;
		PrintWriter printWriter;
		List<Acceleration> accelerations;
		
		createBuildMonFodler();
		
		/* Dump X axis accelerations */
		
		dumpFile = new File(Environment.getExternalStoragePublicDirectory(BUILD_MON_FOLDER), DUMP_X_FILENAME);
		printWriter = new PrintWriter(new FileWriter(dumpFile, false));
		
		accelerations = getAllAccelerations(AccelerationAxis.X);
		
		for(Acceleration acceleration : accelerations) {
			printWriter.println(Long.toString(acceleration.getTimestamp()) + "\t" + Float.toString(acceleration.getAcceleration()));
		}
		
		printWriter.close();
		
		Log.d(DEBUG_TAG, "Wrote " + DUMP_X_FILENAME);
		
		/* Dump Y axis accelerations */

		dumpFile = new File(Environment.getExternalStoragePublicDirectory(BUILD_MON_FOLDER), DUMP_Y_FILENAME);
		printWriter = new PrintWriter(new FileWriter(dumpFile, false));
		
		accelerations = getAllAccelerations(AccelerationAxis.Y);
		
		for(Acceleration acceleration : accelerations) {
			printWriter.println(Long.toString(acceleration.getTimestamp()) + "\t" + Float.toString(acceleration.getAcceleration()));
		}
		
		printWriter.close();

		Log.d(DEBUG_TAG, "Wrote " + DUMP_Y_FILENAME);		
		
		/* Dump Z axis accelerations */

		dumpFile = new File(Environment.getExternalStoragePublicDirectory(BUILD_MON_FOLDER), DUMP_Z_FILENAME);
		printWriter = new PrintWriter(new FileWriter(dumpFile, false));
		
		accelerations = getAllAccelerations(AccelerationAxis.Z);
		
		for(Acceleration acceleration : accelerations) {
			printWriter.println(Long.toString(acceleration.getTimestamp()) + "\t" + Float.toString(acceleration.getAcceleration()));
		}
		
		printWriter.close();

		Log.d(DEBUG_TAG, "Wrote " + DUMP_Z_FILENAME);
	}
}
