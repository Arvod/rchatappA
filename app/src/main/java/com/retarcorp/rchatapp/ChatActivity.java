package com.retarcorp.rchatapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import com.retarcorp.rchatapp.Utils.DataWorker;

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
            setTitle(getResources().getString(R.string.dialog_on) + Global.CurrentSite.getTitle());
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
            final View viewDate = LayoutInflater.from(this).inflate(R.layout.date_text, null);
            TextView message_text = ((TextView) view.findViewById(R.id.message_text));
            message_text.setText(m.text);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            final TextView dateText = ((TextView) viewDate.findViewById(R.id.message_date));
            dateText.setText(DataWorker.getDifferenceBtwTime(m.created));
            if (m.direction == ChatMessage.Direction.ADMIN) {
                lp.gravity = Gravity.RIGHT;
                message_text.setBackgroundColor(Color.parseColor("#1e824c"));
                message_text.setTextColor(Color.parseColor("#ffffff"));
                dateText.setGravity(Gravity.RIGHT);
            } else {
                message_text.setBackgroundColor(Color.parseColor("#ccccff"));
                message_text.setTextColor(Color.parseColor("#000000"));
                lp.gravity = Gravity.LEFT;
            }
            message_text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (dateText.getVisibility()) {
                        case View.GONE:
                            showElements(dateText);
                            break;
                        case View.VISIBLE:
                            hideElements(dateText);
                            break;
                    }
                }
            });
            view.findViewById(R.id.message_layer).setLayoutParams(lp);
            viewDate.findViewById(R.id.message_layer).setLayoutParams(lp);
            layout.addView(view);
            layout.addView(viewDate);
        }
        View dateView = layout.getChildAt(layout.getChildCount() - 1);
        TextView dateText = (TextView) dateView.findViewById(R.id.message_date);
        showElements(dateText);
    }

    private void showElements(final TextView mHiddenLinearLayout) {
        mHiddenLinearLayout.setVisibility(View.VISIBLE);
        mHiddenLinearLayout.setAlpha(0.0f);
        mHiddenLinearLayout
                .animate()
                .setDuration(500L)
                .alpha(1.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mHiddenLinearLayout.animate().setListener(null);
                    }
                })
        ;
    }

    private void hideElements(final TextView mHiddenLinearLayout) {
        mHiddenLinearLayout
                .animate()
                .setDuration(500L)
                .alpha(0.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mHiddenLinearLayout.setVisibility(View.GONE);
                    }
                })
        ;
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
                ((TextView) v.findViewById(R.id.member_props_last_online)).setText(member.last_message);

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
                builder.setPositiveButton(getResources().getString(R.string.close), new DialogInterface.OnClickListener() {
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

