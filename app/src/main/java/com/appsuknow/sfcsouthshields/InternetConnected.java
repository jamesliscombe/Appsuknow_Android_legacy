package com.appsuknow.sfcsouthshields;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InternetConnected {

    public boolean InternetConnectedCheck(final Activity activity) {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);


        NetworkInfo activeNetwork = null;

        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }


        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}
