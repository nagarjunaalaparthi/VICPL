package com.rcumis.vigilance.vicpl.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Nagarjuna on 23/10/16.
 */
public class LocationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e("LocationReceiver", "Received unexpected intent " + intent.getAction());

        // Need to check and grab the Intent's extras like so
        if (LocationResult.hasResult(intent)) {
            LocationResult mLocationResult = LocationResult.extractResult(intent);
            Log.i("LocationReceiver", "Location Received: " + mLocationResult.toString());
            appendLog(dateFormat.format(new Date()) + " : " + mLocationResult.toString());
        }
    }

    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String msg = Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Log.d("debug", msg);
        // Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        appendLog(dateFormat.format(new Date()) + " : " + msg);
    }

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
