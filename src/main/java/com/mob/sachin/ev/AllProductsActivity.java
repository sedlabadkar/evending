package com.mob.sachin.ev;
/**
 * Created by Ankit on 02-Apr-16.
 */
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AllProductsActivity extends ListActivity {

    // Progress Dialog
    private ProgressDialog pDialog;

    ArrayList<ProductDetails> productDetailsArrayList = new ArrayList<>();

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    // JSON Node names
    private static final String TAG_PID = "offset";
    private static final String TAG_NAME = "name";
    private static final String TAG_UNIQUEID = "ndbno";
    String url_all_products;
    // products JSONArray
    JSONArray products = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_products);
        Intent iin= getIntent();
        Bundle b = iin.getExtras();

        if(b!=null)
        {
            url_all_products =(String) b.get("url");
            System.out.println("URL " + url_all_products);
            //Textv.setText(j);
        }

        // Loading products in Background Thread
        new LoadAllProducts().execute();

        Button proceedButton = (Button) findViewById(R.id.proceedButton);

        proceedButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent j = new Intent(getApplicationContext(), ConfirmSelection.class);
                Bundle productDisplayForConfirmation = new Bundle();
                productDisplayForConfirmation.putSerializable("key", productDetailsArrayList);
                j.putExtras(productDisplayForConfirmation);
                startActivity(j);
            }
        });
    }

    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadAllProducts extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AllProductsActivity.this);
            pDialog.setMessage("Loading products. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            // getting JSON string from URL
            String url_all_products = "http://api.nal.usda.gov/ndb/search/?format=json&q=french%20fries&sort=n&max=250&offset=0&api_key=DEMO_KEY";
            JSONObject json = jParser.makeHttpRequest(url_all_products, "GET", params);

            // Check your log cat for JSON response
            Log.d("All Products: ", json.toString());

            try {
                // Getting Array of Products
                products = json.getJSONObject("list").getJSONArray("item");

                // looping through All Products
                for (int i = 0; i < products.length(); i++) {
                    JSONObject c = products.getJSONObject(i);

                    // Storing each json item in variable
                    ProductDetails temp = new ProductDetails();
                    temp.productName = c.getString(TAG_NAME);
                    temp.quantityAvailable = c.getString(TAG_PID);
                    temp.uniqueId = c.getString(TAG_UNIQUEID);

                    productDetailsArrayList.add(temp);

                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {

            // dismiss the dialog after getting all products
            pDialog.dismiss();

            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                MyCustomListViewAdapter adapter = new MyCustomListViewAdapter(AllProductsActivity.this, productDetailsArrayList);
                setListAdapter(adapter);
                }
            });
        }
    }
}
