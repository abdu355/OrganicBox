package com.example.b00047562.organicbox;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
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

public class MyBasket extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /*TODO
    Add price tags to each item
     */

    ListView orders;
    List<ParseObject> ob;
    ProgressDialog mProgressDialog;
    BasketAdapter adapter;
    private List<OrderBox> orderBoxList = null;
    Button checkout, quickpay, clear;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;

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
        clear = (Button) findViewById(R.id.clear_btn);

        checkout.setOnClickListener(this);
        quickpay.setOnClickListener(this);
        clear.setOnClickListener(this);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();//required by Stripe
        StrictMode.setThreadPolicy(policy);//required by Stripe
        buildGoogleApiClient();

        //Google Location Services
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        } else{
            //displayPromptForEnablingGPS(this);
        }
            //Toast.makeText(this, "Not connected...", Toast.LENGTH_SHORT).show();
        //Google Location Services
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
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("Returning Customer")
                        .setMessage("Pay Now?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new UpdateListsTask().execute();
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();

                break;
            case R.id.clear_btn:
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Empty Basket")
                        .setMessage("Are you sure you want to clear your basket?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //clear basket
                                clearBasket();
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();
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
                    ParseObject imageobject = (ParseObject) order.get("image");
                    ParseFile image = imageobject.getParseFile("image");

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
            adapter = new BasketAdapter(MyBasket.this,
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
        protected Void doInBackground(Void... params) {
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
                            int size = basketlist.size();
                            List<ParseObject> neworderslist = new ArrayList<ParseObject>();
                            ParseObject orderlist = new ParseObject("Orders");
                            for (int i = 0; i < size; i++) {
                                orderlist = new ParseObject("Orders");
                                orderlist.put("username", ParseUser.getCurrentUser().getUsername());
                                orderlist.put("createdBy", ParseUser.getCurrentUser());
                                orderlist.put("name", basketlist.get(i).get("name"));
                                orderlist.put("type", basketlist.get(i).get("type"));
                                orderlist.put("orderaddress", ParseUser.getCurrentUser().get("BillingAddress"));
                                orderlist.put("image", basketlist.get(i).get("image"));
                                orderlist.put("tracker_status", "Preparing");
                                if(mLastLocation!=null)
                                    orderlist.put("order_loc", new ParseGeoPoint(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                                else
                                    orderlist.put("order_loc", new ParseGeoPoint(0,0));
                                neworderslist.add(i, orderlist);
                            }
                            orderlist.saveAllInBackground(neworderslist);
                            for (int i = 0; i < size; i++) {
                                try {
                                    basketlist.get(i).delete();
                                    //basketlist.get(i).saveInBackground();
                                } catch (ParseException e1) {
                                    Toast.makeText(getApplicationContext(), "Basket Clear Failed", Toast.LENGTH_SHORT).show();
                                    Log.d("ParseBasket", e1.getMessage());
                                }
                            }
                        } else {

                        }
                    }
                });

            } catch (NullPointerException|AuthenticationException | InvalidRequestException | APIConnectionException | CardException | APIException e) {
                Log.d("Stripe", e.getMessage());
                MyBasket.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "No ID found", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), CheckoutActivity.class));
                    }
                });

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            mProgressDialog.dismiss();
            Vibrator vibe = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
            vibe.vibrate(50); // 50 is time in ms
            finish();

        }
    }

    private void clearBasket() {
        ParseQuery<ParseObject> querybasket = ParseQuery.getQuery("Basket");
        querybasket.whereEqualTo("createdBy", ParseUser.getCurrentUser());
        querybasket.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> basketlist, ParseException e) {
                if (e == null) {
                    int size = basketlist.size();
                    for (int i = 0; i < size; i++) {
                        try {
                            basketlist.get(i).delete();
                        } catch (ParseException e1) {
                            Toast.makeText(getApplicationContext(), "Basket Clear Failed", Toast.LENGTH_SHORT).show();
                            Log.d("ParseBasket", e1.getMessage());
                        }
                    }
                    finish();
                } else {

                }
            }
        });

    }


    public static void displayPromptForEnablingGPS(final Activity activity)
    {

        final AlertDialog.Builder builder =  new AlertDialog.Builder(activity);
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        final String message = "Do you want open GPS setting?";

        builder.setMessage(message)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                activity.startActivity(new Intent(action));
                                d.dismiss();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                            }
                        });
        builder.create().show();
    }

    //Google Location Services
    @Override
    public void onConnected(Bundle bundle) {

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Failed to connect to GPS", Toast.LENGTH_SHORT).show();
        displayPromptForEnablingGPS(this);

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    //Google Location Services

}
