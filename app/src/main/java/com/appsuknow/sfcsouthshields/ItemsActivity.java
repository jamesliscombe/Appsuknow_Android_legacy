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
import android.widget.TextView;

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

public class ItemsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "ItemsActivity";
    private ArrayList<Items> items = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private String incomingCategoryDescription;
    private String incomingCategoryName;
    TextView description;
    Button basketButton;
    BasketLogic basketLogic = new BasketLogic();
    NumberFormat format = NumberFormat.getCurrencyInstance();
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);
        Log.d(TAG, "onCreate started.");
        description = findViewById(R.id.text_view_description);
        initRecyclerView();
        new GetItems().execute();
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_items);
        swipeRefreshLayout.setOnRefreshListener(this);

        if(incomingCategoryDescription.length() > 1) {
            description.setText(incomingCategoryDescription);
        } else {
            description.setVisibility(View.GONE);
        }

        toolbar = findViewById(R.id.toolbar_custom);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(incomingCategoryName);
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
        bundle.putString("goto","ItemsActivity");

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
        RecyclerView recyclerView = findViewById(R.id.recycler_view_items);
        ItemsRecyclerViewAdapter adapter = new ItemsRecyclerViewAdapter(items ,this, basketButton);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void getIncomingIntent() {
        Log.d(TAG,"Retrieving incoming intent extras.");
        if(getIntent().hasExtra("name") && getIntent().hasExtra("description")) {
            Log.d(TAG,"Found intent extras.");
            incomingCategoryName = getIntent().getStringExtra("name");
            incomingCategoryDescription = getIntent().getStringExtra("description");
        }
    }

    @Override
    public void onRefresh() {
        items.clear();
        new GetItems().execute();
    }

    private class GetItems extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getIncomingIntent();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler httpHandler = new HttpHandler();

            String urlStr = Constants.baseURL+"category/items/"+incomingCategoryName;
            URL url = null;
            URI uri;

            try {
                url = new URL(urlStr);
                uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
                url = uri.toURL();
            } catch (MalformedURLException | URISyntaxException e) {
                e.printStackTrace();
            }

            Log.d(TAG,"URL = "+ url);


            String itemsStr;

            if (url != null) {
                itemsStr = httpHandler.makeServiceCall(url.toString());
                Log.d(TAG,"Response from JSON: " + itemsStr);
            } else {
                itemsStr = null;
            }


            if(itemsStr != null) {
                try {
                    JSONArray itemJsonArr = new JSONArray(itemsStr);

                    for(int i = 0; i < itemJsonArr.length(); i++) {
                        JSONObject itemJsonObj = itemJsonArr.getJSONObject(i);
                        String name = itemJsonObj.getString("name");
                        String description = itemJsonObj.getString("description");
                        String category = itemJsonObj.getString("category");
                        String item_id = itemJsonObj.getString("item_id");
                        String multi = itemJsonObj.getString("multi");
                        String price = itemJsonObj.getString("price");

                        Items item = new Items();
                        item.name = name;
                        item.description = description;
                        item.category = category;
                        item.item_id = item_id;
                        item.multi = multi;
                        item.price = price;
                        items.add(item);
                    }
                } catch (JSONException e) {
                    Log.d(TAG,"JSON Exception: " + e);
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
