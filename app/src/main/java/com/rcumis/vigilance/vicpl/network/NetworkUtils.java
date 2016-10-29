package com.rcumis.vigilance.vicpl.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Arjun on 10/28/2016.
 */

public class NetworkUtils {

    public static boolean isNetworkAvailable(Context mContext){
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if(activeNetwork!=null){
            return activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
        }else{
            return false;
        }
    }

}
