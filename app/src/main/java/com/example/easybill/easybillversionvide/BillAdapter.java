package com.example.easybill.easybillversionvide;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by louis on 21/11/2017.
 */

public class BillAdapter extends ArrayAdapter<Bill> {

    private Context mContext;
    private int mResource;

    public BillAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Bill> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // Get the bill's information
        float price = getItem(position).getPrice();
        String place = getItem(position).getPlace();
        String date = getItem(position).getDate();
        String path = getItem(position).getPath();
        String folder = getItem(position).getFolder();

        // Create the Bill object
        Bill bill = new Bill(price, place, date, path, folder);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView tvPrice = (TextView) convertView.findViewById(R.id.price);
        TextView tvPlace = (TextView) convertView.findViewById(R.id.place);
        TextView tvDate = (TextView) convertView.findViewById(R.id.date);

        tvPrice.setText(Float.toString(price));
        tvPlace.setText(place);
        tvDate.setText(date);

        return convertView;
    }

}
