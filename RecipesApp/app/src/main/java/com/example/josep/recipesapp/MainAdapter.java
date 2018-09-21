package com.example.josep.recipesapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by josep on 2/9/2018.
 * Much of the code in this project was taken from StackOverflow and developer.android.com.
 */

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.mainViewHolder> {
    private Context context;
    private JSONArray json;


    public MainAdapter(Context context, JSONArray json) {
        this.context = context;
        this.json = json;
    }

    @Override
    public mainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recipe_item, parent, false);
        return new mainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(mainViewHolder holder, int position) {
        //https://stackoverflow.com/questions/1568762/accessing-members-of-items-in-a-jsonarray-with-java
        try {
            JSONObject recipe = json.getJSONObject(position);
            holder.title.setText(recipe.getString("title"));
            holder.view.setTag(recipe);
        } catch(Exception e){
            //do nothing
        }
    }

    @Override
    public int getItemCount() {
        return json.length();
    }

    class mainViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView view;

        public mainViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.Title);
            view = itemView.findViewById(R.id.view);
        }
    }
}

