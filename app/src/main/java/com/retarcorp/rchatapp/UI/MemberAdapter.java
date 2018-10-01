package com.retarcorp.rchatapp.UI;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.retarcorp.rchatapp.ChatActivity;
import com.retarcorp.rchatapp.Model.Member;
import com.retarcorp.rchatapp.R;

import java.util.ArrayList;

/**
 * Created by CaptainOsmant on 11.01.2018.
 */

public class MemberAdapter extends ArrayAdapter<Member> {

    public MemberAdapter(@NonNull Context context, ArrayList<Member> members) {
        super(context, R.layout.member_layout, members);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        final Member m = getItem(position);
        convertView = LayoutInflater.from(getContext()).inflate(
                R.layout.member_layout, null);
        if(m.unread > 0){
            convertView.findViewById(R.id.member_layout_main).setBackgroundColor(Color.parseColor("#ccffcc"));
        }
        ((TextView)convertView.findViewById(R.id.member_last_message)).setText(m.last_message);
        ((TextView)convertView.findViewById(R.id.member_messages)).setText("Сообщений: "+m.messages + " (+"+m.unread+")");
        ((TextView)convertView.findViewById(R.id.member_ssid)).setText(m.ssid);
        convertView.findViewById(R.id.member_layout_main).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ChatActivity.class);

                intent.putExtra("member_id", m.getId());
                getContext().startActivity(intent);
            }
        });

        return convertView;
    }
}
