package com.appsuknow.sfcsouthshields;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;

public class DealsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "DealsActivity";
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<Deal> deals = new ArrayList<>();
    Button basketButton;
    BasketLogic basketLogic = new BasketLogic();
    NumberFormat format = NumberFormat.getCurrencyInstance();
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deals);
        Log.d(TAG,"onCreate: started.");
        new GetDeals().execute();
        toolbar = findViewById(R.id.toolbar_custom);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("DEALS");
        }
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_deals);
        swipeRefreshLayout.setOnRefreshListener(this);
        basketButton = findViewById(R.id.button_go_to_basket);

        if(toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.black),PorterDuff.Mode.SRC_ATOP);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        checkBasketButtonTitle();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        checkBasketButtonTitle();
    }

    public void checkBasketButtonTitle() {
        format.setCurrency(Currency.getInstance("GBP"));
        if(basketLogic.priceOfAllItems() == 0) {
            basketButton.setText("£0.00");
        } else {
            basketButton.setText(String.valueOf(format.format(basketLogic.priceOfAllItems())));
        }
    }

    public void launchBasket(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("goto","DealsActivity");

        if(basketLogic.countOfAllItems() == 0) {
            Intent intent = new Intent(this,EmptyBasketActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this,BasketActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    private void initRecyclerView() {
        Log.d(TAG,"initRecyclerView: started");
        RecyclerView recyclerView = findViewById(R.id.recycler_view_deals);
        DealRecyclerViewAdapter adapter = new DealRecyclerViewAdapter(deals ,this, basketButton);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onRefresh() {
        deals.clear();
        new GetDeals().execute();
    }

    private class GetDeals extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler httpHandler = new HttpHandler();
            String url = Constants.baseURL+"deals";
            String dealStr = httpHandler.makeServiceCall(url);

            Log.d(TAG,"Response from JSON: " + dealStr);

            if(dealStr != null) {
                try {
                    JSONArray dealJsonArr = new JSONArray(dealStr);

                    for(int i = 0; i < dealJsonArr.length(); i++) {
                        JSONObject dealJsonObject = dealJsonArr.getJSONObject(i);
                        String deal_id = dealJsonObject.getString("deal_id");
                        String name = dealJsonObject.getString("name");
                        String description = dealJsonObject.getString("description");
                        String num_sections = dealJsonObject.getString("num_sections");
                        String price = dealJsonObject.getString("price");

                        Deal deal = new Deal();
                        deal.deal_id = deal_id;
                        deal.name = name;
                        deal.description = description;
                        deal.num_sections = num_sections;
                        deal.price = price;
                        deals.add(deal);
                    }


                } catch (JSONException e) {

                }
            } else {
                Log.d(TAG,"TO-DO Error communicating with server");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            initRecyclerView();
            if(swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }
}
