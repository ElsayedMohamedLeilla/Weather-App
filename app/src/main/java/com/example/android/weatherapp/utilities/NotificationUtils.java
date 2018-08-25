package com.example.android.weatherapp.utilities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;

import com.example.android.weatherapp.DetailActivity;
import com.example.android.weatherapp.R;
import com.example.android.weatherapp.data.SunshinePreferences;
import com.example.android.weatherapp.data.WeatherContract;

public class NotificationUtils {


    public static final String[] WEATHER_NOTIFICATION_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
    };


    public static final int INDEX_WEATHER_ID = 0;
    public static final int INDEX_MAX_TEMP = 1;
    public static final int INDEX_MIN_TEMP = 2;


    private static final int WEATHER_NOTIFICATION_ID = 3004;


    public static void notifyUserOfNewWeather(Context context) {

        /* Build the URI for today's weather in order to show up to date data in notification */
        Uri todaysWeatherUri = WeatherContract.WeatherEntry
                .buildWeatherUriWithDate( SunshineDateUtils.normalizeDate( System.currentTimeMillis() ) );

        Cursor todayWeatherCursor = context.getContentResolver().query(
                todaysWeatherUri,
                WEATHER_NOTIFICATION_PROJECTION,
                null,
                null,
                null );


        if (todayWeatherCursor.moveToFirst()) {

            /* Weather ID as returned by API, used to identify the icon to be used */
            int weatherId = todayWeatherCursor.getInt( INDEX_WEATHER_ID );
            double high = todayWeatherCursor.getDouble( INDEX_MAX_TEMP );
            double low = todayWeatherCursor.getDouble( INDEX_MIN_TEMP );

            Resources resources = context.getResources();
            int largeArtResourceId = SunshineWeatherUtils
                    .getLargeArtResourceIdForWeatherCondition( weatherId );

            Bitmap largeIcon = BitmapFactory.decodeResource(
                    resources,
                    largeArtResourceId );

            String notificationTitle = context.getString( R.string.app_name );

            String notificationText = getNotificationText( context, weatherId, high, low );

            /* getSmallArtResourceIdForWeatherCondition returns the proper art to show given an ID */
            int smallArtResourceId = SunshineWeatherUtils
                    .getSmallArtResourceIdForWeatherCondition( weatherId );


            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder( context )
                    .setColor( ContextCompat.getColor( context, R.color.colorPrimary ) )
                    .setSmallIcon( smallArtResourceId )
                    .setLargeIcon( largeIcon )
                    .setContentTitle( notificationTitle )
                    .setContentText( notificationText )
                    .setAutoCancel( true );


            Intent detailIntentForToday = new Intent( context, DetailActivity.class );
            detailIntentForToday.setData( todaysWeatherUri );

            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create( context );
            taskStackBuilder.addNextIntentWithParentStack( detailIntentForToday );
            PendingIntent resultPendingIntent = taskStackBuilder
                    .getPendingIntent( 0, PendingIntent.FLAG_UPDATE_CURRENT );

            notificationBuilder.setContentIntent( resultPendingIntent );

            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService( Context.NOTIFICATION_SERVICE );

            notificationManager.notify( WEATHER_NOTIFICATION_ID, notificationBuilder.build() );


            SunshinePreferences.saveLastNotificationTime( context, System.currentTimeMillis() );
        }

        /* Always close your cursor when you're done with it to avoid wasting resources. */
        todayWeatherCursor.close();
    }


    private static String getNotificationText(Context context, int weatherId, double high, double low) {


        String shortDescription = SunshineWeatherUtils
                .getStringForWeatherCondition( context, weatherId );

        String notificationFormat = context.getString( R.string.format_notification );

        /* Using String's format method, we create the forecast summary */
        String notificationText = String.format( notificationFormat,
                shortDescription,
                SunshineWeatherUtils.formatTemperature( context, high ),
                SunshineWeatherUtils.formatTemperature( context, low ) );

        return notificationText;
    }
}
