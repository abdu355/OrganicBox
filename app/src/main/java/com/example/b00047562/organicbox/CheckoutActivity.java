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
import android.widget.EditText;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
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


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

public class CheckoutActivity extends AppCompatActivity implements View.OnClickListener {

    EditText billadd, cardnum, cvc, eyear, emonth;
    Button pay;
    Customer customer;
    ProgressDialog mProgressDialog;


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

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_checkout_pay:
                new UpdateListsTask().execute();
                break;
        }
    }

    private void validatePay() throws AuthenticationException, InvalidRequestException, APIConnectionException, CardException, APIException {
        Card card = new Card(cardnum.getText().toString().trim(), Integer.parseInt(emonth.getText().toString()), Integer.parseInt(eyear.getText().toString()), cvc.getText().toString().trim());
        //Log.d("cardinfo", cardnum.getText().toString().trim() + "\n" + emonth.getText().toString() + "\n" + eyear.getText().toString() + "\n" + cvc.getText().toString().trim());

        //card.validateCard();
        if (card.validateCard()) {
            Stripe stripe = new Stripe("pk_test_GxD0bUCCzUEqy2CKiKf8TEcB");
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
                            chargeParams.put("amount", 100 * 20);//$20.00
                            chargeParams.put("currency", "usd");
                            chargeParams.put("customer", customer.getId());
                            chargeParams.put("description", "OrganicBox Purchase");

                                /*save CUSTOMER ID to parse here */
                            try {
                                Charge.create(chargeParams);//charge card

                                Toast.makeText(getApplicationContext(), "Payment Processed", Toast.LENGTH_SHORT).show();
                            } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException | APIException e2) {
                                Toast.makeText(getApplicationContext(), "Card Declined", Toast.LENGTH_SHORT).show();
                            }


                            // /*save Orders to parse here */
                            ParseQuery<ParseObject> querybasket = ParseQuery.getQuery("Basket");
                            querybasket.whereEqualTo("createdBy", ParseUser.getCurrentUser());
                            querybasket.findInBackground(new FindCallback<ParseObject>() {
                                public void done(List<ParseObject> basketlist, ParseException e) {
                                    if (e == null) {
                                        int size = basketlist.size();
                                        ParseObject orderlist = new ParseObject("Orders");
                                         /*TODO
                                            fix this function
                                         */
                                        for (int i = 0; i < size; i++) {
                                            orderlist.put("username", ParseUser.getCurrentUser().getUsername());
                                            orderlist.put("createdBy", ParseUser.getCurrentUser());
                                            orderlist.put("name", basketlist.get(i).get("name"));
                                            orderlist.put("type", basketlist.get(i).get("type"));
                                            orderlist.put("orderaddress", billadd.getText().toString());
                                            orderlist.saveInBackground();
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


                        }

                        public void onError(Exception error) {
                            // Show localized error message
                            Toast.makeText(getApplicationContext(), "Token Error", Toast.LENGTH_LONG).show();
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

        }
    }
}




