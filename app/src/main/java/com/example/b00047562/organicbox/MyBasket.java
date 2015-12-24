package com.example.b00047562.organicbox;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Charge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyBasket extends AppCompatActivity implements View.OnClickListener {

    ListView orders;
    List<ParseObject> ob;
    ProgressDialog mProgressDialog;
    BasketWishAdapter adapter;
    private List<OrderBox> orderBoxList = null;
    Button checkout, quickpay,clear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_basket);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        checkout = (Button) findViewById(R.id.checkout_btn);
        quickpay = (Button) findViewById(R.id.btn_paybasket);
        clear=(Button)findViewById(R.id.clear_btn);

        checkout.setOnClickListener(this);
        quickpay.setOnClickListener(this);
        clear.setOnClickListener(this);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new RemoteDataTask().execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.checkout_btn:
                startActivity(new Intent(this, CheckoutActivity.class));
                break;
            case R.id.btn_paybasket:
                new UpdateListsTask().execute();
                break;
        }
    }

    private class RemoteDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Create a progressdialog
            mProgressDialog = new ProgressDialog(MyBasket.this);
            // Set progressdialog title
            mProgressDialog.setTitle("Basket");
            // Set progressdialog message
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            // Show progressdialog
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            orderBoxList = new ArrayList<OrderBox>();
            // Locate the class table named "Country" in Parse.com
            ParseQuery<ParseObject> query = new ParseQuery("Basket");
            query.whereEqualTo("createdBy", ParseUser.getCurrentUser());
            query.include("image");
            query.orderByDescending("createdAt");

            try {
                ob = query.find();
                for (ParseObject order : ob) {
                    // Locate images in flag column
                    ParseObject imageobject = (ParseObject)order.get("image");
                    ParseFile image= imageobject.getParseFile("image");

                    OrderBox map = new OrderBox();
                    map.setName((String) order.get("name"));
                    map.setType((String) order.get("type"));
                    map.setImage(image.getUrl());
                    orderBoxList.add(map);
                }
            } catch (ParseException e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            orders = (ListView) findViewById(R.id.list_basket);
            // Pass the results into ListViewAdapter.java
            adapter = new BasketWishAdapter(MyBasket.this,
                    orderBoxList);
            // Binds the Adapter to the ListView
            if (adapter.getCount() != 0) {
                orders.setAdapter(adapter);
            } else {
                quickpay.setEnabled(false);
                checkout.setEnabled(false);
                clear.setEnabled(false);
            }
            // Close the progressdialog
            mProgressDialog.dismiss();

        }
    }
    private class UpdateListsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Create a progressdialog
            mProgressDialog = new ProgressDialog(MyBasket.this);
            // Set progressdialog title
            mProgressDialog.setTitle("");
            // Set progressdialog message
            mProgressDialog.setMessage("Processing Purchase...");
            mProgressDialog.setIndeterminate(false);
            // Show progressdialog
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params)  {
            String cust_id;
            try {
                com.stripe.Stripe.apiKey = "sk_test_2PsJDIaB5Fzw0ZFoDrrCFEwV";
                //check for existing user
                cust_id = ParseUser.getCurrentUser().get("CustomerID").toString();

                //Log.d("Parse", cust_id);

                final Map<String, Object> quickchargeParams = new HashMap<String, Object>();
                quickchargeParams.put("amount", 100 * 20);//$20.00
                quickchargeParams.put("currency", "usd");
                quickchargeParams.put("customer", cust_id);
                quickchargeParams.put("description", "OrganicBox Purchase");


                Charge.create(quickchargeParams);//charge card

                // /*save Orders to parse here */
                ParseQuery<ParseObject> querybasket = ParseQuery.getQuery("Basket");
                querybasket.whereEqualTo("createdBy", ParseUser.getCurrentUser());
                querybasket.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> basketlist, ParseException e) {
                        if (e == null) {
                            int size= basketlist.size();
                            ParseObject orderlist = new ParseObject("Orders");
                            /*TODO
                                 fix this function
                             */
                            for (int i = 0; i < size; i++) {
                                orderlist.put("username", ParseUser.getCurrentUser().getUsername());
                                orderlist.put("createdBy", ParseUser.getCurrentUser());
                                orderlist.put("name", basketlist.get(i).get("name"));
                                orderlist.put("type", basketlist.get(i).get("type"));
                                orderlist.put("orderaddress",ParseUser.getCurrentUser().get("BillingAddress"));
                                orderlist.put("image",basketlist.get(i).get("image"));
                                orderlist.saveInBackground();

                            }
                            for(int i=0; i<size;i++)
                            {
                                try {
                                    basketlist.get(i).delete();
                                    //basketlist.get(i).saveInBackground();
                                } catch (ParseException e1) {
                                    Toast.makeText(getApplicationContext(), "Basket Clear Failed", Toast.LENGTH_SHORT).show();
                                    Log.d("ParseBasket",e1.getMessage());
                                }
                            }
                        } else {

                        }
                    }
                });

            } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException | APIException e) {
                Log.d("Stripe",e.getMessage());
                Toast.makeText(getApplicationContext(), "No ID found", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), CheckoutActivity.class));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            mProgressDialog.dismiss();

        }
    }

}
