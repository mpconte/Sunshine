package com.example.android.sunshine.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.android.sunshine.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.data.WeatherDbHelper;

/**
 * Created by mattconte on 14/10/14.
 */
public class TestDB extends AndroidTestCase{
    public static final String LOG_TAG = TestDB.class.getSimpleName();

    public void testCreateDB() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDB() {
        // Test data we're going to insert into DB to see if it works
        String testName = "North Pole";
        String testLocationSetting = "99705";
        double testLongitude = 64.772;
        double testLatitude = -147.355;

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create map of values where column names are keys
        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_CITY_NAME, testName);
        values.put(LocationEntry.COLUMN_LOCATION_SETTING, testLocationSetting);
        values.put(LocationEntry.COLUMN_COORD_LAT, testLatitude);
        values.put(LocationEntry.COLUMN_COORD_LONG, testLongitude);

        long locationRowId;
        locationRowId = db.insert(LocationEntry.TABLE_NAME, null, values);

        // Verify we got a row back
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        //Specify which columns you want
        String[] columns = {
                LocationEntry._ID,
                LocationEntry.COLUMN_LOCATION_SETTING,
                LocationEntry.COLUMN_CITY_NAME,
                LocationEntry.COLUMN_COORD_LAT,
                LocationEntry.COLUMN_COORD_LONG
        };

        // A cursor is a primary interface to the query results
        Cursor cursor = db.query(
                LocationEntry.TABLE_NAME, // table to query
                columns, // columns to query
                null,// WHERE clause columns
                null,// values for WHERE clause
                null,//columns to group by
                null,//columns to filter by row groups
                null//sort order
        );

        if (cursor.moveToFirst()) {
            // Get the value in each column by finding the appropriate column index
            int locationIdx = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_SETTING);
            String location = cursor.getString(locationIdx);

            int cityIdx = cursor.getColumnIndex(LocationEntry.COLUMN_CITY_NAME);
            String name = cursor.getString(cityIdx);

            int latIdx = cursor.getColumnIndex(LocationEntry.COLUMN_COORD_LAT);
            double latitude = cursor.getDouble(latIdx);

            int longIdx = cursor.getColumnIndex(LocationEntry.COLUMN_COORD_LONG);
            double longitude = cursor.getDouble(longIdx);

            // Assert that it's the right data
            assertEquals(testName,name);
            assertEquals(testLocationSetting,location);
            assertEquals(testLatitude,latitude);
            assertEquals(testLongitude,longitude);

            // Awesome, we have a location, no let's add weather data
        }
        else {
            // Weird...
            fail("No values return :(");
        }

    }
}
