package com.example.b00047562.organicbox;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

public class CheckoutActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /*TODO
    Add price tags to each item
     */

    EditText billadd, cardnum, cvc, eyear, emonth;
    CheckBox address;
    Button pay;
    Customer customer;
    ProgressDialog mProgressDialog;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    private Double chargeamount;
    private Double totalcharge;
    SharedPreferences prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        pay = (Button) findViewById(R.id.btn_checkout_pay);
        pay.setOnClickListener(this);

        billadd = (EditText) findViewById(R.id.editText_billadd);
        cardnum = (EditText) findViewById(R.id.editText_creditcardnum);
        cvc = (EditText) findViewById(R.id.editText_CVC);
        eyear = (EditText) findViewById(R.id.editText_expiryyear);
        emonth = (EditText) findViewById(R.id.editTextexpirymonth);
        address=(CheckBox)findViewById(R.id.checkBox_Address);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();//required by Stripe
        StrictMode.setThreadPolicy(policy);//required by Stripe

        //Google Location Services
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        } else{
            //displayPromptForEnablingGPS(this);
        }
        //Toast.makeText(this, "Not connected...", Toast.LENGTH_SHORT).show();
        //Google Location Services

        //Intent i = getIntent();
        //totalcharge = i.getDoubleExtra("totalcharge",Math.round(1));
        //Log.d("CheckoutCharge",totalcharge+"");

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        address.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    billadd.setEnabled(false);
                    billadd.setText(prefs.getString("ShipAd","N/A"));
                }
                else
                {
                    billadd.setEnabled(true);
                    billadd.setText("");
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_checkout_pay:
                String billingad = billadd.getText().toString();
                String cardnumber = cardnum.getText().toString();
                String cvcnum = cvc.getText().toString();
                String cardyear = eyear.getText().toString();
                String cardmonth= emonth.getText().toString();

                if (billingad.isEmpty() || cardnumber.isEmpty()|| cvcnum.isEmpty() || cardyear.isEmpty() || cardmonth.isEmpty()) {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(CheckoutActivity.this);
                    builder.setMessage(R.string.checkout_error_message)
                            .setTitle(R.string.checkout_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    android.app.AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else
                    new UpdateListsTask().execute();
                break;
        }
    }

    private void validatePay() throws AuthenticationException, InvalidRequestException, APIConnectionException, CardException, APIException {
        final Card card = new Card(cardnum.getText().toString().trim(), Integer.parseInt(emonth.getText().toString()), Integer.parseInt(eyear.getText().toString()), cvc.getText().toString().trim());
        final Stripe stripe = new Stripe("pk_test_GxD0bUCCzUEqy2CKiKf8TEcB");
        //Log.d("cardinfo", cardnum.getText().toString().trim() + "\n" + emonth.getText().toString() + "\n" + eyear.getText().toString() + "\n" + cvc.getText().toString().trim());
        //card.validateCard();

        if (card.validateCard()) {
            CheckoutActivity.this.runOnUiThread(new Runnable() {
                public void run() {

            stripe.createToken(
                    card,
                    new TokenCallback() {

                        public void onSuccess(Token token) {


                            com.stripe.Stripe.apiKey = "sk_test_2PsJDIaB5Fzw0ZFoDrrCFEwV";


                            Map<String, Object> customerParams = new HashMap<String, Object>();
                            customerParams.put("source", token.getId());
                            customerParams.put("description", ParseUser.getCurrentUser().getUsername());

                            try {
                                customer = Customer.create(customerParams);
                            } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException | APIException e1) {
                                Toast.makeText(getApplicationContext(), "Customer Create Error", Toast.LENGTH_SHORT).show();
                            }


                            ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
                            query.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseObject>() {
                                public void done(ParseObject user, ParseException e) {
                                    if (e == null) {
                                        user.put("CustomerID", customer.getId());
                                        user.put("BillingAddress", billadd.getText().toString());
                                        user.saveInBackground();
                                    }
                                }
                            });


                            final Map<String, Object> chargeParams = new HashMap<String, Object>();

                            chargeParams.put("currency", "aed");
                            chargeParams.put("customer", customer.getId());
                            chargeParams.put("description", "OrganicBox Purchase");


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
                                            orderlist.put("orderaddress", billadd.getText().toString());
                                            orderlist.put("image", basketlist.get(i).get("image"));
                                            orderlist.put("tracker_status", "Preparing");
                                            chargeamount=basketlist.get(i).getDouble("price");
                                            orderlist.put("price",chargeamount);

                                            if (mLastLocation != null)
                                                orderlist.put("order_loc", new ParseGeoPoint(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                                            else
                                                orderlist.put("order_loc", new ParseGeoPoint(0, 0));
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
                            chargeParams.put("amount",Math.round(totalcharge*100));
                            try {
                                Charge.create(chargeParams);//charge card

                                Toast.makeText(getApplicationContext(), "Payment Processed", Toast.LENGTH_SHORT).show();
                            } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException | APIException e2) {
                                Toast.makeText(getApplicationContext(), "Card Declined", Toast.LENGTH_SHORT).show();
                            }
                        }

                        public void onError(Exception error) {
                            // Show localized error message
                            Toast.makeText(getApplicationContext(), "Token Error", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "Validation Error", Toast.LENGTH_SHORT).show();
        }


    }



    private class UpdateListsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Create a progressdialog
            mProgressDialog = new ProgressDialog(CheckoutActivity.this);
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
            try {
                validatePay();
            } catch (AuthenticationException | InvalidRequestException | APIConnectionException | APIException | CardException e) {
                Toast.makeText(getApplicationContext(), "Stripe Connection Error", Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            mProgressDialog.dismiss();
            Vibrator vibe = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE) ;
            vibe.vibrate(50); // 50 is time in ms

            ParseQuery<ParseObject> querycharge = ParseQuery.getQuery("Totals");
            querycharge.whereEqualTo("createdBy", ParseUser.getCurrentUser());
            querycharge.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> totalist, ParseException e) {
                    if (e == null) {
                        int size = totalist.size();
                        for (int i = 0; i < size; i++) {
                            totalist.get(i).put("total", 0.0);
                            totalist.get(i).saveInBackground();
                        }
                        finish();
                    } else {

                    }
                }
            });

            finish();

        }
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


    @Override
    protected void onResume() {
        super.onResume();
        totalcharge = Double.valueOf(prefs.getFloat("chargetotal",1));
        Log.d("Checkout",""+totalcharge);
    }
}





