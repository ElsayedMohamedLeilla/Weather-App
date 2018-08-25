package com.example.android.weatherapp.sync;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.example.android.weatherapp.data.WeatherContract;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

public class SunshineSyncUtils {


    private static final int SYNC_INTERVAL_HOURS = 3;
    private static final int SYNC_INTERVAL_SECONDS = (int) TimeUnit.HOURS.toSeconds( SYNC_INTERVAL_HOURS );
    private static final int SYNC_FLEXTIME_SECONDS = SYNC_INTERVAL_SECONDS / 3;

    private static boolean sInitialized;

    private static final String SUNSHINE_SYNC_TAG = "sunshine-sync";


    static void scheduleFirebaseJobDispatcherSync(@NonNull final Context context) {

        Driver driver = new GooglePlayDriver( context );
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher( driver );

        /* Create the Job to periodically sync Sunshine */
        Job syncSunshineJob = dispatcher.newJobBuilder()
                /* The Service that will be used to sync Sunshine's data */
                .setService( SunshineFirebaseJobService.class )
                /* Set the UNIQUE tag used to identify this Job */
                .setTag( SUNSHINE_SYNC_TAG )

                .setConstraints( Constraint.ON_ANY_NETWORK )
                .setLifetime( Lifetime.FOREVER )

                .setRecurring( true )

                .setTrigger( Trigger.executionWindow(
                        SYNC_INTERVAL_SECONDS,
                        SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS ) )

                .setReplaceCurrent( true )
                /* Once the Job is ready, call the builder's build method to return the Job */
                .build();

        /* Schedule the Job with the dispatcher */
        dispatcher.schedule( syncSunshineJob );
    }

    synchronized public static void initialize(@NonNull final Context context) {


        if (sInitialized) return;

        sInitialized = true;


        scheduleFirebaseJobDispatcherSync( context );


        Thread checkForEmpty = new Thread( new Runnable() {
            @Override
            public void run() {

                /* URI for every row of weather data in our weather table*/
                Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;


                String[] projectionColumns = {WeatherContract.WeatherEntry._ID};
                String selectionStatement = WeatherContract.WeatherEntry
                        .getSqlSelectForTodayOnwards();

                /* Here, we perform the query to check to see if we have any weather data */
                Cursor cursor = context.getContentResolver().query(
                        forecastQueryUri,
                        projectionColumns,
                        selectionStatement,
                        null,
                        null );

                if (null == cursor || cursor.getCount() == 0) {
                    startImmediateSync( context );
                }

                /* Make sure to close the Cursor to avoid memory leaks! */
                cursor.close();
            }
        } );

        checkForEmpty.start();
    }


    public static void startImmediateSync(@NonNull final Context context) {
        Intent intentToSyncImmediately = new Intent( context, SunshineSyncIntentService.class );
        context.startService( intentToSyncImmediately );
    }
}