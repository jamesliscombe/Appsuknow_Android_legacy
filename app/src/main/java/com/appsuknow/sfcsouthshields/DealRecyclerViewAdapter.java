package com.appsuknow.sfcsouthshields;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;

public class DealRecyclerViewAdapter extends RecyclerView.Adapter<DealRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "DealRecyclerView";
    private ArrayList<Deal> deals;
    private Context context;
    Button basketButton;
    NumberFormat format = NumberFormat.getCurrencyInstance();
    BasketLogic basketLogic = new BasketLogic();

    public DealRecyclerViewAdapter(ArrayList<Deal> deals, Context context, Button basketButton) {
        this.deals = deals;
        this.context = context;
        this.basketButton = basketButton;
    }

    @NonNull
    @Override
    public DealRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View dealView = LayoutInflater.from(parent.getContext()).inflate(R.layout.deal_listitem,parent, false);
        DealRecyclerViewAdapter.ViewHolder dealViewHolder = new DealRecyclerViewAdapter.ViewHolder(dealView);
        return dealViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DealRecyclerViewAdapter.ViewHolder holder, final int position) {
        Log.d(TAG,"onBindViewHolder: called.");

        //Get our cell height on device
        final float scale = context.getResources().getDisplayMetrics().density;
        int px = (int) (44 * scale + 0.5f);

        holder.dealNameTextView.setText(deals.get(position).name);

        holder.dealPrice.setText(format.format(Double.parseDouble(deals.get(position).price)));

        if(deals.get(position).description.length() > 0) {
            holder.dealDescription.setText(deals.get(position).description);

        } else {
            LayoutParams lpName = (LayoutParams) holder.dealNameTextView.getLayoutParams();
            LayoutParams lpPrice = (LayoutParams) holder.dealPrice.getLayoutParams();

            holder.dealRelativeLayout.getLayoutParams().height = px;
            holder.dealNameTextView.setGravity(Gravity.CENTER_VERTICAL);

            lpName.addRule(RelativeLayout.CENTER_VERTICAL);
            lpPrice.addRule(RelativeLayout.CENTER_VERTICAL);
        }

        holder.dealRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"Deal");
                Bundle bundle = new Bundle();
                bundle.putString("goto","DealActivity");
                Intent intent = new Intent(context,ChoicesParentActivity.class);
                intent.putExtra("name",deals.get(position).name);
                intent.putExtra("id",deals.get(position).deal_id);
                intent.putExtra("description",deals.get(position).description);
                intent.putExtra("num_sections",deals.get(position).num_sections);
                intent.putExtra("dealPrice",deals.get(position).price);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return deals.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView dealNameTextView;
        RelativeLayout dealRelativeLayout;
        TextView dealPrice;
        TextView dealDescription;

        public ViewHolder(View itemView) {
            super(itemView);

            dealNameTextView = itemView.findViewById(R.id.text_view_deal_name);
            dealRelativeLayout = itemView.findViewById(R.id.relative_layout_deal_cell);
            dealPrice = itemView.findViewById(R.id.text_view_deal_price);
            dealDescription = itemView.findViewById(R.id.text_view_deal_description);
        }
    }
}