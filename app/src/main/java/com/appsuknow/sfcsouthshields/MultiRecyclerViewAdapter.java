package com.appsuknow.sfcsouthshields;

import android.content.Context;
import android.content.DialogInterface;
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

public class MultiRecyclerViewAdapter extends RecyclerView.Adapter<MultiRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "MultiRecyclerViewAdpter";

    private ArrayList<MultiItems> multiItems;
    private Context context;
    private Button basketButton;
    NumberFormat format = NumberFormat.getCurrencyInstance();
    BasketLogic basketLogic = new BasketLogic();

    public MultiRecyclerViewAdapter(ArrayList<MultiItems> multiItems, Context context, Button basketButton) {
        this.multiItems = multiItems;
        this.context = context;
        this.basketButton = basketButton;
        format.setCurrency(Currency.getInstance("GBP"));
    }

    @NonNull
    @Override
    public MultiRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.multi_item_listitem,parent, false);
        MultiRecyclerViewAdapter.ViewHolder viewHolder = new MultiRecyclerViewAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MultiRecyclerViewAdapter.ViewHolder holder, final int position) {
        Log.d(TAG,"onBindViewHolder: called.");

        //Get our cell height on device
        final float scale = context.getResources().getDisplayMetrics().density;
        int px = (int) (44 * scale + 0.5f);

        holder.multiItemName.setText(context.getString(R.string.multirecyclerviewadapter_filling_name,multiItems.get(position).filling,multiItems.get(position).item_name));

        if(multiItems.get(position).multi_description.length() > 0) {
            holder.multiItemDescription.setText(multiItems.get(position).multi_description);
        } else {
            holder.itemLayout.getLayoutParams().height = px;
        }

        holder.multiItemPrice.setText(format.format(Double.parseDouble(multiItems.get(position).multi_price)));

        holder.addToBasket.setOnClickListener(new View.OnClickListener() {
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

                MultiItemsBasket localItem = new MultiItemsBasket();
                localItem.multi_item_id = multiItems.get(position).multi_item_id;
                localItem.filling = multiItems.get(position).filling;
                localItem.item_name = multiItems.get(position).item_name;
                localItem.multi_price = Double.parseDouble(multiItems.get(position).multi_price);

                if(BasketLogic.multiItems.containsKey(multiItems.get(position).multi_item_id)) {
                    localItem.quantity = BasketLogic.multiItems.get(multiItems.get(position).multi_item_id).quantity + 1;
                    BasketLogic.multiItems.put(multiItems.get(position).multi_item_id,localItem);
                } else {
                    localItem.quantity = 1;
                    BasketLogic.multiItems.put(multiItems.get(position).multi_item_id,localItem);
                }
                checkBasketButtonTitle();
                //Toast.makeText(context,"Item added to basket", Toast.LENGTH_SHORT).show();
                builder1.setMessage("Item added to basket");
                AlertDialog alert11 = builder1.create();
                alert11.show();
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
        return multiItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView multiItemName;
        TextView multiItemPrice;
        TextView multiItemDescription;
        RelativeLayout itemLayout;
        Button addToBasket;

        public ViewHolder(View itemView) {
            super(itemView);

            multiItemName = itemView.findViewById(R.id.text_view_multi_item_name);
            itemLayout = itemView.findViewById(R.id.relative_layout_multi_item_cell);
            addToBasket = itemView.findViewById(R.id.button_add_multi_item_to_basket);
            multiItemPrice = itemView.findViewById(R.id.text_view_multi_item_price);
            multiItemDescription = itemView.findViewById(R.id.text_view_multi_item_description);
        }
    }
}
