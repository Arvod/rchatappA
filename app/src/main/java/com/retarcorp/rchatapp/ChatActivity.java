package com.retarcorp.rchatapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.retarcorp.rchatapp.Model.DBMembers;
import com.retarcorp.rchatapp.Model.Member;
import com.retarcorp.rchatapp.Net.GrabMessagesTask;
import com.retarcorp.rchatapp.Net.MessageSentCallback;
import com.retarcorp.rchatapp.Net.MessagesGrabCallback;
import com.retarcorp.rchatapp.Net.MessagesWatchTask;
import com.retarcorp.rchatapp.Net.SendMessageTask;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity implements MessagesGrabCallback, MessageSentCallback {

    private int id;
    private ArrayList<ChatMessage> messages = new ArrayList<>();
    private DBMembers dbHelper;
    private SQLiteDatabase db;

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
        dbHelper = new DBMembers(Global.Ctx);
        onCreateHashMessages();

        db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("unread", 0);

        db.update("members", cv, "id" + " = ?", new String[]{String.valueOf(id)});
        db.close();

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
        try {
            db = dbHelper.getReadableDatabase();
            Cursor mCursor = db.query("messages", new String[]{"uid"}, "uid" + " = ?", new String[]{String.valueOf(id)}, null, null, null);
            if (messages.size() != mCursor.getCount()) {
                onCreateHashMessages();
                scrollMessageList();
            }
            mCursor.close();
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
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
                db = dbHelper.getWritableDatabase();
                Cursor mCursor = db.query("members", new String[]{"pagehref ", "last_city", "last_ip", "lastonline"}, "id" + " = ?", new String[]{String.valueOf(id)}, null, null, null);
                mCursor.moveToFirst();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View v = LayoutInflater.from(this).inflate(R.layout.member_properties, null);
                ((TextView) v.findViewById(R.id.member_props_pagehref)).setText(mCursor.getString(0));
                ((TextView) v.findViewById(R.id.member_props_last_city)).setText(mCursor.getString(1));
                ((TextView) v.findViewById(R.id.member_props_last_ip)).setText(mCursor.getString(2));
                ((TextView) v.findViewById(R.id.member_props_last_online)).setText(mCursor.getString(3));

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

    public void onCreateHashMessages() {
        messages = new ArrayList<>();
        db = dbHelper.getReadableDatabase();
        Cursor mCursor = db.query("messages", null, "uid" + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        mCursor.moveToFirst();
        if (mCursor.getCount() != 0) {
            for (int i = 0; i < mCursor.getCount(); i++) {
                ChatMessage.Direction direction = mCursor.getInt(1) == 1 ? ChatMessage.Direction.MEMBER : ChatMessage.Direction.ADMIN;
                Date created = new Date();
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
                try {
                    created = format.parse(mCursor.getString(2));
                } catch (ParseException e) {
                    e.printStackTrace();
                    created = null;
                }
                ChatMessage m = new ChatMessage(mCursor.getString(3), created, direction);
                messages.add(m);
                mCursor.moveToNext();
            }
        }
        mCursor.close();
        rebuildMessageList(messages);
    }
}

