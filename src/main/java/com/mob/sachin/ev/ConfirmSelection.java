package com.mob.sachin.ev;

import android.app.Activity;
import android.os.Bundle;
//import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;

/**
 * Created by Ankit on 10-Apr-16.
 */
public class ConfirmSelection extends Activity {

    //private ArrayAdapter ConfirmedListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selection_confirmation);
        ListView displayListView = (ListView) findViewById(R.id.listConfirm);
        ArrayList<ProductDetails> confirmList = new ArrayList<>();
        try {
            // Get the Bundle Object
            Bundle bundleObject = getIntent().getExtras();

            // Get ArrayList Bundle
            ArrayList<ProductDetails> classObject = (ArrayList<ProductDetails>) bundleObject.getSerializable("key");

            //Retrieve Objects from Bundle
            for (int index = 0; index < classObject.size(); index++) {

                ProductDetails Object = classObject.get(index);
                int val = Integer.parseInt(Object.selectedQuantity);
                if(val != 0){
                    //Toast.makeText(getApplicationContext(), Object.productName, Toast.LENGTH_SHORT).show();
                    confirmList.add(Object);
                }
            }

            MyCustomConfirmedListAdapter adapter = new MyCustomConfirmedListAdapter(ConfirmSelection.this, confirmList);
            displayListView.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
