package com.appsuknow.sfcsouthshields;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.Currency;

public class BasketActivity extends AppCompatActivity implements OnQuantityChangeListener {

    private static final String TAG = "BasketActivity";
    boolean deliverySelected = true;
    BasketLogic basketLogic = new BasketLogic();
    NumberFormat format = NumberFormat.getCurrencyInstance();
    Button checkoutButton;
    RadioButton deliveryRadioButton;
    RadioButton collectionRadioButton;
    TextView totalPriceTopTextView;
    TextView totalItemsTextView;
    TextView minOrderNotReached;
    TextView subtotalLabelBottom;
    TextView subtotalPriceBottom;
    TextView deliveryLabelBottom;
    TextView deliveryPriceBottom;
    TextView totalLabelBottom;
    TextView totalPriceBottom;
    TextView averageTime;
    RecyclerView itemsRecyclerView;
    RecyclerView multiItemsRecyclerView;
    RecyclerView dealsRecyclerView;
    RelativeLayout dietaryReqs;
    BasketItemsRecyclerViewAdapter itemsAdapter = new BasketItemsRecyclerViewAdapter(this,this.getIntent(), this);
    BasketMultiItemsRecyclerViewAdapter multiItemsAdapter = new BasketMultiItemsRecyclerViewAdapter(this, this.getIntent(), this);
    BasketDealsRecyclerViewAdapter dealsAdapter = new BasketDealsRecyclerViewAdapter(this, this.getIntent());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket);

        Toolbar toolbar = findViewById(R.id.toolbar_custom_basket);
        checkoutButton = findViewById(R.id.button_go_to_checkout);
        deliveryRadioButton = findViewById(R.id.radio_button_delivery);
        collectionRadioButton = findViewById(R.id.radio_button_collection);
        totalPriceTopTextView = findViewById(R.id.text_view_total_price_basket);
        totalItemsTextView = findViewById(R.id.text_view_total_items_basket);
        minOrderNotReached = findViewById(R.id.text_view_min_order_message);
        dietaryReqs = findViewById(R.id.relative_layout_dietary_requirement);
        subtotalLabelBottom = findViewById(R.id.text_view_subtotal_label_bottom);
        subtotalPriceBottom = findViewById(R.id.text_view_subtotal_price_bottom);
        deliveryLabelBottom = findViewById(R.id.text_view_delivery_label_bottom);
        deliveryPriceBottom = findViewById(R.id.text_view_delivery_price_bottom);
        totalLabelBottom = findViewById(R.id.text_view_total_label_bottom);
        totalPriceBottom = findViewById(R.id.text_view_total_price_bottom);
        averageTime = findViewById(R.id.text_view_average_time);

        averageTime.setText(getString(R.string.average_delivery_time,Constants.average_delivery));

        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        deliveryRadioButton.toggle();
        format.setCurrency(Currency.getInstance("GBP"));

        setPrices();
        checkPriceForDelivery();
        initRecyclerView();
        swipeToDeleteItems();
        swipeToDeleteMultiItems();
        swipeToDeleteDeals();
    }

    public void checkPriceForDelivery() {
        if(this.deliverySelected) {
            if(basketLogic.priceOfAllItems() < Double.parseDouble(BasketLogic.minOrder)) {
                this.checkoutButton.setEnabled(false);
                this.checkoutButton.setBackgroundColor(getResources().getColor(R.color.blueDisabled));
                this.checkoutButton.setTextColor(getResources().getColor(R.color.whiteDisabled));
                this.minOrderNotReached.setVisibility(View.VISIBLE);
            } else {
                this.checkoutButton.setEnabled(true);
                this.minOrderNotReached.setVisibility(View.GONE);
                this.checkoutButton.setEnabled(true);
                this.checkoutButton.setBackgroundColor(getResources().getColor(R.color.blue));
                this.checkoutButton.setTextColor(getResources().getColor(R.color.white));
            }
        } else {
            this.checkoutButton.setEnabled(true);
            this.minOrderNotReached.setVisibility(View.GONE);
            this.checkoutButton.setEnabled(true);
            this.checkoutButton.setBackgroundColor(getResources().getColor(R.color.blue));
            this.checkoutButton.setTextColor(getResources().getColor(R.color.white));
        }
    }

    public void openDietary(View view) {
        Log.d(TAG,"Dietary info cell pressed");
        Bundle bundle = new Bundle();
        bundle.putString("goto","BasketActivity");
        Intent intent = new Intent(this,DietaryRequirementsActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void setPrices() {
        if(basketLogic.countOfAllItems() == 1) {
            totalItemsTextView.setText(getString(R.string.basket_activity_set_prices_single_item, basketLogic.countOfAllItems()));
        } else {
            totalItemsTextView.setText(getString(R.string.basket_activity_set_prices_single_items, basketLogic.countOfAllItems()));
        }

        if(this.deliverySelected) {
            totalPriceTopTextView.setText(format.format(basketLogic.priceOfAllItems() + Double.valueOf(BasketLogic.deliveryCharge)));
            deliveryLabelBottom.setVisibility(View.VISIBLE);
            deliveryPriceBottom.setVisibility(View.VISIBLE);
            deliveryPriceBottom.setText(format.format(Double.parseDouble(BasketLogic.deliveryCharge)));
            totalPriceBottom.setText(format.format(Double.parseDouble(BasketLogic.deliveryCharge) + basketLogic.priceOfAllItems()));
        } else {
            totalPriceTopTextView.setText(format.format(Double.valueOf(basketLogic.priceOfAllItems())));
            deliveryLabelBottom.setVisibility(View.GONE);
            deliveryPriceBottom.setVisibility(View.GONE);
            totalPriceBottom.setText(format.format(basketLogic.priceOfAllItems()));
        }

        subtotalPriceBottom.setText(format.format(Double.valueOf(basketLogic.priceOfAllItems())));
    }

    public void goToCheckout(View view) {
        Log.d(TAG,"Go to checkout");
        Bundle bundle = new Bundle();
        bundle.putString("goto","BasketActivity");
        Intent intent = new Intent(this,NoteTimeActivity.class);
        intent.putExtra("deliverySelected",this.deliverySelected);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void radioButtonDeliveryClicked(View view) {
        Log.d(TAG,"Delivery selected");
        this.deliverySelected = true;
        averageTime.setText(getString(R.string.average_delivery_time,Constants.average_delivery));
        setPrices();
        checkPriceForDelivery();
    }

    public void radioButtonCollectionClicked(View view) {
        Log.d(TAG,"Collection selected");
        this.deliverySelected = false;
        averageTime.setText(getString(R.string.average_collection_time,Constants.average_collection));
        setPrices();
        checkPriceForDelivery();
    }

    private void initRecyclerView() {
        Log.d(TAG,"initRecyclerView: started");
        //Items
        itemsRecyclerView = findViewById(R.id.recycler_view_basket_items);
        itemsRecyclerView.setAdapter(itemsAdapter);
        itemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Multi Items
        multiItemsRecyclerView = findViewById(R.id.recycler_view_basket_multi_items);
        multiItemsRecyclerView.setAdapter(multiItemsAdapter);
        multiItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Deals
        dealsRecyclerView = findViewById(R.id.recycler_view_basket_deals);
        dealsRecyclerView.setAdapter(dealsAdapter);
        dealsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void swipeToDeleteItems() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                final int position = viewHolder.getAdapterPosition();

                itemsAdapter.removeItem(position);

                if (basketLogic.countOfAllItems() == 0) {
                    Intent intent = new Intent(this.mContext, EmptyBasketActivity.class);
                    startActivity(intent);
                } else {
                    this.mContext.startActivity(getIntent());
                }
            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(itemsRecyclerView);
    }

    private void swipeToDeleteMultiItems() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                final int position = viewHolder.getAdapterPosition();

                multiItemsAdapter.removeItem(position);

                if (basketLogic.countOfAllItems() == 0) {
                    Intent intent = new Intent(this.mContext, EmptyBasketActivity.class);
                    startActivity(intent);
                } else {
                    this.mContext.startActivity(getIntent());
                }
            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(multiItemsRecyclerView);
    }

    private void swipeToDeleteDeals() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                Log.d(TAG,"CALLED onSwipe");
                final int position = viewHolder.getAdapterPosition();

                dealsAdapter.removeItem(position);

                if (basketLogic.countOfAllItems() == 0) {
                    Intent intent = new Intent(this.mContext, EmptyBasketActivity.class);
                    startActivity(intent);
                } else {
                    this.mContext.startActivity(getIntent());
                }
            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(dealsRecyclerView);
    }

    @Override
    public void onQuantityChange(int quantity, int section) {
        setPrices();
        checkPriceForDelivery();
    }
}
