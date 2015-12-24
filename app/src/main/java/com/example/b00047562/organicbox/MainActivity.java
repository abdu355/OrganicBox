package com.example.b00047562.organicbox;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    ListView orders;
    List<ParseObject> ob;
    ProgressDialog mProgressDialog;
    ListViewAdapter adapter;
    TextView userview;
    private List<OrderBox> orderBoxList = null;
    final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    private ImageView mapbtn,basketbtn;

//hello
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundColor(Color.parseColor("#e8f5e9"));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getApplicationContext(), NewOrder.class));
            }
        });
        ParseUser currentUser = ParseUser.getCurrentUser();//check if user logged in
        if (currentUser == null) {
            loadLoginView();
        }

        userview = (TextView)findViewById(R.id.userview_main);
        orders =(ListView)findViewById(R.id.lv_orders);
        mapbtn=(ImageView)findViewById(R.id.map_btn);
        basketbtn=(ImageView)findViewById(R.id.basket_btn);

        userview.setText(currentUser.getUsername()+"'s List of Orders");
        mapbtn.setOnClickListener(this);
        basketbtn.setOnClickListener(this);


    }
    public void loadLoginView() {
        Intent intent = new Intent(this, Login.class); //go to login activity
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if(id==R.id.action_logout)
        {
            ParseUser.logOut();//update Parse current user
            loadLoginView();//load login activity
        }
        if(id==R.id.action_wishlist)
        {

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new RemoteDataTask().execute();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.map_btn:
                startActivity(new Intent(this,MapsActivity.class));
                break;
            case R.id.basket_btn:
                startActivity(new Intent(this,MyBasket.class));
                break;
        }
    }

    //RemoteDataTask AsyncTask
    private class RemoteDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Create a progressdialog
            mProgressDialog = new ProgressDialog(MainActivity.this);
            // Set progressdialog title
            mProgressDialog.setTitle("Orders");
            // Set progressdialog message
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            // Show progressdialog
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            orderBoxList = new ArrayList<OrderBox>();
            ParseUser usr = ParseUser.getCurrentUser();
            // Locate the class table named "Country" in Parse.com
            ParseQuery<ParseObject> query = new ParseQuery("Orders");
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
                    map.setOrdernum(order.getObjectId());
                    map.setDate(formatter.format(order.getCreatedAt()).toString());
                    map.setImage(image.getUrl());
                    map.setType((String)order.get("type"));
                    map.setStatus((String)order.get("tracker_status"));
                    orderBoxList.add(map);
                }
            } catch (ParseException |NullPointerException e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            orders = (ListView) findViewById(R.id.lv_orders);
            // Pass the results into ListViewAdapter.java
            adapter = new ListViewAdapter(MainActivity.this,
                    orderBoxList);
            // Binds the Adapter to the ListView
            if(adapter.getCount()!=0){
                orders.setAdapter(adapter);
            }else{
                Toast.makeText(MainActivity.this, "No Items Available", Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, "Click to Add new Order --->", Toast.LENGTH_SHORT).show();
            }
            // Close the progressdialog
            mProgressDialog.dismiss();

        }
    }
}
