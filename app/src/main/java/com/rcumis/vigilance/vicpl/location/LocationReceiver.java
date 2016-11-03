package com.rcumis.vigilance.vicpl.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.LocationResult;
import com.rcumis.vigilance.vicpl.network.HttpClientHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Arjun on 23/10/16.
 * Receiver to handle the location data
 */
public class LocationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e("LocationReceiver", "Received unexpected intent " + intent.getAction());

        // Need to check and grab the Intent's extras like so
        if (LocationResult.hasResult(intent)) {
            LocationResult mLocationResult = LocationResult.extractResult(intent);
            Log.i("LocationReceiver", "Location Received: " + mLocationResult.toString());
            HttpClientHelper helper = new HttpClientHelper(context);
            helper.sendLocationToServer(mLocationResult.getLastLocation());
        }
    }
}
