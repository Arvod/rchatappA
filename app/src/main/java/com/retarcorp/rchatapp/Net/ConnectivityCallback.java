package com.retarcorp.rchatapp.Net;

/**
 * Created by CaptainOsmant on 10.01.2018.
 */

public interface ConnectivityCallback {
    public void onConnectivityChecked(String protocol, String domain, String key, boolean result);
}
