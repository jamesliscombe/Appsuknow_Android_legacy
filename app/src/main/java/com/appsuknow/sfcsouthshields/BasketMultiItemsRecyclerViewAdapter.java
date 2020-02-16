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

public class BasketMultiItemsRecyclerViewAdapter extends RecyclerView.Adapter<BasketMultiItemsRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "BasketItemsRecycler";
    private Context context;
    private Intent intent;
    NumberFormat format = NumberFormat.getCurrencyInstance();

    private OnQuantityChangeListener onQuantityChangeListener;

    public BasketMultiItemsRecyclerViewAdapter(Context context, Intent intent, OnQuantityChangeListener onQuantityChangeListener) {
        this.intent = intent;
        this.context = context;
        this.onQuantityChangeListener = onQuantityChangeListener;
        format.setCurrency(Currency.getInstance("GBP"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View basketMultiItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.basket_multi_item_listitem,parent, false);
        ViewHolder basketMultiItemViewHolder = new ViewHolder(basketMultiItemView);
        return basketMultiItemViewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Log.d(TAG,"onBindViewHolder: called.");

        List keys = new ArrayList(BasketLogic.multiItems.keySet());

        Object value = null;

        for(int i = 0; i < keys.size(); i++) {
            value = keys.get(position);
        }

        final Object posValue = value;

        //holder.basketMultiItemNameTextView.setText(BasketLogic.multiItems.get(String.valueOf(value)).item_name + " " + BasketLogic.multiItems.get(String.valueOf(value)).filling);
        holder.basketMultiItemNameTextView.setText(context.getString(R.string.basketmultiitemsrecyclerviewadapter_multi_item,BasketLogic.multiItems.get(String.valueOf(value)).item_name,BasketLogic.multiItems.get(String.valueOf(value)).filling));

        holder.quantityPicker.setFilters(new InputFilter[]{new InputFilterMinMax("1","99")});
        holder.quantityPicker.setText(String.valueOf(BasketLogic.multiItems.get(String.valueOf(value)).quantity));

        holder.quantityPicker.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                MultiItemsBasket localUpdate = new MultiItemsBasket();

                localUpdate.filling = BasketLogic.multiItems.get(String.valueOf(posValue)).filling;
                localUpdate.multi_price = BasketLogic.multiItems.get(String.valueOf(posValue)).multi_price;
                localUpdate.multi_item_id = BasketLogic.multiItems.get(String.valueOf(posValue)).multi_item_id;
                localUpdate.item_name = BasketLogic.multiItems.get(String.valueOf(posValue)).item_name;

                int qty;

                if(holder.quantityPicker.length() == 0) {
                    localUpdate.quantity = 1;
                    qty = 1;
                } else {
                    localUpdate.quantity = Integer.valueOf(holder.quantityPicker.getText().toString());
                    qty = Integer.valueOf(holder.quantityPicker.getText().toString());
                }

                BasketLogic.multiItems.put(String.valueOf(posValue),localUpdate);

                onQuantityChangeListener.onQuantityChange(qty,0);

                double price = BasketLogic.multiItems.get(String.valueOf(posValue)).quantity * BasketLogic.multiItems.get(String.valueOf(posValue)).multi_price;

                holder.basketItemPriceTextView.setText(format.format(Double.parseDouble(String.valueOf(price))));
            }
        });

        double price = BasketLogic.multiItems.get(String.valueOf(posValue)).quantity * BasketLogic.multiItems.get(String.valueOf(posValue)).multi_price;

        holder.basketItemPriceTextView.setText(format.format(Double.parseDouble(String.valueOf(price))));
    }

    @Override
    public int getItemCount() {
        if(!BasketLogic.multiItems.isEmpty()) {
            return BasketLogic.multiItems.size();
        } else {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView basketMultiItemNameTextView;
        RelativeLayout basketMultiItemRelativeLayout;
        EditText quantityPicker;
        TextView basketItemPriceTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            basketMultiItemNameTextView = itemView.findViewById(R.id.text_view_basket_multi_item_name);
            basketMultiItemRelativeLayout = itemView.findViewById(R.id.relative_layout_basket_multi_item_cell);
            quantityPicker = itemView.findViewById(R.id.edit_text_multi_item_quantity);
            basketItemPriceTextView = itemView.findViewById(R.id.text_view_basket_multi_item_price);
        }
    }

    public void removeItem(int position) {

        List keys = new ArrayList(BasketLogic.multiItems.keySet());

        Object value = null;

        for(int i = 0; i < keys.size(); i++) {
            value = keys.get(position);
        }

        BasketLogic.multiItems.remove(value);
        notifyItemRemoved(position);
    }
}
