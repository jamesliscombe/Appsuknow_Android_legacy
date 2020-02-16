package com.appsuknow.sfcsouthshields;

import android.content.Intent;
import android.os.AsyncTask;
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

public class ChoicesChildActivity extends AppCompatActivity {

    private static final String TAG = "ChoicesChildActivity";
    private ArrayList<Choices> choices = new ArrayList<>();
    private String incomingDealId;
    private String incomingSection;
    Button basketButton;
    NumberFormat format = NumberFormat.getCurrencyInstance();
    BasketLogic basketLogic = new BasketLogic();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choices_child);
        Log.d(TAG, "onCreate started.");
        initRecyclerView();
        new GetChoices().execute();

        Toolbar toolbar = findViewById(R.id.toolbar_custom);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Selection");
        }

        basketButton = findViewById(R.id.button_go_to_basket);
        checkBasketButtonTitle();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void launchBasket(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("goto","ChoicesChildActivity");

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
        RecyclerView recyclerView = findViewById(R.id.recycler_view_choices_child);
        ChoicesChildRecyclerViewAdapter adapter = new ChoicesChildRecyclerViewAdapter(choices ,this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void getIncomingIntent() {
        Log.d(TAG,"Retrieving incoming intent extras.");
        if(getIntent().hasExtra("incomingDealId") && getIntent().hasExtra("incomingSection")) {
            Log.d(TAG,"Found intent extras.");
            incomingDealId = getIntent().getStringExtra("incomingDealId");
            incomingSection = getIntent().getStringExtra("incomingSection");
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

    private class GetChoices extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getIncomingIntent();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler httpHandler = new HttpHandler();

            String urlStr = Constants.baseURL+"deals/"+incomingDealId+"/"+incomingSection;
            URL url = null;
            URI uri;

            try {
                url = new URL(urlStr);
                uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
                url = uri.toURL();

                Log.d(TAG,"URL = "+ url);

            } catch (MalformedURLException | URISyntaxException e) {
                e.printStackTrace();
            }


            String choicesStr = httpHandler.makeServiceCall(url.toString());

            Log.d(TAG,"Response from JSON: " + choicesStr);

            if(choicesStr != null) {
                try {
                    JSONArray choicesJsonArr = new JSONArray(choicesStr);

                    for(int i = 0; i < choicesJsonArr.length(); i++) {
                        JSONObject choicesJsonObj = choicesJsonArr.getJSONObject(i);
                        String deal_item_id = choicesJsonObj.getString("deal_item_id");
                        String deal_id = choicesJsonObj.getString("deal_id");
                        String section = choicesJsonObj.getString("section");
                        String item = choicesJsonObj.getString("item");

                        Choices choice = new Choices();
                        choice.deal_item_id = deal_item_id;
                        choice.deal_id = deal_id;
                        choice.section = section;
                        choice.item = item;
                        choices.add(choice);
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
        }
    }
}
