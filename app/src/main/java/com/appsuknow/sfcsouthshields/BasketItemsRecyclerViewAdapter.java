package com.appsuknow.sfcsouthshields;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

public class BasketItemsRecyclerViewAdapter extends RecyclerView.Adapter<BasketItemsRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "BasketItemsRecycler";
    private Context context;
    private Intent intent;
    NumberFormat format = NumberFormat.getCurrencyInstance();

    private OnQuantityChangeListener onQuantityChangeListener;

    public BasketItemsRecyclerViewAdapter(Context context, Intent intent, OnQuantityChangeListener onQuantityChangeListener) {
        this.context = context;
        this.intent = intent;
        this.onQuantityChangeListener = onQuantityChangeListener;
        format.setCurrency(Currency.getInstance("GBP"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View basketItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.basket_item_listitem,parent, false);
        ViewHolder basketItemViewHolder = new ViewHolder(basketItemView);
        return basketItemViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Log.d(TAG,"onBindViewHolder: called.");

        List keys = new ArrayList(BasketLogic.singleItems.keySet());

        Object value = null;

        for(int i = 0; i < keys.size(); i++) {
            value = keys.get(position);
        }

        final Object posValue = value;

        holder.basketItemNameTextView.setText(BasketLogic.singleItems.get(String.valueOf(value)).name);

        holder.quantityPicker.setFilters(new InputFilter[]{new InputFilterMinMax("1","99")});
        holder.quantityPicker.setText(String.valueOf(BasketLogic.singleItems.get(String.valueOf(value)).quantity));

        holder.quantityPicker.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                ItemsBasket localUpdate = new ItemsBasket();

                localUpdate.name = BasketLogic.singleItems.get(String.valueOf(posValue)).name;
                localUpdate.item_price = BasketLogic.singleItems.get(String.valueOf(posValue)).item_price;
                localUpdate.item_id = BasketLogic.singleItems.get(String.valueOf(posValue)).item_id;

                int qty;

                if(holder.quantityPicker.length() == 0) {
                    localUpdate.quantity = 1;
                    qty = 1;
                } else {
                    localUpdate.quantity = Integer.valueOf(holder.quantityPicker.getText().toString());
                    qty = Integer.valueOf(holder.quantityPicker.getText().toString());
                }

                BasketLogic.singleItems.put(String.valueOf(posValue),localUpdate);

                onQuantityChangeListener.onQuantityChange(qty,0);

                double price = BasketLogic.singleItems.get(String.valueOf(posValue)).quantity * BasketLogic.singleItems.get(String.valueOf(posValue)).item_price;

                holder.basketItemPriceTextView.setText(format.format(Double.parseDouble(String.valueOf(price))));
            }
        });

        double price = BasketLogic.singleItems.get(String.valueOf(value)).quantity * BasketLogic.singleItems.get(String.valueOf(value)).item_price;

        holder.basketItemPriceTextView.setText(format.format(Double.parseDouble(String.valueOf(price))));
    }

    @Override
    public int getItemCount() {
        if(!BasketLogic.singleItems.isEmpty()) {
            return BasketLogic.singleItems.size();
        } else {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView basketItemNameTextView;
        RelativeLayout basketItemRelativeLayout;
        EditText quantityPicker;
        TextView basketItemPriceTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            basketItemNameTextView = itemView.findViewById(R.id.text_view_basket_item_name);
            basketItemRelativeLayout = itemView.findViewById(R.id.relative_layout_basket_item_cell);
            quantityPicker = itemView.findViewById(R.id.edit_text_item_quantity);
            basketItemPriceTextView = itemView.findViewById(R.id.text_view_basket_item_price);
        }
    }

    public void removeItem(int position) {

        List keys = new ArrayList(BasketLogic.singleItems.keySet());

        Object value = null;

        for(int i = 0; i < keys.size(); i++) {
            value = keys.get(position);
        }

        BasketLogic.singleItems.remove(value);
        notifyItemRemoved(position);
    }
}