package com.appsuknow.sfcsouthshields;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;

public class CategoriesActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "CategoriesActivity";
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<Category> categories = new ArrayList<>();
    private ArrayList<DeliveryCostSettings> deliveryCostSettings = new ArrayList<>();
    private ArrayList<AverageTimes> averageTimes = new ArrayList<>();
    private ArrayList<PublishableKey> keys = new ArrayList<>();
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    InternetConnected internetConnected = new InternetConnected();
    TextView deliveryCostTextView;
    TextView minimumOrderTextView;
    Button basketButton;
    BasketLogic basketLogic = new BasketLogic();
    NumberFormat format = NumberFormat.getCurrencyInstance();
    AlertDialog.Builder noInternetBuilder;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        noInternetBuilder = new AlertDialog.Builder(this);
        setContentView(R.layout.activity_categories);
        //Check internet connection
        if(!internetConnected.InternetConnectedCheck(this)) {
            Log.d(TAG,"internet is false");
            noInternetBuilder.setCancelable(false);

            noInternetBuilder.setPositiveButton(
                    "Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            finish();
                        }
                    });

            noInternetBuilder.setMessage("No internet connection. Please check your network settings and try again.");
            AlertDialog alert12 = noInternetBuilder.create();
            alert12.show();
        } else {
            Log.d(TAG,"internet is true");

            Toolbar toolbar = findViewById(R.id.toolbar_custom_categories);
            setSupportActionBar(toolbar);
            mDrawerLayout = findViewById(R.id.settings_menu);
            mToggle = new ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
            mDrawerLayout.addDrawerListener(mToggle);
            mToggle.syncState();
            setNavigationViewListner();
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("SFC South Shields");
            format.setCurrency(Currency.getInstance("GBP"));
            deliveryCostTextView = findViewById(R.id.text_view_delivery_cost);
            minimumOrderTextView = findViewById(R.id.text_view_minimum_order);
            Log.d(TAG,"onCreate: started.");
            new GetCategories().execute();
            new GetDeliveryCosts().execute();
            new GetAverageTimes().execute();
            swipeRefreshLayout = findViewById(R.id.swipe_refresh_categories);
            swipeRefreshLayout.setOnRefreshListener(this);
            basketButton = findViewById(R.id.button_go_to_basket);
            checkBasketButtonTitle();

            // Get token
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "getInstanceId failed", task.getException());
                                return;
                            }

                            // Get new Instance ID token
                            String token = task.getResult().getToken();

                            // Log and toast
                            String msg = getString(R.string.msg_token_fmt, token);
                            Log.d(TAG, msg);
                            Constants.device_token = token;
                        }
                    });
        }
    }

    private void setNavigationViewListner() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(internetConnected.InternetConnectedCheck(this)) {
            checkBasketButtonTitle();
        }
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
        bundle.putString("goto","CategoriesActivity");

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
        RecyclerView recyclerView = findViewById(R.id.recycler_view_categories);
        CategoryRecyclerViewAdapter adapter = new CategoryRecyclerViewAdapter(categories ,this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onRefresh() {
        categories.clear();
        deliveryCostSettings.clear();
        new GetDeliveryCosts().execute();
        new GetCategories().execute();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_privacy: {
                Log.d(TAG,"Open Privacy Policy");
                Intent intent = new Intent(this,PrivacyPolicyActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.nav_terms: {
                Log.d(TAG,"Open Terms and Conditions");
                Intent intent = new Intent(this,TermsConditionsActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.nav_find_us: {
                Log.d(TAG,"Opening Find Us page");
                Intent intent = new Intent(this,FindUsActivity.class);
                startActivity(intent);
                break;
            }
        }
        //close navigation drawer
        mDrawerLayout.closeDrawers();
        return true;
    }

    private class GetCategories extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler httpHandler = new HttpHandler();
            String url = Constants.baseURL+"category";
            String categoryStr = httpHandler.makeServiceCall(url);

            Log.d(TAG,"Response from JSON: " + categoryStr);

            if(categoryStr != null) {
                try {
                    JSONArray categoryJsonArr = new JSONArray(categoryStr);

                    for(int i = 0; i < categoryJsonArr.length(); i++) {
                        JSONObject categoryJsonObj = categoryJsonArr.getJSONObject(i);
                        String name = categoryJsonObj.getString("name");
                        String description = categoryJsonObj.getString("description");

                        Category category = new Category();
                        category.name = name;
                        category.description = description;
                        categories.add(category);
                    }


                } catch (JSONException e) {
                    Log.d(TAG,e.toString());
                }
            } else {
                Log.d(TAG,"TO-DO Error communicating with server");
            }

            String urlPublishableKey = Constants.baseURL+"keys";
            String keysStr = httpHandler.makeServiceCall(urlPublishableKey);

            Log.d(TAG,"Response from Keys: " + keysStr);

            if(keysStr != null) {
                try {
                    JSONArray keysJsonArr = new JSONArray(keysStr);

                    for(int i = 0; i < keysJsonArr.length(); i++) {
                        JSONObject keysJsonObj = keysJsonArr.getJSONObject(i);
                        String stripe_publishable_key = keysJsonObj.getString("stripe_publishable_key");

                        PublishableKey key = new PublishableKey();
                        key.stripe_publishable_key = stripe_publishable_key;
                        keys.add(key);

                        Constants.publishable_key = keys.get(0).stripe_publishable_key;
                    }


                } catch (JSONException e) {
                    Log.d(TAG,e.toString());
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

    private class GetDeliveryCosts extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler httpHandler = new HttpHandler();
            String url = Constants.baseURL+"delivery";
            String deliveryCostStr = httpHandler.makeServiceCall(url);

            Log.d(TAG,"Response from JSON: " + deliveryCostStr);

            if(deliveryCostStr != null) {
                try {
                    JSONArray deliveryCostJsonArr = new JSONArray(deliveryCostStr);

                    for(int i = 0; i < deliveryCostJsonArr.length(); i++) {
                        JSONObject deliveryCostJsonObj = deliveryCostJsonArr.getJSONObject(i);
                        String setting = deliveryCostJsonObj.getString("setting");
                        String value = deliveryCostJsonObj.getString("value");

                        DeliveryCostSettings deliveryCostSetting = new DeliveryCostSettings();
                        deliveryCostSetting.setting = setting;
                        deliveryCostSetting.value = value;
                        deliveryCostSettings.add(deliveryCostSetting);
                    }
                } catch (JSONException e) {
                    Log.d(TAG,"Problem getting delivery settings: " + e);
                }
            } else {
                Log.d(TAG,"TO-DO Error communicating with server");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            format.setCurrency(Currency.getInstance("GBP"));
            deliveryCostTextView.setText(getString(R.string.categories_activity_delivery_cost, deliveryCostSettings.get(0).value));
            minimumOrderTextView.setText(getString(R.string.categories_activity_minimum_order, deliveryCostSettings.get(1).value));
            BasketLogic.minOrder = deliveryCostSettings.get(1).value;
            BasketLogic.deliveryCharge = deliveryCostSettings.get(0).value;
        }
    }

    private class GetAverageTimes extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler httpHandler = new HttpHandler();
            String url = Constants.baseURL+"times";
            String averageTimesStr = httpHandler.makeServiceCall(url);

            Log.d(TAG,"Response from JSON: " + averageTimesStr);

            if(averageTimesStr != null) {
                try {
                    JSONArray averageTimesJsonArr = new JSONArray(averageTimesStr);

                    for(int i = 0; i < averageTimesJsonArr.length(); i++) {
                        JSONObject averageTimesJsonObj = averageTimesJsonArr.getJSONObject(i);
                        String setting = averageTimesJsonObj.getString("setting");
                        int value = averageTimesJsonObj.getInt("value");

                        AverageTimes averageTime = new AverageTimes();
                        averageTime.setting = setting;
                        averageTime.value = value;
                        averageTimes.add(averageTime);
                    }
                } catch (JSONException e) {
                    Log.d(TAG,"Problem getting average times settings: " + e);
                }
            } else {
                Log.d(TAG,"TO-DO Error communicating with server");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Constants.average_delivery = averageTimes.get(0).value;
            Constants.average_collection = averageTimes.get(1).value;
        }
    }
}
