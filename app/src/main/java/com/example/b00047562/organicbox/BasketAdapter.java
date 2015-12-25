package com.example.b00047562.organicbox;

/**
 * Created by Administrator on 12/22/2015.
 */

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class BasketAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    ImageLoader imageLoader;
    private List<OrderBox> orderBoxList = null;
    private ArrayList<OrderBox> arraylist;

    public BasketAdapter(Context context,
                         List<OrderBox> orderBoxList) {
        this.context = context;
        this.orderBoxList = orderBoxList;
        inflater = LayoutInflater.from(context);
        this.arraylist = new ArrayList<OrderBox>();
        this.arraylist.addAll(orderBoxList);
        imageLoader = new ImageLoader(context);
    }

    public class ViewHolder {
        TextView name;
        TextView type;
        ImageView image;
    }

    @Override
    public int getCount() {
        return orderBoxList.size();
    }

    @Override
    public Object getItem(int position) {
        return orderBoxList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.listview_item, null);
            // Locate the TextViews in listview_item.xml
            holder.name = (TextView) view.findViewById(R.id.name);
            holder.type = (TextView) view.findViewById(R.id.type);
            // Locate the ImageView in listview_item.xml
            holder.image = (ImageView) view.findViewById(R.id.flag);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Set the results into TextViews
        holder.name.setText(orderBoxList.get(position).getName());
        holder.type.setText(orderBoxList.get(position).getType());
        // Set the results into ImageView
        imageLoader.DisplayImage(orderBoxList.get(position).getImage(), holder.image);
        // Listen for ListView Item Click
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View arg0) {
                //clear item clicked
                new AlertDialog.Builder(arg0.getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Clear Item")
                        .setMessage("Are you sure you want to remove this item?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //clear item
                                ParseQuery<ParseObject> querybasket = ParseQuery.getQuery("Basket");
                                querybasket.whereEqualTo("createdBy", ParseUser.getCurrentUser());
                                querybasket.orderByDescending("createdAt");
                                querybasket.findInBackground(new FindCallback<ParseObject>() {
                                    public void done(List<ParseObject> basketlist, ParseException e) {
                                        if (e == null) {
                                            try {
                                                basketlist.get(position).delete();
                                                //TODO add refresh
                                            } catch (ParseException e1) {
                                                Toast.makeText(arg0.getContext(), "Item Clear Failed", Toast.LENGTH_SHORT).show();
                                                Log.d("ParseBasket", e1.getMessage());
                                            }

                                        } else {

                                        }
                                    }
                                });
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
        return view;
    }
}
//http://www.androidbegin.com/tutorial/android-parse-com-listview-images-and-texts-tutorial/
