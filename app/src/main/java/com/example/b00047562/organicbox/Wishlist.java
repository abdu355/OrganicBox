package com.example.b00047562.organicbox;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class Wishlist extends AppCompatActivity implements View.OnClickListener {

    Button clearwishlist;
    ListView orders;
    List<ParseObject> ob;
    ProgressDialog mProgressDialog;
    WishAdapter adapter;
    private List<OrderBox> orderBoxList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        clearwishlist=(Button)findViewById(R.id.btn_clearwishlist);
        clearwishlist.setOnClickListener(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        new RemoteDataTask().execute();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_clearwishlist:
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Empty Wishlist")
                        .setMessage("Are you sure you want to clear your wishlist?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //clear wishlist
                                clearWishlist();
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();
                break;
        }
    }
    public class RemoteDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Create a progressdialog
            mProgressDialog = new ProgressDialog(Wishlist.this);
            // Set progressdialog title
            mProgressDialog.setTitle("Wishlist");
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
            ParseQuery<ParseObject> query = new ParseQuery("WishList");
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
            orders = (ListView) findViewById(R.id.lv_wishlist);
            // Pass the results into ListViewAdapter.java
            adapter = new WishAdapter(Wishlist.this,
                    orderBoxList);
            // Binds the Adapter to the ListView
            if (adapter.getCount() != 0) {
                orders.setAdapter(adapter);
            } else {
                clearwishlist.setEnabled(false);
            }
            // Close the progressdialog
            mProgressDialog.dismiss();

        }
    }
    private void clearWishlist()
    {
        ParseQuery<ParseObject> querybasket = ParseQuery.getQuery("WishList");
        querybasket.whereEqualTo("createdBy", ParseUser.getCurrentUser());
        querybasket.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> basketlist, ParseException e) {
                if (e == null) {
                    int size= basketlist.size();
                    for(int i=0; i<size;i++)
                    {
                        try {
                            basketlist.get(i).delete();
                        } catch (ParseException e1) {
                            Toast.makeText(getApplicationContext(), "Wishlist Clear Failed", Toast.LENGTH_SHORT).show();
                            Log.d("ParseBasket",e1.getMessage());
                        }
                    }
                } else {

                }
            }
        });

    }


}
