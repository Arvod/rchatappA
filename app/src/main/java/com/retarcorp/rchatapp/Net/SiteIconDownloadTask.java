package com.retarcorp.rchatapp.Net;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;

import com.retarcorp.rchatapp.Model.Site;
import com.retarcorp.rchatapp.Utils.Downloader;

import java.io.ByteArrayOutputStream;

/**
 * Created by CaptainOsmant on 13.01.2018.
 */

public class SiteIconDownloadTask extends AsyncTask<Site,Void,Site> {

    public SiteIconDownloadTask(){

    }

    @Override
    protected Site doInBackground(Site... params) {
        Site site = params[0];
        try {
            Bitmap bmp = Downloader.downloadBitmap(site.api.getIconURL());
            ByteArrayOutputStream os=  new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
            String icon = Base64.encodeToString(os.toByteArray(), Base64.DEFAULT);
            site.icon = icon;

        }catch (Exception e){
            e.printStackTrace();
        }
        return site;
    }

    @Override
    public void onPostExecute(Site result){
        result.update();
    }
}
