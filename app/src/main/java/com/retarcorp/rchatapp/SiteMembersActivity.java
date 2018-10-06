package com.retarcorp.rchatapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.retarcorp.rchatapp.Model.DBConnector;
import com.retarcorp.rchatapp.Model.Site;
import com.retarcorp.rchatapp.Net.MemberGrabCallback;
import com.retarcorp.rchatapp.Net.MembersWatchTask;
import com.retarcorp.rchatapp.UI.MemberAdapter;

public class SiteMembersActivity extends AppCompatActivity implements MemberGrabCallback {

    private static boolean isInBackground;

    private MemberAdapter adapter = null;
    private MembersWatchTask task;
    private DBConnector dbConnector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_members);
        int siteId = getIntent().getIntExtra("site_id", 0);
        if (siteId == 0) {
            siteId = Global.CurrentSite.getId();
        }
        dbConnector = new DBConnector();
        Site currentSite = new Site(siteId);
        Global.CurrentSite = currentSite;
        setTitle(currentSite.getTitle() + " - диалоги");
        adapter = new MemberAdapter(this, dbConnector.getMembers());
        if (!isOnline()) {
            Snackbar membersSnackbarHash = Snackbar.make(findViewById(R.id.members_layout), "Нет подключения к интернету", Snackbar.LENGTH_LONG);
            membersSnackbarHash.show();
        }
        ((ListView) findViewById(R.id.members_list)).setAdapter(adapter);
    }


    private void launchMembersWatch() {
        task = new MembersWatchTask(this, 1000);
        task.execute(Global.CurrentSite);
    }

    @Override
    public void onResume() {
        super.onResume();
        isInBackground = false;
        Global.CurrentMember = null;
        launchMembersWatch();
    }

    @Override
    public void onPause() {
        super.onPause();
        isInBackground = true;
        task.cancel(true);
    }

    public static boolean isIsInBackground() {
        return isInBackground;
    }

    @Override
    public void onMembersJSONGrabbed(String json) {
        try {
            adapter.clear();
            adapter.addAll(dbConnector.getMembers());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
