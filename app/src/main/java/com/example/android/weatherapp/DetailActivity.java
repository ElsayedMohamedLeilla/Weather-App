package com.example.android.weatherapp;

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.android.weatherapp.data.WeatherContract;
import com.example.android.weatherapp.databinding.ActivityDetailBinding;
import com.example.android.weatherapp.utilities.SunshineDateUtils;
import com.example.android.weatherapp.utilities.SunshineWeatherUtils;

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks <Cursor> {

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";


    public static final String[] WEATHER_DETAIL_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };


    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_HUMIDITY = 3;
    public static final int INDEX_WEATHER_PRESSURE = 4;
    public static final int INDEX_WEATHER_WIND_SPEED = 5;
    public static final int INDEX_WEATHER_DEGREES = 6;
    public static final int INDEX_WEATHER_CONDITION_ID = 7;


    private static final int ID_DETAIL_LOADER = 353;

    /* A summary of the forecast that can be shared by clicking the share button in the ActionBar */
    private String mForecastSummary;

    /* The URI that is used to access the chosen day's weather details */
    private Uri mUri;


    private ActivityDetailBinding mDetailBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );


        mDetailBinding = DataBindingUtil.setContentView( this, R.layout.activity_detail );

        mUri = getIntent().getData();
        if (mUri == null) throw new NullPointerException( "URI for DetailActivity cannot be null" );

        /* This connects our Activity into the loader lifecycle. */
        getSupportLoaderManager().initLoader( ID_DETAIL_LOADER, null, this );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate( R.menu.detail, menu );
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* Get the ID of the clicked item */
        int id = item.getItemId();

        /* Settings menu item clicked */
        if (id == R.id.action_settings) {
            startActivity( new Intent( this, SettingsActivity.class ) );
            return true;
        }

        /* Share menu item clicked */
        if (id == R.id.action_share) {
            Intent shareIntent = createShareForecastIntent();
            startActivity( shareIntent );
            return true;
        }

        return super.onOptionsItemSelected( item );
    }


    private Intent createShareForecastIntent() {
        Intent shareIntent = ShareCompat.IntentBuilder.from( this )
                .setType( "text/plain" )
                .setText( mForecastSummary + FORECAST_SHARE_HASHTAG )
                .getIntent();
        shareIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_DOCUMENT );
        return shareIntent;
    }


    @Override
    public Loader <Cursor> onCreateLoader(int loaderId, Bundle loaderArgs) {

        switch (loaderId) {

            case ID_DETAIL_LOADER:

                return new CursorLoader( this,
                        mUri,
                        WEATHER_DETAIL_PROJECTION,
                        null,
                        null,
                        null );

            default:
                throw new RuntimeException( "Loader Not Implemented: " + loaderId );
        }
    }


    @Override
    public void onLoadFinished(Loader <Cursor> loader, Cursor data) {


        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            /* We have valid data, continue on to bind the data to the UI */
            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {
            /* No data to display, simply return and do nothing */
            return;
        }

        int weatherId = data.getInt( INDEX_WEATHER_CONDITION_ID );
        /* Use our utility method to determine the resource ID for the proper art */
        int weatherImageId = SunshineWeatherUtils.getLargeArtResourceIdForWeatherCondition( weatherId );

        /* Set the resource ID on the icon to display the art */
        mDetailBinding.primaryInfo.weatherIcon.setImageResource( weatherImageId );


        long localDateMidnightGmt = data.getLong( INDEX_WEATHER_DATE );
        String dateText = SunshineDateUtils.getFriendlyDateString( this, localDateMidnightGmt, true );

        mDetailBinding.primaryInfo.date.setText( dateText );

        String description = SunshineWeatherUtils.getStringForWeatherCondition( this, weatherId );

        /* Create the accessibility (a11y) String from the weather description */
        String descriptionA11y = getString( R.string.a11y_forecast, description );

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.primaryInfo.weatherDescription.setText( description );
        mDetailBinding.primaryInfo.weatherDescription.setContentDescription( descriptionA11y );

        /* Set the content description on the weather image (for accessibility purposes) */
        mDetailBinding.primaryInfo.weatherIcon.setContentDescription( descriptionA11y );
        double highInCelsius = data.getDouble( INDEX_WEATHER_MAX_TEMP );

        String highString = SunshineWeatherUtils.formatTemperature( this, highInCelsius );

        /* Create the accessibility (a11y) String from the weather description */
        String highA11y = getString( R.string.a11y_high_temp, highString );

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.primaryInfo.highTemperature.setText( highString );
        mDetailBinding.primaryInfo.highTemperature.setContentDescription( highA11y );


        double lowInCelsius = data.getDouble( INDEX_WEATHER_MIN_TEMP );

        String lowString = SunshineWeatherUtils.formatTemperature( this, lowInCelsius );

        String lowA11y = getString( R.string.a11y_low_temp, lowString );

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.primaryInfo.lowTemperature.setText( lowString );
        mDetailBinding.primaryInfo.lowTemperature.setContentDescription( lowA11y );


        float humidity = data.getFloat( INDEX_WEATHER_HUMIDITY );
        String humidityString = getString( R.string.format_humidity, humidity );

        String humidityA11y = getString( R.string.a11y_humidity, humidityString );

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.extraDetails.humidity.setText( humidityString );
        mDetailBinding.extraDetails.humidity.setContentDescription( humidityA11y );

        mDetailBinding.extraDetails.humidityLabel.setContentDescription( humidityA11y );


        float windSpeed = data.getFloat( INDEX_WEATHER_WIND_SPEED );
        float windDirection = data.getFloat( INDEX_WEATHER_DEGREES );
        String windString = SunshineWeatherUtils.getFormattedWind( this, windSpeed, windDirection );

        String windA11y = getString( R.string.a11y_wind, windString );

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.extraDetails.windMeasurement.setText( windString );
        mDetailBinding.extraDetails.windMeasurement.setContentDescription( windA11y );

        mDetailBinding.extraDetails.windLabel.setContentDescription( windA11y );


        float pressure = data.getFloat( INDEX_WEATHER_PRESSURE );


        String pressureString = getString( R.string.format_pressure, pressure );

        String pressureA11y = getString( R.string.a11y_pressure, pressureString );

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.extraDetails.pressure.setText( pressureString );
        mDetailBinding.extraDetails.pressure.setContentDescription( pressureA11y );

        mDetailBinding.extraDetails.pressureLabel.setContentDescription( pressureA11y );

        /* Store the forecast summary String in our forecast summary field to share later */
        mForecastSummary = String.format( "%s - %s - %s/%s",
                dateText, description, highString, lowString );
    }


    @Override
    public void onLoaderReset(Loader <Cursor> loader) {
    }
}