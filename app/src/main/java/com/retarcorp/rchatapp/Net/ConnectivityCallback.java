package com.retarcorp.rchatapp.Net;

public interface ConnectivityCallback {
    void onConnectivityChecked(String protocol, String domain, String key, boolean result);
}
