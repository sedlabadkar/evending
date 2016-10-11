package com.mob.sachin.ev;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Ankit on 12-Apr-16.
 */
public class MyCustomConfirmedListAdapter extends ArrayAdapter<ProductDetails> {
    Context currentApplicationContext;
    ArrayList<ProductDetails> arraylist;

    public MyCustomConfirmedListAdapter(Context context, ArrayList<ProductDetails> p1){
        super(context, R.layout.confirmed_list_layout, p1);
        currentApplicationContext = context;
        arraylist = p1;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = LayoutInflater.from(currentApplicationContext);
        View row = inflater.inflate(R.layout.confirmed_list_layout, parent, false);
        TextView productBought = (TextView) row.findViewById(R.id.confirmedProductName);
        productBought.setText("Product Name: " + arraylist.get(position).productName);
        return row;
    }
}
