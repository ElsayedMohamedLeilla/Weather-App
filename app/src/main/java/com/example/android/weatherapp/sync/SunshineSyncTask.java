package com.example.android.weatherapp.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.text.format.DateUtils;

import com.example.android.weatherapp.data.SunshinePreferences;
import com.example.android.weatherapp.data.WeatherContract;
import com.example.android.weatherapp.utilities.NetworkUtils;
import com.example.android.weatherapp.utilities.NotificationUtils;
import com.example.android.weatherapp.utilities.OpenWeatherJsonUtils;

import java.net.URL;

public class SunshineSyncTask {


    synchronized public static void syncWeather(Context context) {

        try {

            URL weatherRequestUrl = NetworkUtils.getUrl( context );

            /* Use the URL to retrieve the JSON */
            String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl( weatherRequestUrl );

            /* Parse the JSON into a list of weather values */
            ContentValues[] weatherValues = OpenWeatherJsonUtils
                    .getWeatherContentValuesFromJson( context, jsonWeatherResponse );


            if (weatherValues != null && weatherValues.length != 0) {
                /* Get a handle on the ContentResolver to delete and insert data */
                ContentResolver sunshineContentResolver = context.getContentResolver();

                /* Delete old weather data because we don't need to keep multiple days' data */
                sunshineContentResolver.delete(
                        WeatherContract.WeatherEntry.CONTENT_URI,
                        null,
                        null );

                /* Insert our new weather data into Sunshine's ContentProvider */
                sunshineContentResolver.bulkInsert(
                        WeatherContract.WeatherEntry.CONTENT_URI,
                        weatherValues );


                boolean notificationsEnabled = SunshinePreferences.areNotificationsEnabled( context );


                long timeSinceLastNotification = SunshinePreferences
                        .getEllapsedTimeSinceLastNotification( context );

                boolean oneDayPassedSinceLastNotification = false;

                if (timeSinceLastNotification >= DateUtils.DAY_IN_MILLIS) {
                    oneDayPassedSinceLastNotification = true;
                }


                if (notificationsEnabled && oneDayPassedSinceLastNotification) {
                    NotificationUtils.notifyUserOfNewWeather( context );
                }


            }

        } catch (Exception e) {
            /* Server probably invalid */
            e.printStackTrace();
        }
    }
}