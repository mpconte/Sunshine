package com.example.android.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.sunshine.data.WeatherContract;

import java.util.Date;

//import android.content.Loader;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int FORECAST_LOADER = 0;
    private String mLocation;

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATETEXT,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;


    private ForecastAdapter mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment,menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        String[] foreCastArray = {
//                "Mon 6/23 - Sunny - 31/17",
//                "Tue 6/24 - Foggy - 21/8",
//                "Wed 6/25 - Cloudy - 22/17",
//                "Thurs 6/26 - Rainy - 18/11",
//                "Fri 6/27 - Foggy - 21/10",
//                "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
//                "Sun 6/29 - Sunny - 20/7"
//        };
//        List<String> weekForecast = new ArrayList<String>(Arrays.asList(foreCastArray));

        // The SimpleCursorAdapter will take data from the database through the
        // Loader and use it to populate the ListView it's attached to.
        mForecastAdapter = new ForecastAdapter(getActivity(),null,0);
//                  new SimpleCursorAdapter(
//                getActivity(),
//                R.layout.list_item_forecast,
//                null,
//                // the column names to use to fill the textviews
//                new String[]{WeatherContract.WeatherEntry.COLUMN_DATETEXT,
//                        WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
//                        WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
//                        WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
//                },
//                // the textviews to fill with the data pulled from the columns above
//                new int[]{R.id.list_item_date_textview,
//                        R.id.list_item_forecast_textview,
//                        R.id.list_item_high_textview,
//                        R.id.list_item_low_textview
//                },
//                0
//        );

//        mForecastAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
//            @Override
//            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
//                boolean isMetric = Utility.isMetric(getActivity());
//                switch (columnIndex) {
//                    case COL_WEATHER_MAX_TEMP:
//                    case COL_WEATHER_MIN_TEMP: {
//                        // we have to do some formatting and possibly a conversion
//                        ((TextView) view).setText(Utility.formatTemperature(
//                                cursor.getDouble(columnIndex), isMetric));
//                        return true;
//                    }
//                    case COL_WEATHER_DATE: {
//                        String dateString = cursor.getString(columnIndex);
//                        TextView dateView = (TextView) view;
//                        dateView.setText(Utility.formatDate(dateString));
//                        return true;
//                    }
//                }
//                return false;
//            }
//        });


        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView and attach this adapter to it
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//              String forecast = mForecastAdapter.getItem(position);

                ForecastAdapter adapter = (ForecastAdapter) parent.getAdapter();
                Cursor cursor = adapter.getCursor();
                if (null != cursor && cursor.moveToPosition(position)) {

                    // Below is removed since our cursor points to the appropriate ListView entry for weather date to get our forecast data from
                    // boolean isMetric = Utility.isMetric(getActivity());
//                    String forecast = String.format("%s - %s - %s/%s",
//                            Utility.formatDate(cursor.getString(COL_WEATHER_DATE)),
//                            cursor.getString(COL_WEATHER_DESC),
//                            Utility.formatTemperature(cursor.getDouble(COL_WEATHER_MAX_TEMP), isMetric),
//                            Utility.formatTemperature(cursor.getDouble(COL_WEATHER_MIN_TEMP), isMetric));
                    Intent intent = new Intent(getActivity(), DetailActivity.class);
                    //intent.putExtra(intent.EXTRA_TEXT, cursor.getString(COL_WEATHER_DATE)forecast);
                    intent.putExtra(DetailActivity.DATE_KEY, cursor.getString(COL_WEATHER_DATE));
                    startActivity(intent);
                    // Toast.makeText(getActivity(), forecast, Toast.LENGTH_LONG).show();
                }
            }
        });

        return rootView;
    }

    private void updateWeather() {
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity(), mForecastAdapter);
        //SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //String value = shared.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        //weatherTask.execute(value);

        String location = Utility.getPreferredLocation(getActivity());
        weatherTask.execute(location);
    }

    @Override
    public void onStart() {
        super.onStart();

        // We no longer want to fetch the same weather data every time the activity starts, since we're storing it in a database now.
        // This does still means that if you clear the data in your app or run it on a new emulator or device, you'll have to hit the refresh button, though.
        //updateWeather(); // This simply calls OpenWeather API to get weather for our default "preferred" location every time
    }

    @Override
    public void onResume() {
        super.onResume();

        // If preferred location has changed, we can now fetch weather data of the new preferred location
        if (mLocation != null && !Utility.getPreferredLocation(getActivity()).equals(mLocation)) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, get the String representation for today,
        // and filter the query to return weather only for dates after or including today.
        // Only return data after today.
        String startDate = WeatherContract.getDbDateString(new Date());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";

        mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                mLocation, startDate);

        Log.d("ForecastFragment", "Uri: " + weatherForLocationUri.toString());

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }
}
