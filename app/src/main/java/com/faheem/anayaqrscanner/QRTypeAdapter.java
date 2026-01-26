package com.faheem.anayaqrscanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class QRTypeAdapter extends BaseAdapter {

    private Context context;
    private List<QRType> qrTypeList;
    private LayoutInflater inflater;

    public QRTypeAdapter(Context context, List<QRType> qrTypeList) {
        this.context = context;
        this.qrTypeList = qrTypeList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return qrTypeList.size();
    }

    @Override
    public Object getItem(int position) {
        return qrTypeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_qr_type, parent, false);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.ivIcon);
            holder.name = convertView.findViewById(R.id.tvLabel);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        QRType qrType = qrTypeList.get(position);
        holder.icon.setImageResource(qrType.getIcon());
        holder.name.setText(qrType.getName());

        return convertView;
    }

    static class ViewHolder {
        ImageView icon;
        TextView name;
    }
}