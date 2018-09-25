package com.retarcorp.rchatapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.retarcorp.rchatapp.Model.DBMembers;
import com.retarcorp.rchatapp.Model.Member;
import com.retarcorp.rchatapp.Model.Site;
import com.retarcorp.rchatapp.Net.MemberGrabCallback;
import com.retarcorp.rchatapp.Net.MembersWatchTask;
import com.retarcorp.rchatapp.UI.MemberAdapter;

import java.util.ArrayList;

public class SiteMembersActivity extends AppCompatActivity implements MemberGrabCallback {

    private static boolean isInBackground;

    DBMembers dbHelper = new DBMembers(Global.Ctx);
    SQLiteDatabase db;

    private ArrayList<Member> members = new ArrayList<>();
    private MemberAdapter adapter = null;
    private MembersWatchTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_members);
        int siteId = getIntent().getIntExtra("site_id", 0);
        if (siteId == 0) {
            siteId = Global.CurrentSite.getId();
        }
        Site currentSite = new Site(siteId);
            Global.CurrentSite = currentSite;
            setTitle(currentSite.getTitle() + " - диалоги");
        adapter = new MemberAdapter(this, members);
        onCreateHashMembers();
        if (!isOnline()) {
            Snackbar membersSnackbarHash = Snackbar.make(findViewById(R.id.members_layout), "Нет подключения к интернету", Snackbar.LENGTH_LONG);
            membersSnackbarHash.show();
        }
        ((ListView) findViewById(R.id.members_list)).setAdapter(adapter);
    }


    private void launchMembersWatch() {
//        startService(new Intent(this, RefreshMessages.class));
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

    @Override
    public void onMembersJSONGrabbed(String json) {
        dbHelper = new DBMembers(Global.Ctx);
        try {
            addMembers();
            dbHelper.close();
            adapter.clear();
            adapter.addAll(members);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isIsInBackground() {
        return isInBackground;
    }

    public void onCreateHashMembers() {
        dbHelper = new DBMembers(Global.Ctx);
        if (!dbHelper.isEmpty("members")) {
            db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("select * from " + "members", null);
            cursor.moveToFirst();
            members = new ArrayList<>();
            for (int i = 0; i < cursor.getCount(); i++) {
                Member m = new Member(cursor.getInt(0));
                m.last_city = cursor.getString(3);
                m.last_ip = cursor.getString(4);
                m.last_message = cursor.getString(7);
                m.pagehref = cursor.getString(5);
                m.ssid = cursor.getString(1);
                m.messages = cursor.getInt(8);
                m.unread = cursor.getInt(6);
                members.add(m);
                cursor.moveToNext();
            }
            cursor.close();
            db.close();
            dbHelper.close();
            adapter.clear();
            adapter.addAll(members);
        }
    }

    public void addMembers() {
        dbHelper = new DBMembers(Global.Ctx);
        members = new ArrayList<>();
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + "members", null);
        cursor.moveToFirst();
        members = new ArrayList<>();
        for (int i = 0; i < cursor.getCount(); i++) {
            Member m = new Member(cursor.getInt(0));
            m.last_city = cursor.getString(3);
            m.last_ip = cursor.getString(4);
            m.last_message = cursor.getString(7);
            m.pagehref = cursor.getString(5);
            m.ssid = cursor.getString(1);
            m.messages = cursor.getInt(8);
            m.unread = cursor.getInt(6);
            members.add(m);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        dbHelper.close();
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
