package com.example.b00047562.organicbox;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class SingleItemView extends AppCompatActivity {

    TextView txtname,txttype,txtdate,txtstat,txtorderid;
    String name,type,date,status,order_id;
    String image;
    String position;
    ImageLoader imageLoader = new ImageLoader(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_item_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Retrieve data from MainActivity on item click event
        Intent i = getIntent();

        // Get the name
        name = i.getStringExtra("name");
        type=i.getStringExtra("type");
        if(type.equals("pack"))
            type="pack x12";
        date=i.getStringExtra("date");
        status=i.getStringExtra("status");
        order_id=i.getStringExtra("ordernum");
        image=i.getStringExtra("image");

        // Locate the TextView in singleitemview.xml
        txtname = (TextView) findViewById(R.id.name);
        txttype= (TextView) findViewById(R.id.type);
        txtdate= (TextView) findViewById(R.id.date);
        txtstat= (TextView) findViewById(R.id.status);
        txtorderid=(TextView)findViewById(R.id.ordernum);

        ImageView imgflag = (ImageView) findViewById(R.id.flag);

        // Load the text into the TextView
        txtname.setText("Name: "+name);
        txttype.setText("Type: "+type);
        txtdate.setText("Date: "+date);
        txtstat.setText("Status: "+status);
        txtorderid.setText("Order No.: "+order_id);

        imageLoader.DisplayImage(image, imgflag);
    }

}
