package com.rcumis.vigilance.vicpl.location;


/**
 * Created by Nagarjuna on 23/10/16.
 */

        import android.Manifest;
        import android.app.PendingIntent;
        import android.app.Service;
        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.content.pm.PackageManager;
        import android.location.Location;
        import android.os.Binder;
        import android.os.Bundle;
        import android.os.IBinder;
        import android.os.PowerManager;
        import android.support.v4.content.ContextCompat;
        import android.util.Log;

        import com.google.android.gms.common.ConnectionResult;
        import com.google.android.gms.common.GoogleApiAvailability;
        import com.google.android.gms.common.api.GoogleApiClient;
        import com.google.android.gms.location.LocationListener;
        import com.google.android.gms.location.LocationRequest;
        import com.google.android.gms.location.LocationServices;

        import java.io.BufferedWriter;
        import java.io.File;
        import java.io.FileWriter;
        import java.io.IOException;
        import java.text.SimpleDateFormat;
        import java.util.Date;

/**
 *
 * BackgroundLocationService used for tracking user location in the background.
 * @author cblack
 */
public class BackgroundLocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    IBinder mBinder = new LocalBinder();

    private GoogleApiClient mGoogleApiClient;
    private PowerManager.WakeLock mWakeLock;
    private LocationRequest mLocationRequest;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;

    private Boolean servicesAvailable = false;
    private LocationChangeReciever changeReciever;

    public class LocalBinder extends Binder {
        public BackgroundLocationService getServerInstance() {
            return BackgroundLocationService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();


        mInProgress = false;
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        //mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(Constants.UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(Constants.FASTEST_INTERVAL);

        servicesAvailable = servicesConnected();

        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        setUpLocationClientIfNeeded();
        registerLocationReciever();

    }

    private void registerLocationReciever() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.interval.update");
        changeReciever = new LocationChangeReciever();
        getApplicationContext().registerReceiver(changeReciever,intentFilter);
    }

    /*
     * Create a new location client, using the enclosing class to
     * handle callbacks.
     */
    protected synchronized void buildGoogleApiClient() {
        this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {

            return false;
        }
    }

    public int onStartCommand (Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        PowerManager mgr = (PowerManager)getSystemService(Context.POWER_SERVICE);

        /*
        WakeLock is reference counted so we don't want to create multiple WakeLocks. So do a check before initializing and acquiring.

        This will fix the "java.lang.Exception: WakeLock finalized while still held: MyWakeLock" error that you may find.
        */
        if (this.mWakeLock == null) { //**Added this
            this.mWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        }

        if (!this.mWakeLock.isHeld()) { //**Added this
            this.mWakeLock.acquire();
        }

        if(!servicesAvailable || mGoogleApiClient.isConnected() || mInProgress)
            return START_STICKY;

        setUpLocationClientIfNeeded();
        if(!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting() && !mInProgress) {
            appendLog(dateFormat.format(new Date()) + ": Started");
            mInProgress = true;
            mGoogleApiClient.connect();
        }
        Log.d("debug", "service started");
        return START_STICKY;
    }


    private void setUpLocationClientIfNeeded() {
        if(mGoogleApiClient == null)
            buildGoogleApiClient();
    }

    // Define the callback method that receives location updates
    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String msg = Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude()) + " -------- "+mLocationRequest.getFastestInterval()+"  -----upda: "+mLocationRequest.getInterval();
        Log.d("debug", msg);
        // Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        appendLog(dateFormat.format(new Date()) + " : " + msg);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String fileName = "logs.txt";
    public void appendLog(String text) {
//
//        File logsDir = FileUtils.geLogsDirectory();
//        File logFile = new File(logsDir, fileName);
//        if (!logFile.exists()) {
//            try {
//                logFile.createNewFile();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//        try {
//            //BufferedWriter for performance, true to set append to file flag
//            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
//            buf.append(text);
//            buf.newLine();
//            buf.close();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }

    @Override
    public void onDestroy() {
        // Turn off the request flag
        this.mInProgress = false;

        if (this.servicesAvailable && this.mGoogleApiClient != null) {
            this.mGoogleApiClient.unregisterConnectionCallbacks(this);
            this.mGoogleApiClient.unregisterConnectionFailedListener(this);
            this.mGoogleApiClient.disconnect();
            // Destroy the current location client
            this.mGoogleApiClient = null;
        }
        // Display the connection status
        // Toast.makeText(this, DateFormat.getDateTimeInstance().format(new Date()) + ":
        // Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();

        if (this.mWakeLock != null) {
            this.mWakeLock.release();
            this.mWakeLock = null;
        }

        Log.i("service","disconnected");
        super.onDestroy();
    }
    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {

        try {

            // Check if the permissions are already granted or not
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                //Here we have to check for the read and write external storage permissions
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // Request location updates using static settings
                    Intent intent = new Intent(this, LocationReceiver.class);
                    PendingIntent locationIntent = PendingIntent.getBroadcast(getApplicationContext(), 14872, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient,
                            mLocationRequest, locationIntent); // This is the changed line.

                    /*LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient,
                            mLocationRequest, this);*/
                    appendLog(dateFormat.format(new Date()) + ": Connected");
                }
            }
        } catch (Exception ex) {

        }
    }

    /*
 * Called by Location Services if the connection to the
 * location client drops because of an error.
 */
    @Override
    public void onConnectionSuspended(int i) {
        // Turn off the request flag
        mInProgress = false;
        // Destroy the current location client
        mGoogleApiClient = null;
        // Display the connection status
        // Toast.makeText(this, DateFormat.getDateTimeInstance().format(new Date()) + ": Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        appendLog(dateFormat.format(new Date()) + ": Disconnected");
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mInProgress = false;

        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            // If no resolution is available, display an error dialog
        } else {

        }
    }

    public class LocationChangeReciever extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent!=null && intent.getExtras()!=null){
                int priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
                if(intent.getExtras().containsKey("accuracy")){
                    priority = intent.getExtras().getInt("accuracy");
                }
                if(mLocationRequest==null) {
                    mLocationRequest = LocationRequest.create();
                }
                mLocationRequest.setPriority(priority);
                if(priority == LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY){
                    mLocationRequest.setInterval(60*1000);
                    // Set the fastest update interval to 1 second
                    mLocationRequest.setFastestInterval(60*1000);
                }else if(priority == LocationRequest.PRIORITY_HIGH_ACCURACY){
                    mLocationRequest.setInterval(Constants.UPDATE_INTERVAL);
                    // Set the fastest update interval to 1 second
                    mLocationRequest.setFastestInterval(Constants.FASTEST_INTERVAL);
                }
                Intent locintent = new Intent(BackgroundLocationService.this, LocationReceiver.class);
                PendingIntent locationIntent = PendingIntent.getBroadcast(getApplicationContext(), 14872, locintent, PendingIntent.FLAG_CANCEL_CURRENT);
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                        mLocationRequest, locationIntent);
            }
        }
    }
}