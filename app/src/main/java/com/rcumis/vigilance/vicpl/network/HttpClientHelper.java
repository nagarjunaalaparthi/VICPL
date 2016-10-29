package com.rcumis.vigilance.vicpl.network;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.rcumis.vigilance.vicpl.utils.VigilancePreferenceManager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
                Log.i("response string",response.toString());
            }
        });
    }

    private String getFormedUrl(Location location) {
        return "https://vigilance.rcumis.com/location/api?email="
                + VigilancePreferenceManager.getEmailOfUser(mContext)
                +"&lat="+location.getLatitude()
                +"&long="+location.getLongitude()
                +"&time="+getTime();
    }

    private String getTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        return format.format(new Date(System.currentTimeMillis()));
    }
}
