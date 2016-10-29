package com.rcumis.vigilance.vicpl.network;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.rcumis.vigilance.vicpl.utils.VigilancePreferenceManager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by Arjun on 10/29/2016.
 * Api class helper
 */

public class HttpClientHelper {

    private final Context mContext;

    public HttpClientHelper(Context context) {
        this.mContext = context;
    }

    public void sendLocationToServer(Location location) {
        OkHttpClient client = new OkHttpClient();
        String url = getFormedUrl(location);
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client.newBuilder().addInterceptor(interceptor);
        Log.i("request urlis",url);
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
            }
        });
    }

    public String getFormedUrl(Location location) {
        String url = "https://vigilance.rcumis.com/location/api?email="
                + VigilancePreferenceManager.getEmailOfUser(mContext)
                +"&lat="+location.getLatitude()
                +"&long="+location.getLongitude()
                +"&time="+getTime();
        return url;
    }

    public String getTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss_dd/MMM/yyyy");
        return format.format(new Date(System.currentTimeMillis()));
    }
}
