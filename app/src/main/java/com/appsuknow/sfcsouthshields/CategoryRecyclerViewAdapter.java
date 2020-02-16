package com.appsuknow.sfcsouthshields;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class CategoryRecyclerViewAdapter extends RecyclerView.Adapter<CategoryRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";
    private ArrayList<Category> categories;
    private Context context;

    public CategoryRecyclerViewAdapter(ArrayList<Category> categories, Context context) {
        this.categories = categories;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View categoryView = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_listitem,parent, false);
        ViewHolder categoryViewHolder = new ViewHolder(categoryView);
        return categoryViewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG,"onBindViewHolder: called.");

        if(holder.getAdapterPosition() == 0) {
            holder.categoryNameTextView.setText(context.getString(R.string.categoryrecyclerviewadapter_deals));

            holder.categoryRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG,"Deal/Offer clicked");
                    Intent intent = new Intent(context,DealsActivity.class);
                    context.startActivity(intent);
                }
            });
        } else {
            holder.categoryNameTextView.setText(categories.get(position -1).name);

            holder.categoryRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: clicked on: " + categories.get(position - 1));
                    Bundle bundle = new Bundle();
                    bundle.putString("goto","CategoriesActivity");
                    Intent intent = new Intent(context,ItemsActivity.class);
                    intent.putExtra("name",categories.get(position - 1).name);
                    intent.putExtra("description",categories.get(position - 1).description);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return categories.size() + 1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameTextView;
        RelativeLayout categoryRelativeLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            categoryNameTextView = itemView.findViewById(R.id.text_view_category_name);
            categoryRelativeLayout = itemView.findViewById(R.id.relative_layout_category_cell);
        }
    }
}

