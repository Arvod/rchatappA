package com.retarcorp.rchatapp.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Downloader {

    public static String download(String href) throws Exception{
        URL url = new URL(href);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.connect();
        String s = "";
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        while((s=reader.readLine())!=null){
            builder.append(s);
        }
        return builder.toString();
    }

    public static Bitmap downloadBitmap(String href) throws Exception{
        URL url = new URL(href);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.connect();

        return BitmapFactory.decodeStream(connection.getInputStream());

    }
}
