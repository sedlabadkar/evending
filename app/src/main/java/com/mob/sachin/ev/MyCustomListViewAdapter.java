package com.mob.sachin.ev;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by Ankit on 04-Apr-16.
 */
public class MyCustomListViewAdapter extends ArrayAdapter<ProductDetails> {
    Context currApplicationContext;
    public ArrayList<ProductDetails> arrayList;

    public MyCustomListViewAdapter(Context context, ArrayList<ProductDetails> p) {
        super(context, R.layout.list_item, p);
        currApplicationContext = context;
        arrayList = p;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
            LayoutInflater inflater = LayoutInflater.from(currApplicationContext);
            View row = inflater.inflate(R.layout.list_item, parent, false);
            TextView productName = (TextView) row.findViewById(R.id.productName);
            productName.setText("Product Name: " + arrayList.get(position).productName);
            final TextView productAvailableQuantity = (TextView) row.findViewById(R.id.availableQuantity);
            productAvailableQuantity.setText("Available Quantity: " + arrayList.get(position).quantityAvailable);
            final TextView selectedQuantity = (TextView) row.findViewById(R.id.selectQuantity);
            selectedQuantity.setText("0");
            final Button btnAddition = (Button) row.findViewById(R.id.btnAdd);
            btnAddition.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (v == btnAddition) {
                        String temp = selectedQuantity.getText().toString();
                        int i = Integer.parseInt(temp);
                        String str = productAvailableQuantity.getText().toString().substring(20);
                        int j = Integer.parseInt(str);
                        if(i < j) {
                            i = i + 1;
                            temp = Integer.toString(i);
                            selectedQuantity.setText(temp);
                            arrayList.get(position).selectedQuantity = temp;
                        }
                        else{
                            temp = Integer.toString(i);
                            selectedQuantity.setText(temp);
                            arrayList.get(position).selectedQuantity = temp;
                        }
                    }
                }
            });
            final Button btnSub = (Button) row.findViewById(R.id.btnRemove);
            btnSub.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (v == btnSub) {
                        String temp = selectedQuantity.getText().toString();
                        int i = Integer.parseInt(temp);
                        if(i > 0) {
                            i = i - 1;
                            temp = Integer.toString(i);
                            selectedQuantity.setText(temp);
                            arrayList.get(position).selectedQuantity = temp;
                        }
                        else{
                            temp = Integer.toString(i);
                            selectedQuantity.setText(temp);
                            arrayList.get(position).selectedQuantity = temp;
                        }
                    }
                }
            });
            return row;
    }
}
