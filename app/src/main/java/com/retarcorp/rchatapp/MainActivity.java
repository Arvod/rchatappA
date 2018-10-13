package com.retarcorp.rchatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.retarcorp.rchatapp.Model.Site;
import com.retarcorp.rchatapp.Services.BasicWidgetProvider;
import com.retarcorp.rchatapp.Services.RefreshService;
import com.retarcorp.rchatapp.UI.SiteAdapter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Global.Ctx = getApplicationContext();
        setContentView(R.layout.activity_main);
        setTitle(getResources().getString(R.string.my_site));
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddSiteActivity.class);
                startActivity(intent);
                Intent refreshIntent = new Intent(getApplicationContext(), BasicWidgetProvider.class);
                sendBroadcast(refreshIntent);
            }
        });

    }

    @Override
    public void onResume(){
        super.onResume();
        ListView listView = (ListView)findViewById(R.id.sites);
        SiteAdapter adapter = new SiteAdapter(this, Site.getSites());
        Global.CurrentSite = null;
        Global.CurrentMember = null;
        listView.setAdapter(adapter);
        if (Site.getSites().size() != 0) {
            startService(new Intent(MainActivity.this, RefreshService.class));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
