package com.retarcorp.rchatapp.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.retarcorp.rchatapp.Model.Site;
import com.retarcorp.rchatapp.R;
import com.retarcorp.rchatapp.SiteMembersActivity;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

public class SiteAdapter extends ArrayAdapter<Site> {

    public SiteAdapter(@NonNull Context context, ArrayList<Site> sites) {
        super(context, R.layout.site_adapter_element, sites);
    }

    @NonNull
    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final Site site = getItem(position);
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.site_adapter_element,null);
        assert site != null;
        ((TextView)convertView.findViewById(R.id.sitename)).setText(site.getTitle());

        if(site.icon!=null) {
            if (!site.icon.trim().equals("")) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(Base64.decode(site.icon, Base64.DEFAULT)));
                    ((ImageView) convertView.findViewById(R.id.site_image)).setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        convertView.findViewById(R.id.site_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getContext(), SiteMembersActivity.class);
                intent.putExtra("site_id", site.getId());
                getContext().startActivity(intent);
            }
        });
        return convertView;
    }

}
