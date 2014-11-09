package com.example.android.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.data.WeatherContract;


public class DetailActivity extends ActionBarActivity {


    public static final String DATE_KEY = "date"; // date DB field
    public static final String LOCATION_KEY = "location"; // location DB field

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)  {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();
        private static final String FORECAST_SHARE_HASHTAG=" #SunShine";
        //private String mForecastStr;
        private String mLocation;
        private String mForecast;
        private static final int DETAIL_LOADER = 0;

        public ImageView mIconView;
        public TextView mFriendlyDateView;
        public TextView mDateView;
        public TextView mDescriptionView;
        public TextView mHighTempView;
        public TextView mLowTempView;
        public TextView mHumidityView;
        public TextView mWindView;
        public TextView mPressureView;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            if (null != savedInstanceState) {
                mLocation = savedInstanceState.getString(LOCATION_KEY);
            }
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            mIconView = (ImageView)rootView.findViewById(R.id.detail_icon);
            mDateView = (TextView)rootView.findViewById(R.id.detail_date_textview);
            mFriendlyDateView = (TextView)rootView.findViewById(R.id.detail_day_textview);
            mDescriptionView = (TextView)rootView.findViewById(R.id.detail_forecast_textview);
            mHighTempView = (TextView)rootView.findViewById(R.id.detail_high_textview);
            mLowTempView = (TextView)rootView.findViewById(R.id.detail_low_textview);
            mHumidityView = (TextView)rootView.findViewById(R.id.detail_humidity_textview);
            mWindView = (TextView)rootView.findViewById(R.id.detail_wind_textview);
            mPressureView = (TextView)rootView.findViewById(R.id.detail_pressure_textview);

            // Not needed anymore since OnCreateLoader already does this including after view is destroyed (e.g. via screen flip)
            //Intent intent = getActivity().getIntent();
            //if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)){
            //    mForecast = intent.getStringExtra(Intent.EXTRA_TEXT);
             //   ((TextView) rootView.findViewById(R.id.detail_text))
             //           .setText(mForecast);
            //}

            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Inflate the menu item; contributes this item to the action bar of DetailActivity (if setHasOptionsMenu is set to true like above)
            inflater.inflate(R.menu.detailfragment,menu);

            // Retrieve the share menu item
            MenuItem item = menu.findItem(R.id.action_share);

            // Get the provider and hold onto it to set/change the share intent
            ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

            // Attach an intent to this ShareActionProvider. You can update this at any time,
            // like when the user selects a new piece of data they might liek to share
            if (shareActionProvider != null) {
                shareActionProvider.setShareIntent(createShareForecastIntent());
            }
            else {
                Log.d(LOG_TAG, "Share Action Provider is NULL?");
            }
        }

        private Intent createShareForecastIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
            return shareIntent;
        }

        // used to preserve instance state of activity into the bundle before being killed (e.g. screen flip, action bar item clicked) so state can be restored on Create or onRestoreInstanceState
        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);

            if (null != mLocation) {
                outState.putString(LOCATION_KEY, mLocation);
            }
        }

        @Override
        // Called when hitting "Back"
        public void onResume() {
            super.onResume();
            if (mLocation != null &&
                    !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
                // we should listen for changes on a new URI
                getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
            }
        }

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            String dateString = getActivity().getIntent().getStringExtra(DATE_KEY);

            // This is called when a new loader needs to be created. This fragment
            // only uses one loader, so we don't care about checking the id.

            // For the forecast view we're showing only a small subset of the stored data.
            //Specify the columns  we need.
            String[] columns= {
                    // In this case the id needs to be fully qualified with a table name, since
                    // the content provider joins the location & weather tables in the background
                    // (both have an _id column)
                    // On the one hand, that's annoying. On the other, you can search weather
                    // using the postal code, which is only in the location table. So the
                    // convenience is worth it
                    WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                    WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                    WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                    WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                    WeatherContract. WeatherEntry.COLUMN_MIN_TEMP,
                    WeatherContract. WeatherEntry.COLUMN_HUMIDITY,
                    WeatherContract. WeatherEntry.COLUMN_PRESSURE,
                    WeatherContract. WeatherEntry.COLUMN_WIND_SPEED,
                    WeatherContract. WeatherEntry.COLUMN_DEGREES,
                    WeatherContract. WeatherEntry.COLUMN_WEATHER_ID,
                    // This works because the WeatherProvider returns location data joined with weather data
                    // even though they're stored in two different tables
                    WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
            };

            mLocation = Utility.getPreferredLocation(getActivity());
            Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(mLocation, dateString);

            // Now create and return a CursorLoader that will take care of creating
            // a Cursor for the data being displayed
            return new CursorLoader(
                    getActivity(),
                    weatherUri,
                    columns,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            if (cursor.moveToFirst()) {

                // Read weather condition ID form cursor
                int weatherId = cursor.getInt(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));

                // Use placeholder image for now
                mIconView.setImageResource(R.drawable.ic_launcher);

                String desc = cursor.getString(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC));
                String date = cursor.getString(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT));
                String dateText = Utility.getFormattedMonthDay(getActivity(), date);
                String friendlyDateText = Utility.getDayName(getActivity(), date);
                double high = cursor.getDouble(cursor.getColumnIndex((WeatherContract.WeatherEntry.COLUMN_MAX_TEMP)));
                double low = cursor.getDouble(cursor.getColumnIndex((WeatherContract.WeatherEntry.COLUMN_MIN_TEMP)));
                float humidity = cursor.getFloat(cursor.getColumnIndex((WeatherContract.WeatherEntry.COLUMN_HUMIDITY)));
                float pressure = cursor.getFloat(cursor.getColumnIndex((WeatherContract.WeatherEntry.COLUMN_PRESSURE)));
                float wind_speed = cursor.getFloat(cursor.getColumnIndex((WeatherContract.WeatherEntry.COLUMN_WIND_SPEED)));
                float wind_dir = cursor.getFloat(cursor.getColumnIndex((WeatherContract.WeatherEntry.COLUMN_DEGREES)));

                boolean isMetric = Utility.isMetric(getActivity());

//              TextView dateView = (TextView)getView().findViewById(R.id.detail_date_textview);
//              TextView descriptionView = (TextView)getView().findViewById(R.id.detail_forecast_textview);
//              TextView highTempView = (TextView)getView().findViewById(R.id.detail_high_textview);
//              TextView lowTempView = (TextView)getView().findViewById(R.id.detail_low_textview);

                mDateView.setText(dateText);
                mFriendlyDateView.setText(friendlyDateText);
                mDescriptionView.setText(desc);
                mHighTempView.setText(Utility.formatTemperature(getActivity(), high, isMetric));
                mLowTempView.setText(Utility.formatTemperature(getActivity(), low, isMetric));
                mWindView.setText(Utility.getFormattedWind(getActivity(),wind_speed,wind_dir));
                mPressureView.setText(getActivity().getString(R.string.format_pressure, pressure));
                mHumidityView.setText(getActivity().getString(R.string.format_humidity, humidity));

                // We still need this for the share intent
                mForecast = String.format("%s - %s - %s/%s",
                        mDateView.getText(),
                        mDescriptionView.getText(),
                        mHighTempView.getText(),
                        mLowTempView.getText());
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {

        }
    }
}

