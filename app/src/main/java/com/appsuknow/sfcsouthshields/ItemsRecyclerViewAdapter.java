package com.appsuknow.sfcsouthshields;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;

public class ItemsRecyclerViewAdapter extends RecyclerView.Adapter<ItemsRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<Items> items;
    private Context context;
    Button basketButton;
    NumberFormat format = NumberFormat.getCurrencyInstance();
    BasketLogic basketLogic = new BasketLogic();

    public ItemsRecyclerViewAdapter(ArrayList<Items> items, Context context, Button basketButton) {
        this.items = items;
        this.context = context;
        this.basketButton = basketButton;
        format.setCurrency(Currency.getInstance("GBP"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_listitem,parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Log.d(TAG,"onBindViewHolder: called.");

        //Get our cell height on device
        final float scale = context.getResources().getDisplayMetrics().density;
        int px = (int) (44 * scale + 0.5f);

        holder.itemNameTextView.setText(items.get(position).name);

        if(items.get(position).multi.equals("1")) {
            holder.itemPriceTextView.setText(context.getString(R.string.itemsrecyclerviewadapter_from_price,items.get(position).price));
            holder.cell.getLayoutParams().height = px;
        } else {
            holder.itemPriceTextView.setText(format.format(Double.parseDouble(items.get(position).price)));
            if(items.get(position).description.length() > 0) {
                holder.itemDescriptionTextView.setText(items.get(position).description);
            } else {
                holder.cell.getLayoutParams().height = px;
            }
        }


        holder.addItemToBasketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"Button pressed");
                AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                if(items.get(position).multi.equals("1")) {
                    Log.d(TAG,"Multi item");
                    Bundle bundle = new Bundle();
                    bundle.putString("goto","ItemsActivity");
                    Intent intent = new Intent(context,MultiItemActivity.class);
                    intent.putExtra("multiItemID",items.get(position).item_id);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                } else {
                    Log.d(TAG,"Single item");

                    ItemsBasket localItem = new ItemsBasket();
                    localItem.item_id = items.get(position).item_id;
                    localItem.item_price = Double.parseDouble(items.get(position).price);
                    localItem.name = items.get(position).name;

                    if(BasketLogic.singleItems.containsKey(items.get(position).item_id)) {
                        Log.d(TAG,"Item exists, adding to quantity");
                        localItem.quantity = BasketLogic.singleItems.get(items.get(position).item_id).quantity + 1;
                        BasketLogic.singleItems.put(items.get(position).item_id,localItem);
                    } else {
                        Log.d(TAG,"New Single item added");
                        localItem.quantity = 1;
                        BasketLogic.singleItems.put(items.get(position).item_id,localItem);
                    }
                    checkBasketButtonTitle();
                    //Toast.makeText(context,"Item added to basket", Toast.LENGTH_SHORT).show();
                    builder1.setMessage("Item added to basket");
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                    Log.d(TAG,String.valueOf(BasketLogic.singleItems.size()));
                }
            }
        });
    }

    public void checkBasketButtonTitle() {
        format.setCurrency(Currency.getInstance("GBP"));
        if(basketLogic.priceOfAllItems() == 0) {
            basketButton.setText(context.getString(R.string.generic_basket_button_title));
        } else {
            basketButton.setText(String.valueOf(format.format(basketLogic.priceOfAllItems())));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView itemNameTextView;
        TextView itemPriceTextView;
        TextView itemDescriptionTextView;
        Button addItemToBasketButton;
        RelativeLayout cell;

        public ViewHolder(View itemView) {
            super(itemView);

            cell = itemView.findViewById(R.id.relative_layout_item_cell);
            itemNameTextView = itemView.findViewById(R.id.text_view_item_name);
            itemPriceTextView = itemView.findViewById(R.id.text_view_item_price);
            itemDescriptionTextView = itemView.findViewById(R.id.text_view_item_description);
            addItemToBasketButton = itemView.findViewById(R.id.button_add_item_to_basket);
        }
    }
}
