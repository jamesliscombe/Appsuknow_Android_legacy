package com.appsuknow.sfcsouthshields;

import android.annotation.SuppressLint;
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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;

public class MultiItemActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "MultiItemsActivity";
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<MultiItems> multiItems = new ArrayList<>();
    private String incomingItemID;
    BasketLogic basketLogic = new BasketLogic();
    NumberFormat format = NumberFormat.getCurrencyInstance();
    Toolbar toolbar;
    Button basketButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_item);
        Log.d(TAG, "onCreate started.");
        initRecyclerView();
        new GetMultiItems().execute();
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_multi_items);
        swipeRefreshLayout.setOnRefreshListener(this);

        toolbar = findViewById(R.id.toolbar_custom);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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
        bundle.putString("goto","MultiItemActivity");

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
        RecyclerView recyclerView = findViewById(R.id.recycler_View_multi_items);
        MultiRecyclerViewAdapter adapter = new MultiRecyclerViewAdapter(multiItems ,this, basketButton);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void getIncomingIntent() {
        Log.d(TAG,"Retrieving incoming intent extras.");
        if(getIntent().hasExtra("multiItemID")) {
            Log.d(TAG,"Found intent extras.");
            incomingItemID = getIntent().getStringExtra("multiItemID");
        }
    }

    @Override
    public void onRefresh() {
        multiItems.clear();
        new GetMultiItems().execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class GetMultiItems extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getIncomingIntent();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler httpHandler = new HttpHandler();

            String urlStr = Constants.baseURL+"category/items/multi/" + incomingItemID;
            Log.d(TAG,urlStr);
            URL url = null;
            URI uri;

            try {
                url = new URL(urlStr);
                uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
                url = uri.toURL();
            } catch (MalformedURLException | URISyntaxException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "URL = " + url);

            String itemsStr;

            if(url != null) {
                itemsStr = httpHandler.makeServiceCall(url.toString());
                Log.d(TAG, "Response from JSON: " + itemsStr);
            } else {
                itemsStr = null;
            }

            if (itemsStr != null) {
                try {
                    JSONArray itemJsonArr = new JSONArray(itemsStr);

                    for (int i = 0; i < itemJsonArr.length(); i++) {
                        JSONObject itemJsonObj = itemJsonArr.getJSONObject(i);
                        String multi_item_id = itemJsonObj.getString("multi_item_id");
                        String filling = itemJsonObj.getString("filling");
                        String multi_description = itemJsonObj.getString("multi_description");
                        String multi_price = itemJsonObj.getString("multi_price");
                        String item_name = itemJsonObj.getString("item_name");
                        String item_category = itemJsonObj.getString("item_category");
                        String item_id = itemJsonObj.getString("item_id");

                        MultiItems multiItem = new MultiItems();
                        multiItem.multi_item_id = multi_item_id;
                        multiItem.filling = filling;
                        multiItem.multi_description = multi_description;
                        multiItem.multi_price = multi_price;
                        multiItem.item_name = item_name;
                        multiItem.item_category = item_category;
                        multiItem.item_id = item_id;
                        multiItems.add(multiItem);
                    }


                } catch (JSONException e) {
                    Log.d(TAG,"Something went wrong parsing the multiItems JSON data - fatal");
                    finish();
                }
            } else {
                Log.d(TAG, "TO-DO Error communicating with server");
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
