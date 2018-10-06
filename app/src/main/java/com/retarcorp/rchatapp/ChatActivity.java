package com.retarcorp.rchatapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.retarcorp.rchatapp.Model.DBConnector;
import com.retarcorp.rchatapp.Model.Member;
import com.retarcorp.rchatapp.Net.GrabMessagesTask;
import com.retarcorp.rchatapp.Net.MessageSentCallback;
import com.retarcorp.rchatapp.Net.MessagesGrabCallback;
import com.retarcorp.rchatapp.Net.MessagesWatchTask;
import com.retarcorp.rchatapp.Net.SendMessageTask;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity implements MessagesGrabCallback, MessageSentCallback {

    private int id;
    private ArrayList<ChatMessage> messages = new ArrayList<>();
    private DBConnector dbConnector;

    private MessagesWatchTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        id = getIntent().getIntExtra("member_id", 0);
        if (Global.CurrentSite != null) {
            setTitle("Диалог на " + Global.CurrentSite.getTitle());
        }
        Global.CurrentMember = new Member(id);
        dbConnector = new DBConnector();
        messages = dbConnector.getMessages(id);
        rebuildMessageList(messages);
        dbConnector.setMessagesRead(id);

        findViewById(R.id.send_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendMessage();
            }
        });
        findViewById(R.id.message_textbox).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                scrollMessageList();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        task.cancel(true);
    }


    @Override
    public void onResume() {
        super.onResume();
        launchMessagesWatch();
    }

    public void launchMessagesWatch() {
        loadMessages();
        task = new MessagesWatchTask(this, Global.CurrentSite, 1000);
        task.execute(Global.CurrentMember);
    }

    public void onSendMessage() {
        String text = ((EditText) findViewById(R.id.message_textbox)).getText().toString();
        findViewById(R.id.message_textbox).setEnabled(false);
        (new SendMessageTask(Global.CurrentSite, Global.CurrentMember, this)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, text);
    }

    public void loadMessages() {
        (new GrabMessagesTask(this, Global.CurrentSite)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Global.CurrentMember);
    }

    @Override
    public void onMessagesGrabbed(String json) {
        ArrayList<ChatMessage> chatMessages = dbConnector.getMessages(id);
        if (messages.size() != chatMessages.size()) {
            messages = chatMessages;
            rebuildMessageList(messages);
            scrollMessageList();
        }
    }

    private void rebuildMessageList(ArrayList<ChatMessage> messages) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.messages_container);
        layout.removeAllViews();
        for (ChatMessage m : messages) {
            View view = LayoutInflater.from(this).inflate(R.layout.message_layout, null);
            ((TextView) view.findViewById(R.id.message_text)).setText(m.text);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (m.direction == ChatMessage.Direction.ADMIN) {
                lp.gravity = Gravity.RIGHT;
                view.findViewById(R.id.message_text).setBackgroundColor(Color.parseColor("#1e824c"));
                ((TextView) view.findViewById(R.id.message_text)).setTextColor(Color.parseColor("#ffffff"));
            } else {
                view.findViewById(R.id.message_text).setBackgroundColor(Color.parseColor("#ccccff"));
                lp.gravity = Gravity.LEFT;
            }
            view.findViewById(R.id.message_layer).setLayoutParams(lp);
            layout.addView(view);
        }
    }

    private void scrollMessageList() {
        final ScrollView sw = (ScrollView) findViewById(R.id.messages_scrollview);
        sw.postDelayed(new Runnable() {
            @Override
            public void run() {
                sw.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }, 200);
    }


    @Override
    public void onMessageSent(String json) {
        EditText et = ((EditText) findViewById(R.id.message_textbox));
        et.setEnabled(true);
        et.setText("");
        loadMessages();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_properties:
                Member member = dbConnector.getMember(id);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View v = LayoutInflater.from(this).inflate(R.layout.member_properties, null);
                ((TextView) v.findViewById(R.id.member_props_pagehref)).setText(member.pagehref);
                ((TextView) v.findViewById(R.id.member_props_last_city)).setText(member.last_city);
                ((TextView) v.findViewById(R.id.member_props_last_ip)).setText(member.last_ip);
                ((TextView) v.findViewById(R.id.member_props_last_online)).setText(member.messages);

                v.findViewById(R.id.member_props_pagehref).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String href = (String) ((TextView) v).getText();
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!SiteMembersActivity.isIsInBackground()) {
            Intent intent = new Intent(Global.Ctx, SiteMembersActivity.class);
            intent.putExtra("site_id", Global.CurrentSite.getId());
            startActivity(intent);
        }
        this.finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

