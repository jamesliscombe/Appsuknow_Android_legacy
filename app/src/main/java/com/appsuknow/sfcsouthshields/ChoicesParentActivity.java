package com.appsuknow.sfcsouthshields;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

public class ChoicesParentActivity extends AppCompatActivity {

    private static final String TAG = "ChoicesParentActivity";
    private Map<String,String> choices = new HashMap<>();
    private String incomingDealDescription;
    private String incomingDealName;
    private String incomingDealId;
    private String incomingNumChoices;
    private String incomingDealPrice;
    BasketLogic basketLogic = new BasketLogic();
    TextView description;
    Button basketButton;
    Button addDealToBasket;
    NumberFormat format = NumberFormat.getCurrencyInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choices_parent);
        getIncomingIntent();
        Log.d(TAG, "onCreate started.");
        description = findViewById(R.id.text_view_choices_parent_description);
        initRecyclerView();

        if(incomingDealDescription.length() > 1) {
            description.setText(incomingDealDescription);
        } else {
            description.setVisibility(View.GONE);
        }

        Toolbar toolbar = findViewById(R.id.toolbar_custom);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(incomingDealName);
        }

        basketButton = findViewById(R.id.button_go_to_basket);
        addDealToBasket = findViewById(R.id.button_add_deal_to_basket);
        addDealToBasket.setEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if(toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.black),PorterDuff.Mode.SRC_ATOP);
        } else {
            Log.d(TAG,"Navigation icon is null - fatal.");
        }

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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        getIncomingChoice(intent);
        initRecyclerView();

        if(Integer.parseInt(this.incomingNumChoices) == choices.size()) {
            addDealToBasket.setEnabled(true);
            addDealToBasket.setBackgroundColor(getResources().getColor(R.color.blue));
            addDealToBasket.setTextColor(getResources().getColor(R.color.white));
        }
    }

    public void addDealToBasket(View view) {
        Log.d(TAG,"Adding deal to basket");
        DealsBasket localDeal = new DealsBasket();
        localDeal.deal_id = this.incomingDealId;
        localDeal.name = this.incomingDealName;
        localDeal.price = Double.parseDouble(this.incomingDealPrice);

        localDeal.choices.addAll(choices.values());

        BasketLogic.deals.add(localDeal);

        finish();
    }

    public void launchBasket(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("goto","ChoicesParentActivity");

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
        RecyclerView recyclerView = findViewById(R.id.recycler_view_choices_parent);
        ChoicesParentRecyclerViewAdapter adapter = new ChoicesParentRecyclerViewAdapter(this.choices, Integer.parseInt(incomingNumChoices), incomingDealId ,this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void getIncomingIntent() {
        Log.d(TAG,"Retrieving incoming intent extras.");
        if(getIntent().hasExtra("name") && getIntent().hasExtra("description") && getIntent().hasExtra("id") && getIntent().hasExtra("num_sections")) {
            Log.d(TAG,"Found intent extras.");
            incomingDealName = getIntent().getStringExtra("name");
            incomingDealDescription = getIntent().getStringExtra("description");
            incomingDealId = getIntent().getStringExtra("id");
            incomingNumChoices = getIntent().getStringExtra("num_sections");
            incomingDealPrice = getIntent().getStringExtra("dealPrice");
        }
    }

    private void getIncomingChoice(Intent intent) {
        Log.d(TAG,"Checking is a deal choice is incoming");
        if(getIntent().hasExtra("choice") && getIntent().hasExtra("section")) {
            String incomingChoice = intent.getStringExtra("choice");
            String incomingSection = intent.getStringExtra("section");

            Log.d(TAG,incomingChoice);
            Log.d(TAG,incomingSection);

            choices.put(incomingSection,incomingChoice);
        }
    }
}
