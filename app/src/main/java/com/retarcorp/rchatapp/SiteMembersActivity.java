package com.retarcorp.rchatapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.retarcorp.rchatapp.Model.Member;
import com.retarcorp.rchatapp.Model.Site;
import com.retarcorp.rchatapp.Net.ConnectivityCallback;
import com.retarcorp.rchatapp.Net.ConnectivityTask;
import com.retarcorp.rchatapp.Net.GrabMembersTask;
import com.retarcorp.rchatapp.Net.MemberGrabCallback;
import com.retarcorp.rchatapp.Net.MembersWatchTask;
import com.retarcorp.rchatapp.UI.MemberAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class SiteMembersActivity extends AppCompatActivity implements MemberGrabCallback, ConnectivityCallback{

    private static boolean isInBackground;
    private Site currentSite = null;
//    DBMembers dbHelper;
//    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_members);

        int siteId = getIntent().getIntExtra("site_id",0);
        if(siteId == 0){
            if(Global.CurrentSite == null) {
                Snackbar.make(findViewById(R.id.members_layout), "Ошибка получения сайта", Snackbar.LENGTH_LONG).show();
            }else{
                siteId = Global.CurrentSite.getId();
            }
        }
        try{
            currentSite = new Site(siteId);
            Global.CurrentSite = currentSite;
            setTitle(currentSite.getTitle() + " - диалоги");
        }catch(Site.SiteNotFoundException e){
            Snackbar.make(findViewById(R.id.members_layout),"Ошибка получения сайта",Snackbar.LENGTH_LONG).show();
            return;
        }

        adapter = new MemberAdapter(this,members);
        ((ListView)findViewById(R.id.members_list)).setAdapter(adapter);
        checkConnectivity();



    }

    private Snackbar connectivitySnackbar = null;
    private void checkConnectivity() {
        (new ConnectivityTask(this)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, currentSite.protocol, currentSite.domain, currentSite.token);
        this.connectivitySnackbar = Snackbar.make(findViewById(R.id.members_layout),"Соединение...",Snackbar.LENGTH_INDEFINITE);
        this.connectivitySnackbar.show();
    }

    private Snackbar siteMembersSnackbar = null;
    public void getSiteMembers(){
        this.siteMembersSnackbar = Snackbar.make(findViewById(R.id.members_layout),"Загрузка посетителей...",Snackbar.LENGTH_INDEFINITE);
        this.siteMembersSnackbar.show();

        (new GrabMembersTask(this)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, currentSite);


    }

    private void launchMembersWatch(){
        task = new MembersWatchTask(this,3000);
        task.execute(Global.CurrentSite);
    }

    private MembersWatchTask task;

    @Override
    public void onResume(){
        super.onResume();
        isInBackground = false;
        Global.CurrentMember = null;
        launchMembersWatch();

    }


    @Override
    public void onPause(){
        super.onPause();
        isInBackground = true;
        task.cancel(true);
    }

    private ArrayList<Member> members = new ArrayList<>();
    private MemberAdapter adapter = null;

    @Override
    public void onMembersJSONGrabbed(String json) {
        if(siteMembersSnackbar != null) {
            siteMembersSnackbar.dismiss();
        }
        //Toast.makeText(this,json,Toast.LENGTH_SHORT).show();

        try {
            JSONObject jsonObj = new JSONObject(json);
            String status = jsonObj.getString("status");

            if(status.equals("OK")){
                //Toast.makeText(this,"Ура!",Toast.LENGTH_SHORT).show();

                members = new ArrayList<>();
                JSONArray array = jsonObj.getJSONArray("data");
                int len = array.length();
                for(int i = 0; i< len; i++){
                    JSONObject obj = array.getJSONObject(i);
                    Member m = new Member(obj.getInt("id"));
                    m.last_city = obj.getString("last_city");
                    m.last_ip = obj.getString("last_ip");
                    m.last_message = obj.getString("last_message");
                    m.pagehref = obj.getString("pagehref");
                    m.ssid = obj.getString("ssid");
                    m.messages = obj.getInt("messages");
                    m.unread = obj.getInt("unread");
                    members.add(m);
                }
                adapter.clear();
                adapter.addAll(members);

            }else{
                if(status.equals("ERROR")){
                    String message = jsonObj.getString("message");
                    longSnack("Ошибка: "+message);
                }else{
                    longSnack("Не удалось расшифровать данные с сервера сайта!!");
                }
            }
        }catch (Exception e) {
            longSnack("Не удалось расшифровать данные с сервера сайта!");
            e.printStackTrace();
        }
    }


    public void longSnack(String msg){
        Snackbar.make(findViewById(R.id.members_layout),msg,Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onConnectivityChecked(String protocol, String domain, String key, boolean result) {
        connectivitySnackbar.dismiss();
        if(result) {
            getSiteMembers();

        }else{
            Snackbar.make(findViewById(R.id.members_layout),"Не удалось подключиться к сайту!",Snackbar.LENGTH_LONG).show();
        }
    }

    public static boolean isIsInBackground() {
        return isInBackground;
    }
}
