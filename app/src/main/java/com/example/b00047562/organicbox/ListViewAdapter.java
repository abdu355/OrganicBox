package com.example.b00047562.organicbox;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 12/20/2015.
 */
public class ListViewAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    ImageLoader imageLoader;
    private List<OrderBox> orderBoxList = null;
    private ArrayList<OrderBox> arraylist;

    public ListViewAdapter(Context context,
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
        TextView ordernum;
        TextView date;
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
            holder.ordernum = (TextView) view.findViewById(R.id.ordernum);
            holder.date = (TextView) view.findViewById(R.id.date);
            holder.type = (TextView)view.findViewById(R.id.type);
            // Locate the ImageView in listview_item.xml
            holder.image = (ImageView) view.findViewById(R.id.flag);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Set the results into TextViews
        holder.name.setText(orderBoxList.get(position).getName());
        holder.ordernum.setText(orderBoxList.get(position).getOrdernum());
        holder.date.setText(orderBoxList.get(position)
                .getDate());
        holder.type.setText(orderBoxList.get(position).getType());
        // Set the results into ImageView
        imageLoader.DisplayImage(orderBoxList.get(position).getImage(),
                holder.image);
        // Listen for ListView Item Click
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Send single item click data to SingleItemView Class
                Intent intent = new Intent(context, SingleItemView.class);
                // Pass all data rank
                intent.putExtra("name", (orderBoxList.get(position).getName()));
                // Pass all data country
                intent.putExtra("ordernum", (orderBoxList.get(position).getOrdernum()));
                // Pass all data population
                intent.putExtra("date", (orderBoxList.get(position).getDate()));
                // Start SingleItemView Class
                // Pass all data flag
                intent.putExtra("image", (orderBoxList.get(position).getImage()));
                intent.putExtra("status",orderBoxList.get(position).getStatus());
                // Start SingleItemView Class
                intent.putExtra("type",orderBoxList.get(position).getType());
                context.startActivity(intent);
            }
        });
        return view;
    }
}
//http://www.androidbegin.com/tutorial/android-parse-com-listview-images-and-texts-tutorial/