package com.appsuknow.sfcsouthshields;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;

public class BasketDealsRecyclerViewAdapter extends RecyclerView.Adapter<BasketDealsRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "BasketItemsRecycler";
    private Context context;
    private Intent intent;
    NumberFormat format = NumberFormat.getCurrencyInstance();

    public BasketDealsRecyclerViewAdapter(Context context, Intent intent) {
        this.intent = intent;
        this.context = context;
        format.setCurrency(Currency.getInstance("GBP"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View basketDealsView = LayoutInflater.from(parent.getContext()).inflate(R.layout.basket_deal_listitem,parent, false);
        ViewHolder basketDealsViewHolder = new ViewHolder(basketDealsView);
        return basketDealsViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG,"onBindViewHolder: called.");

        ArrayList<String> choices = new ArrayList<>();

        for(String choice: BasketLogic.deals.get(position).choices) {
            choices.add(choice);
        }

        String choicesLines = "";

        for (String s : choices)
        {
            choicesLines += s + "\n";
        }

        holder.basketDealTextView.setText(BasketLogic.deals.get(position).name);
        holder.basketDealPriceTextView.setText(String.valueOf(format.format(BasketLogic.deals.get(position).price)));
        holder.basketDealItemsTextView.setText(choicesLines);
    }

    @Override
    public int getItemCount() {
        if(!BasketLogic.deals.isEmpty()) {
            return BasketLogic.deals.size();
        } else {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView basketDealTextView;
        RelativeLayout basketDealRelativeLayout;
        TextView basketDealPriceTextView;
        TextView basketDealItemsTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            basketDealTextView = itemView.findViewById(R.id.text_view_basket_deal_name);
            basketDealRelativeLayout = itemView.findViewById(R.id.relative_layout_basket_deal_cell);
            basketDealPriceTextView = itemView.findViewById(R.id.text_view_basket_deal_price);
            basketDealItemsTextView = itemView.findViewById(R.id.text_view_basket_deal_items);
        }
    }

    public void removeItem(int position) {
        BasketLogic.deals.remove(position);
        notifyItemRemoved(position);
    }
}
