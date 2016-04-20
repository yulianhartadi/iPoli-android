package io.ipoli.android.app.utils;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/16/16.
 */
public class NetworkConnectivityUtils {
    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}