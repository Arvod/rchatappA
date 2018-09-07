package com.retarcorp.rchatapp.UI;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.retarcorp.rchatapp.Model.ChatMessage;
import com.retarcorp.rchatapp.R;

import java.util.ArrayList;

/**
 * Created by CaptainOsmant on 11.01.2018.
 */

@Deprecated
public class MessageAdapter extends ArrayAdapter<ChatMessage> {

    @Deprecated
    public MessageAdapter(@NonNull Context context, ArrayList<ChatMessage> items) {
        super(context, R.layout.message_layout,items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        final ChatMessage m = getItem(position);
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_layout, null);
        ((TextView)convertView.findViewById(R.id.message_text)).setText(m.text);

        if(m.direction == ChatMessage.Direction.ADMIN) {
            convertView.findViewById(R.id.message_layer).setPadding(50, 0, 0, 0);
        }else{
            convertView.findViewById(R.id.message_layer).setPadding(50, 0, 0, 0);
        }


        return convertView;
    }
}
