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
            appendLog(dateFormat.format(new Date()) + " : " + mLocationResult.toString());
        }
    }

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    String fileName = "logs.txt";
    public void appendLog(String text) {

        File logsDir = FileUtils.geLogsDirectory();
        File logFile = new File(logsDir, fileName);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
