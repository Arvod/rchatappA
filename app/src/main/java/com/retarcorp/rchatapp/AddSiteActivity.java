package com.retarcorp.rchatapp;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.retarcorp.rchatapp.Model.Site;
import com.retarcorp.rchatapp.Net.ConnectivityCallback;
import com.retarcorp.rchatapp.Net.ConnectivityTask;
import com.retarcorp.rchatapp.Net.SiteIconDownloadTask;

public class AddSiteActivity extends Activity implements ConnectivityCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_site);
        setTitle("Добавить сайт");
        findViewById(R.id.addsitebtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddSite();
            }
        });
    }

    private Snackbar snackbar;
    public void onAddSite(){
        String domain = ((EditText)findViewById(R.id.domain_name)).getText().toString();
        String key = ((EditText)findViewById(R.id.key)).getText().toString();

        Spinner spinner = (Spinner)findViewById(R.id.protocol);
        String[] protocols = getResources().getStringArray(R.array.protocols);
        String protocol = protocols[spinner.getSelectedItemPosition()];

        if(Site.isSiteAlreadyExists(protocol,domain)){
            Snackbar.make(findViewById(R.id.addsitebtn),"Невозможно добавить сайт "+protocol+"://"+domain+"! Сайт уже существует.",Snackbar.LENGTH_LONG).show();
            return;
        }
        snackbar = Snackbar.make(findViewById(R.id.addsitebtn),"Осуществляем прозвон...",Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
        checkConnectivity(protocol, domain, key);
    }

    public void checkConnectivity(String protocol, String domain, String key){
        (new ConnectivityTask(this)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,protocol, domain, key);
    }

    public void onConnectivityChecked(String protocol,String domain, String key, boolean result){
        snackbar.dismiss();
        if(!result){
            Snackbar.make(findViewById(
                    R.id.addsitebtn)
                    ,"Не удалось осуществить прозвон! Ключ соединения неверен или система RChat на сайте недоступна."
                    ,Snackbar.LENGTH_LONG).show();
            return;
        }
        try {
            Site site = Site.createSite(protocol, domain, key);
            new SiteIconDownloadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, site);
        }catch (Site.SiteAlreadyExistsException e){
            Snackbar.make(findViewById(R.id.addsitebtn),"Невозможно добавить сайт "+protocol+"://"+domain+"! Сайт уже существует.",Snackbar.LENGTH_LONG).show();
        }catch (Exception e) {
            Snackbar.make(findViewById(R.id.addsitebtn), "Unable to create site " + protocol + "://" + domain + " with key " + key + ".", Snackbar.LENGTH_LONG).show();
            e.printStackTrace();
        }
        this.onSiteAddedSuccessfully();
    }

    public void onSiteAddedSuccessfully(){
        Snackbar.make(findViewById(R.id.addsitebtn),"Сайт успешно добавлен!",Snackbar.LENGTH_LONG).show();
    }
}
