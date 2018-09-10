package com.retarcorp.rchatapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.retarcorp.rchatapp.Model.ChatMessage;
import com.retarcorp.rchatapp.Model.Member;
import com.retarcorp.rchatapp.Net.GrabMessagesTask;
import com.retarcorp.rchatapp.Net.MemberInfoReceiveTask;
import com.retarcorp.rchatapp.Net.MemberReceiveCallback;
import com.retarcorp.rchatapp.Net.MessageSentCallback;
import com.retarcorp.rchatapp.Net.MessagesGrabCallback;
import com.retarcorp.rchatapp.Net.MessagesWatchTask;
import com.retarcorp.rchatapp.Net.SendMessageTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class ChatActivity extends AppCompatActivity implements MessagesGrabCallback, MessageSentCallback, MemberReceiveCallback {

    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setTitle("Диалог на "+Global.CurrentSite.getTitle());

        id = getIntent().getIntExtra("member_id", 0);
        if(id==0) {
            longSnack("Не удалось получить данные пользователя!");
        }else{
            Global.CurrentMember = new Member(id);
        }


        findViewById(R.id.send_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendMessage();
            }
        });

        ((EditText)findViewById(R.id.message_textbox)).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                scrollMessageList();
            }
        });

    }


    @Override
    public void onPause(){
        super.onPause();
        //Toast.makeText(Global.Ctx,"Pause!",Toast.LENGTH_SHORT).show();
        task.cancel(true);
    }


    @Override
    public void onResume(){
        super.onResume();
        launchMessagesWatch();
    }

    private MessagesWatchTask task;
    public void launchMessagesWatch(){
        loadMessages();
        task = new MessagesWatchTask(this, Global.CurrentSite, 3000);
        task.execute(Global.CurrentMember);
    }

    public void onSendMessage(){
        String text = ((EditText)findViewById(R.id.message_textbox)).getText().toString();
        ((EditText)findViewById(R.id.message_textbox)).setEnabled(false);
        (new SendMessageTask(Global.CurrentSite, Global.CurrentMember, this)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, text);
    }

    public void loadMessages(){
        (new GrabMessagesTask(this,Global.CurrentSite)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Global.CurrentMember);
    }

    private ArrayList<ChatMessage> messages = new ArrayList<>();


    public void longSnack(String message){
        Snackbar.make(findViewById(R.id.chat_layout),message,Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onMessagesGrabbed(String json) {
        //Toast.makeText(Global.Ctx,"Messages grabbed!",Toast.LENGTH_SHORT).show();
        try {
            JSONObject jsonObj = new JSONObject(json);
            String status = jsonObj.getString("status");
            if(status.trim().equals("OK")){


                JSONArray array = jsonObj.getJSONArray("data");

                int len = array.length();

                if(len == messages.size()){

                    // Check if nothing changed
                    JSONObject firstMessage = array.getJSONObject(0);
                    String text = firstMessage.getString("text");
                    if(messages.get(messages.size()-1).text.equals(text)){
                        return;
                    }
                }
                messages.clear();

                for(int i = 0; i < len; i++){
                    JSONObject obj = array.getJSONObject(i);
                    String text = obj.getString("text");
                    Date created = (new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")).parse(obj.getString("created"));
                    ChatMessage.Direction direction = obj.getInt("direction")==1 ? ChatMessage.Direction.MEMBER : ChatMessage.Direction.ADMIN ;
                    ChatMessage m = new ChatMessage(text, created, direction);
                    messages.add(m);
                }

                Collections.reverse(messages);
                rebuildMessageList(messages);

            }else{
                if(status.trim().equals("ERROR")) {
                    String message = jsonObj.getString("message");
                    this.longSnack(message);
                }else{
                    throw new Exception("Invalid status");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.longSnack("Не удалось расшифровать данные с сервера!");
        }
    }

    private void rebuildMessageList(ArrayList<ChatMessage> messages) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.messages_container);
        layout.removeAllViews();

        for(ChatMessage m : messages){

            View view = LayoutInflater.from(this).inflate(R.layout.message_layout, null);
            ((TextView)view.findViewById(R.id.message_text)).setText(m.text);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            if(m.direction == ChatMessage.Direction.ADMIN){
                lp.gravity = Gravity.RIGHT;
                view.findViewById(R.id.message_text).setBackgroundColor(Color.parseColor("#1e824c"));
                ((TextView)view.findViewById(R.id.message_text)).setTextColor(Color.parseColor("#ffffff"));

            }else{
                view.findViewById(R.id.message_text).setBackgroundColor(Color.parseColor("#ccccff"));
                lp.gravity = Gravity.LEFT;
            }

            view.findViewById(R.id.message_layer).setLayoutParams(lp);
            //view.findViewById(R.id.message_text).setBackgroundColor(Color.parseColor("#"));


            layout.addView(view);
        }
        scrollMessageList();

    }

    private void scrollMessageList() {
        final ScrollView sw = (ScrollView)findViewById(R.id.messages_scrollview);

        sw.postDelayed(new Runnable() {
            @Override
            public void run() {
                sw.fullScroll(ScrollView.FOCUS_DOWN);
            }
        },200);
    }


    @Override
    public void onMessageSent(String json) {
        EditText  et = ((EditText)findViewById(R.id.message_textbox));
        et.setEnabled(true);
        et.setText("");
        loadMessages();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat, menu);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.menu_properties){
            (new MemberInfoReceiveTask(Global.CurrentSite,this)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,Global.CurrentMember);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(!SiteMembersActivity.isIsInBackground()) {
            Intent intent = new Intent(Global.Ctx, SiteMembersActivity.class);
            intent.putExtra("site_id", Global.CurrentSite.getId());
            startActivityForResult(intent, 0);
        }
        this.finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void showMemberInfo(String pagehref, String lastonline, String last_ip, String last_city){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = LayoutInflater.from(this).inflate(R.layout.member_properties, null);
        ((TextView)v.findViewById(R.id.member_props_pagehref)).setText(pagehref);
        ((TextView)v.findViewById(R.id.member_props_last_city)).setText(last_city);
        ((TextView)v.findViewById(R.id.member_props_last_ip)).setText(last_ip);
        ((TextView)v.findViewById(R.id.member_props_last_online)).setText(lastonline);

        v.findViewById(R.id.member_props_pagehref).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String href = (String)( (TextView)v).getText();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(href));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);

            }
        });

        builder.setView(v);
        builder.setPositiveButton("Закрыть", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onMemberInfoReceived(String json) {
        try{
            JSONObject jsonObject = new JSONObject(json);
            String status = jsonObject.getString("status");
            if(status.equals("ERROR")){

                String message = jsonObject.getString("message");
                longSnack("Ошибка: "+message);
            }else{
                if(status.equals("OK")){
                    JSONObject member = jsonObject.getJSONObject("data");
                    String pagehref = member.getString("pagehref");
                    String lastonline = member.getString("lastonline");
                    String last_ip = member.getString("last_ip");
                    String last_city = member.getString("last_city");
                    showMemberInfo(pagehref, lastonline, last_ip, last_city);
                }else{
                    throw new JSONException("Malformed json format");
                }
            }
        }catch (Exception e){
            longSnack("Не удалось получить данные о пользователе!");
        }
    }
}
