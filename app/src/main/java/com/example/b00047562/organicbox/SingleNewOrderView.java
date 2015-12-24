package com.example.b00047562.organicbox;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SingleNewOrderView extends AppCompatActivity implements View.OnClickListener {

    TextView txtname, txttype, stockalert;
    String name, type;
    String image;
    Spinner typespinner;
    ImageLoader imageLoader = new ImageLoader(this);
    Button add, wish;
    private String[] arraySpinner;
    Map<String, String> myMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_new_order_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        add = (Button) findViewById(R.id.addbasket_btn);
        wish = (Button) findViewById(R.id.addtowish_btn);
        typespinner = (Spinner) findViewById(R.id.spinner_packtype);
        stockalert = (TextView) findViewById(R.id.outofstockalert_tv);

        add.setOnClickListener(this);
        wish.setOnClickListener(this);
        this.arraySpinner = new String[]{"2 KG", "5 KG", "10 KG", "15 KG"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        typespinner.setAdapter(adapter);


        // Retrieve data from MainActivity on item click event
        Intent i = getIntent();

        // Get the name
        name = i.getStringExtra("name");
        type = i.getStringExtra("type");
        image = i.getStringExtra("image");

        txtname = (TextView) findViewById(R.id.tv_name_neworder);
        txttype = (TextView) findViewById(R.id.tv_type_neworder);

        ImageView imgflag = (ImageView) findViewById(R.id.img_neworder);

        // Load the text into the TextView
        txtname.setText("Name: " + name);
        txttype.setText("Type: " + type);
        imageLoader.DisplayImage(image, imgflag);


        ParseQuery<ParseObject> query = ParseQuery.getQuery("Stock");
        query.whereEqualTo("name", name);
        query.whereEqualTo("instock", 0);

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> item, ParseException e) {
                if (e == null) {
                    if (item.size() == 0) {
                        stockalert.setText("");
                    } else if (item.size() > 0) {
                        stockalert.setText(item.get(0).getString("name") + " Out of Stock!");
                        add.setEnabled(false);
                    }

                } else {
                    Log.d("stockload", "Error: " + e.getMessage());
                }
            }
        });

        myMap  = new HashMap<String, String>();//object ids of stock
        myMap.put("Carrot","lvHUd3Uf95" );
        myMap.put("Cucumber","KTCyMap5XS" );
        myMap.put("Tomato", "LfJqnMovwm");
        myMap.put("Lettuce","9xoOQRui2e" );
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.addbasket_btn:
                ParseObject basket = new ParseObject("Basket");
                basket.put("name", name);
                basket.put("type", typespinner.getSelectedItem().toString());
                basket.put("createdBy", ParseUser.getCurrentUser());
                basket.put("image",ParseObject.createWithoutData("Stock", myMap.get(name)));
                basket.saveInBackground();
                break;
            case R.id.addtowish_btn:
                ParseObject wishlist = new ParseObject("WishList");
                wishlist.put("name", name);
                wishlist.put("type", typespinner.getSelectedItem().toString());
                wishlist.put("createdBy", ParseUser.getCurrentUser());
                wishlist.put("image",ParseObject.createWithoutData("Stock", myMap.get(name)));
                wishlist.saveInBackground();
                break;
        }
    }
}
