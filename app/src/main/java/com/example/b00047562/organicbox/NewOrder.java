package com.example.b00047562.organicbox;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class NewOrder extends AppCompatActivity {

    ListView orders;
    List<ParseObject> ob;
    ProgressDialog mProgressDialog;
    NewOrderListAdapter adapter;
    private List<OrderBox> stockList = null;
    EditText searchbox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_order);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        searchbox = (EditText)findViewById(R.id.et_searchbox);

    }

    @Override
    protected void onResume() {
        super.onResume();
        new RemoteDataTask().execute();
    }

    //RemoteDataTask AsyncTask
    private class RemoteDataTask extends android.os.AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Create a progressdialog
            mProgressDialog = new ProgressDialog(NewOrder.this);
            // Set progressdialog title
            mProgressDialog.setTitle("Stock");
            // Set progressdialog message
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            // Show progressdialog
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            stockList = new ArrayList<OrderBox>();
            ParseQuery<ParseObject> query = new ParseQuery("Stock");
            query.orderByAscending("name");

            try {
                ob = query.find();
                for (ParseObject order : ob) {
                    // Locate images in flag column

                    ParseFile image= (ParseFile) order.get("image");
                    OrderBox map = new OrderBox();
                    map.setName((String) order.get("name"));
                    map.setType((String)order.get("type"));
                    map.setImage(image.getUrl());
                    stockList.add(map);
                }
            } catch (ParseException e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            orders = (ListView) findViewById(R.id.listview_neworder);
            // Pass the results into ListViewAdapter.java
            adapter = new NewOrderListAdapter(NewOrder.this,
                    stockList);
            // Binds the Adapter to the ListView
            if(adapter.getCount()!=0){
                orders.setAdapter(adapter);
            }else{

            }
            // Close the progressdialog
            mProgressDialog.dismiss();

        }
    }

}
