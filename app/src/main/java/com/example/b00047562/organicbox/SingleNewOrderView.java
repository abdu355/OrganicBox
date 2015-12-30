package com.example.b00047562.organicbox;

import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SingleNewOrderView extends AppCompatActivity implements View.OnClickListener {

    TextView txtname, txttype, stockalert,pricetag;
    String name, type;
    String image;
    Spinner typespinner;
    ImageLoader imageLoader = new ImageLoader(this);
    Button add, wish;
    private String[] arraySpinner;
    Map<String, String> myMap;
    private double price,pricefetch,item_kg;
    TypedArray selectedValues;
    public ParseObject charge = new ParseObject("Totals");

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
        pricetag=(TextView)findViewById(R.id.tv_price);




        add.setOnClickListener(this);
        wish.setOnClickListener(this);

        Resources res = getResources();
        this.arraySpinner = new String[]{"2 KG", "5 KG", "10 KG", "15 KG"};
        selectedValues = res.obtainTypedArray(R.array.kg_values);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        typespinner.setAdapter(adapter);




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

        myMap = new HashMap<String, String>();//object ids of stock
        myMap.put("Carrot", "lvHUd3Uf95");
        myMap.put("Cucumber", "KTCyMap5XS");
        myMap.put("Tomato", "LfJqnMovwm");
        myMap.put("Lettuce", "9xoOQRui2e");

        queryitemprice();
        getPrice(0);
        pricetag.setText("AED " + String.valueOf(price));

        typespinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                getPrice(position);
                pricetag.setText("AED " + String.valueOf(price));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.addbasket_btn:
                ParseObject basket = new ParseObject("Basket");
                basket.put("name", name);
                basket.put("type", typespinner.getSelectedItem().toString());
                basket.put("createdBy", ParseUser.getCurrentUser());
                basket.put("image", ParseObject.createWithoutData("Stock", myMap.get(name)));
                basket.put("price", price);

                ParseQuery<ParseObject> query = ParseQuery.getQuery("Totals");
                query.whereEqualTo("createdBy", ParseUser.getCurrentUser());
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> totallist, ParseException e) {
                        if (e == null) {
                            if(totallist.size()>0) {
                                totallist.get(0).increment("total", price);
                                totallist.get(0).saveInBackground();
                            }
                            else
                            {
                                charge = new ParseObject("Totals");
                                charge.put("createdBy", ParseUser.getCurrentUser());
                                charge.increment("total", price);
                                charge.saveInBackground();
                            }
                        } else {

                        }
                    }
                });
                basket.saveInBackground();


                Toast.makeText(getApplicationContext(), "Item Added to Basket", Toast.LENGTH_SHORT).show();
                break;
            case R.id.addtowish_btn:
                ParseObject wishlist = new ParseObject("WishList");
                wishlist.put("name", name);
                wishlist.put("type", typespinner.getSelectedItem().toString());
                wishlist.put("createdBy", ParseUser.getCurrentUser());
                wishlist.put("image", ParseObject.createWithoutData("Stock", myMap.get(name)));
                wishlist.put("price",price);

                wishlist.saveInBackground();
                Toast.makeText(getApplicationContext(), "Item Added to Wishlist", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void getPrice(final int pos) {

        item_kg =(double)selectedValues.getInt(pos, -1)*1.0;
        price= pricefetch*item_kg*1.0;
    }
    private void queryitemprice()
    {
        price =0;
        item_kg=0;
        pricefetch=0;

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Stock");
        query.whereEqualTo("name", name);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> item, ParseException e) {
                if (e == null) {
                    if (item.size() == 0) {

                    } else if (item.size() > 0) {
                        //kg = item_kg*1.0;
                        pricefetch = item.get(0).getDouble("price_kg");
                        //Log.d("price",item_kg+" "+price );
                    }
                } else {
                    Log.d("stockprice", "Error: " + e.getMessage());
                }

            }
        });

    }


}
