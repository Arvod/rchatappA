package com.retarcorp.rchatapp.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.retarcorp.rchatapp.Global;

public class Network {
    public static boolean isConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) Global.Ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
