package com.example.android.sunshine.test;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.android.sunshine.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;

import java.util.Map;
import java.util.Set;

/**
 * Created by mattconte on 14/10/14.
 */
public class TestProvider extends AndroidTestCase{
    public static final String LOG_TAG = TestDB.class.getSimpleName();

    static public String TEST_CITY_NAME = "North Pole";
    static public String TEST_LOCATION = "99705";
    static public String TEST_DATE= "20141205";

    // The target api annotation is needed for the call to keySet -- we wouldn't want
    // to use this in our app, but in a test it's fine to assume a higher target.
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void addAllContentValues(ContentValues destination, ContentValues source) {
        for (String key : source.keySet()) {
            destination.put(key, source.getAsString(key));
        }
    }

    // Deletes entire database!!
    //public void testDeleteDb() throws Throwable {
    //    mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    //}



    public void testInsertReadProvider() {

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        //WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        //SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestDB.createNorthPoleLocationValues();

        long locationRowId;
        //locationRowId = db.insert(LocationEntry.TABLE_NAME, null, testValues);
        Uri locationUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, testValues);
        locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDB.validateCursor(cursor, testValues);

        cursor.close();

        // Now see if we can successfully query if we include the row id
        cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDB.validateCursor(cursor, testValues);

        // Fantastic.  Now that we have a location, let's add some weather!
        ContentValues weatherValues = TestDB.createWeatherValues(locationRowId);

        // ID for the row in the weather table of our DB
        long weatherRowId;

        // add row to a test DB
        //weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);

        // add row to our provider
        Uri weatherInsertUri = mContext.getContentResolver().insert(WeatherEntry.CONTENT_URI, weatherValues);
        assertTrue(weatherInsertUri != null);
        weatherRowId = ContentUris.parseId(weatherInsertUri);

        // Assert that a row was inserted
        assertTrue(weatherRowId != -1);

        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // sort order
        );

        TestDB.validateCursor(weatherCursor, weatherValues);

        weatherCursor.close();

        // Cursor for test location query
        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocation(TEST_LOCATION),  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // sort order
        );

        TestDB.validateCursor(weatherCursor, weatherValues);

        weatherCursor.close();

        // Cursor for test location with start date query (i.e. >= TEST_DATE)
        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithStartDate(TEST_LOCATION, TEST_DATE),  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // sort order
        );

        TestDB.validateCursor(weatherCursor, weatherValues);

        weatherCursor.close();

        // Cursor for test location with date query (i.e. = TEST_DATE)
        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithDate(TEST_LOCATION, TEST_DATE),  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // sort order
        );

        TestDB.validateCursor(weatherCursor, weatherValues);

        weatherCursor.close();



       //dbHelper.close();
    }

    public void testUpdateLocation() {
        testDeleteAllRecords();

        // Create a new map of values, where column names are the keys
        ContentValues values = TestDB.createNorthPoleLocationValues();

        Uri locationUri = mContext.getContentResolver().
                insert(LocationEntry.CONTENT_URI, values);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(LocationEntry._ID, locationRowId);
        updatedValues.put(LocationEntry.COLUMN_CITY_NAME, "Santa's Village");

        int count = mContext.getContentResolver().update(
                LocationEntry.CONTENT_URI, updatedValues, LocationEntry._ID + "= ?",
                new String[] { Long.toString(locationRowId)});

        assertEquals(count, 1);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );

        TestDB.validateCursor(cursor, updatedValues);

        cursor.close();
    }

    public void testDeleteAllRecords() {

        // Delete weather table first, since weather entries don't exist if location don't also exist
        mContext.getContentResolver().delete(
                WeatherEntry.CONTENT_URI,
                null,
                null
        );

        // Delete location table
        mContext.getContentResolver().delete(
                LocationEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        assertEquals(cursor.getCount(), 0);
        cursor.close();

        cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        assertEquals(cursor.getCount(), 0);
        cursor.close();
    }

    static public ContentValues getLocationContentValues(long locationRowId) {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, TEST_DATE);
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);

        return weatherValues;
    }

    static ContentValues createWeatherValues(long locationRowId) {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, TEST_DATE);
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);

        return weatherValues;
    }

    static ContentValues createNorthPoleLocationValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(LocationEntry.COLUMN_CITY_NAME, TEST_CITY_NAME);
        testValues.put(LocationEntry.COLUMN_LOCATION_SETTING, TEST_LOCATION);
        testValues.put(LocationEntry.COLUMN_COORD_LAT, 64.7488);
        testValues.put(LocationEntry.COLUMN_COORD_LONG, -147.353);

        return testValues;
    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }

    public void testGetType() {
        // content://com.example.android.sunshine/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testLocation = "94074";
        // content://com.example.android.sunshine/weather/94074
        type = mContext.getContentResolver().getType(
        WeatherEntry.buildWeatherLocation(testLocation));
        // vnd.android.cursor.dir/com.example.android.sunshine/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testDate = "20140612";
        // content://com.example.android.sunshine/weather/94074/20140612
        type = mContext.getContentResolver().getType(
         WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        // vnd.android.cursor.item/com.example.android.sunshine/weather
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://com.example.android.sunshine/location/
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine/location
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        // content://com.example.android.sunshine/location/1
        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/com.example.android.sunshine/location
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
        }
}
