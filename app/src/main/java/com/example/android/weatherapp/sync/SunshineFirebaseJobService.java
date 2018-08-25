package com.example.android.weatherapp.sync;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;


public class SunshineFirebaseJobService extends JobService {

    private AsyncTask <Void, Void, Void> mFetchWeatherTask;

    @SuppressLint("StaticFieldLeak")
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {

        mFetchWeatherTask = new AsyncTask <Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Context context = getApplicationContext();
                SunshineSyncTask.syncWeather( context );
                jobFinished( jobParameters, false );
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                jobFinished( jobParameters, false );
            }
        };

        mFetchWeatherTask.execute();
        return true;
    }


    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if (mFetchWeatherTask != null) {
            mFetchWeatherTask.cancel( true );
        }
        return true;
    }
}