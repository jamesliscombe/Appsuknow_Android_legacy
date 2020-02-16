package com.appsuknow.sfcsouthshields;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StripeActivity extends AppCompatActivity {

    private static final String TAG = "StripeActivity";
    Stripe stripe;
    double amount;
    CardInputWidget cardInputWidget;
    EditText editTextCustomerName;
    EditText editTextCustomerAddress;
    EditText editTextCustomerPostcode;
    EditText editTextCustomerCity;
    boolean delivery;
    BasketLogic basketLogic = new BasketLogic();
    RequestQueue MyRequestQueue;
    AlertDialog.Builder builder2;
    AlertDialog.Builder builderPriceMismatch;
    Button submitButton;

    String customerName;
    String customerPhone;
    String customerAddress;
    String customerCity;
    String customerPostcode;
    String email;
    String note;
    String deliveryTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stripe);
        cardInputWidget = findViewById(R.id.card_input_widget);
        editTextCustomerPostcode = findViewById(R.id.edit_text_stripe_customer_postcode);
        editTextCustomerCity = findViewById(R.id.edit_text_stripe_customer_city);
        editTextCustomerAddress = findViewById(R.id.edit_text_stripe_customer_address);
        editTextCustomerName = findViewById(R.id.edit_text_stripe_customer_name);
        Toolbar toolbar = findViewById(R.id.toolbar_custom_basket);
        submitButton = findViewById(R.id.submitButton);
        amount = basketLogic.priceOfAllItems();
        delivery = true;
        getIncomingIntent();
        MyRequestQueue = Volley.newRequestQueue(this);
        stripe = new Stripe(this,Constants.publishable_key);
        builder2 = new AlertDialog.Builder(this);
        builderPriceMismatch = new AlertDialog.Builder(this);

        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void getIncomingIntent() {
        Log.d(TAG,"Retrieving incoming intent extras.");
        if(getIntent().hasExtra("customerName")) {
            Log.d(TAG,"Found customerName intent.");
            this.customerName = getIntent().getStringExtra("customerName");
            Log.d(TAG,this.customerName);
            this.editTextCustomerName.setText(this.customerName);
        }

        if(getIntent().hasExtra("customerPhone")) {
            Log.d(TAG,"Found customerPhone intent.");
            this.customerPhone = getIntent().getStringExtra("customerPhone");
        }

        if(getIntent().hasExtra("customerAddress")) {
            Log.d(TAG,"Found customerAddress intent.");
            this.customerAddress = getIntent().getStringExtra("customerAddress");
            this.editTextCustomerAddress.setText(this.customerAddress);
        }

        if(getIntent().hasExtra("email")) {
            Log.d(TAG,"Found email intent.");
            this.email = getIntent().getStringExtra("email");
        }

        if(getIntent().hasExtra("customerCity")) {
            Log.d(TAG,"Found customerCity intent.");
            this.customerCity = getIntent().getStringExtra("customerCity");
            this.editTextCustomerCity.setText(this.customerCity);
        }

        if(getIntent().hasExtra("customerPostcode")) {
            Log.d(TAG,"Found customerPostcode intent.");
            this.customerPostcode = getIntent().getStringExtra("customerPostcode");
            this.editTextCustomerPostcode.setText(this.customerPostcode);
        } else {
            this.delivery = false;
        }

        if(getIntent().hasExtra("note")) {
            Log.d(TAG,"Found note intent.");
            this.note = getIntent().getStringExtra("note");
        }

        if(getIntent().hasExtra("deliveryTime")) {
            Log.d(TAG,"Found deliveryTime intent.");
            this.deliveryTime = getIntent().getStringExtra("deliveryTime");
        }
    }

    public void submitCard(View view) {
        submitButton.setEnabled(false);
        Card card = cardInputWidget.getCard();
        submitButton.setText(getString(R.string.generic_process_order));
        submitButton.setBackgroundColor(getResources().getColor(R.color.blueDisabled));
        submitButton.setTextColor(getResources().getColor(R.color.whiteDisabled));

        if (card == null) {
            builder2.setCancelable(false);

            builder2.setPositiveButton(
                    "Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

            builder2.setMessage("Please check your card details and try again");
            AlertDialog alert12 = builder2.create();
            alert12.show();

            submitButton.setEnabled(true);
            submitButton.setText(getString(R.string.generic_submit_payment));
            submitButton.setBackgroundColor(getResources().getColor(R.color.blue));
            submitButton.setTextColor(getResources().getColor(R.color.white));
        } else if(editTextCustomerName.getText().toString().trim().equals("") || editTextCustomerAddress.getText().toString().trim().equals("") || editTextCustomerCity.getText().toString().trim().equals("") || editTextCustomerPostcode.getText().toString().trim().equals("")) {
            builder2.setCancelable(false);

            builder2.setPositiveButton(
                    "Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

            builder2.setMessage("Please check your billing address details");
            AlertDialog alert12 = builder2.create();
            alert12.show();

            submitButton.setEnabled(true);
            submitButton.setText(getString(R.string.generic_submit_payment));
            submitButton.setBackgroundColor(getResources().getColor(R.color.blue));
            submitButton.setTextColor(getResources().getColor(R.color.white));
        } else {
            card.setName(this.editTextCustomerName.getText().toString());
            card.setAddressCity(this.editTextCustomerCity.getText().toString());
            card.setAddressLine1(this.editTextCustomerAddress.getText().toString());
            card.setAddressZip(this.editTextCustomerPostcode.getText().toString());

            stripe.createToken(
                    card,
                    new TokenCallback() {
                        public void onSuccess(final Token token) {
                            Log.d(TAG, "Success, pass token to server");

                            Log.d(TAG,"*****************************");
                            Log.d(TAG,"Token sent: "+token.getId());
                            Log.d(TAG,"*****************************");

                            //Start of work on appsuknow server
                            String url = Constants.baseURL+"charge";
                            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    //This code is executed if the server responds, whether or not the response contains data.
                                    //The String 'response' contains the server's response.
                                    Log.d(TAG,response);
                                    if(response != null) {
                                        builder2.setCancelable(false);

                                        builder2.setPositiveButton(
                                                "Ok",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.dismiss();
                                                        BasketLogic.deals.clear();
                                                        BasketLogic.multiItems.clear();
                                                        BasketLogic.singleItems.clear();
                                                        Intent intent = new Intent(getApplicationContext(),CategoriesActivity.class);
                                                        startActivity(intent);
                                                    }
                                                });

                                        builder2.setMessage("Order submitted - An order confirmation has been sent to the provided email.");
                                        AlertDialog alert12 = builder2.create();
                                        alert12.show();

                                        submitButton.setEnabled(false);
                                        submitButton.setText(getString(R.string.generic_submit_payment));
                                        submitButton.setBackgroundColor(getResources().getColor(R.color.blue));
                                        submitButton.setTextColor(getResources().getColor(R.color.white));
                                    }
                                }
                            }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    //This code is executed if there is an error.
                                    Log.d(TAG, error.toString());

                                    builder2.setCancelable(false);

                                    builder2.setPositiveButton(
                                            "Ok",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.dismiss();
                                                }
                                            });

                                    builderPriceMismatch.setCancelable(false);

                                    builderPriceMismatch.setPositiveButton(
                                            "Ok",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.dismiss();
                                                    BasketLogic.deals.clear();
                                                    BasketLogic.multiItems.clear();
                                                    BasketLogic.singleItems.clear();
                                                    Intent intent = new Intent(getApplicationContext(),CategoriesActivity.class);
                                                    startActivity(intent);
                                                }
                                            });

                                    NetworkResponse networkResponse = error.networkResponse;

                                    if(networkResponse.statusCode == 306) {
                                        builder2.setMessage("It looks like something went wrong placing your order. If this continues, contact the takeaway directly.");
                                        AlertDialog alert12 = builder2.create();
                                        alert12.show();
                                    } else if(networkResponse.statusCode == 307) {
                                        builder2.setMessage("There seems to be a problem with the AppsUKnow server. Contact the takeaway directly to place your order.");
                                        AlertDialog alert12 = builder2.create();
                                        alert12.show();
                                    } else if(networkResponse.statusCode == 308) {
                                        builder2.setMessage("There seems to be a problem with the AppsUKnow server. Contact the takeaway directly to place your order.");
                                        AlertDialog alert12 = builder2.create();
                                        alert12.show();
                                    } else if(networkResponse.statusCode == 309) {
                                        builder2.setMessage("There seems to be a problem with the AppsUKnow server. Contact the takeaway directly to place your order.");
                                        AlertDialog alert12 = builder2.create();
                                        alert12.show();
                                    } else if(networkResponse.statusCode == 310) {
                                        builder2.setMessage("There was a problem processing the payment with the supplied card details. Please check the details and try again.");
                                        AlertDialog alert12 = builder2.create();
                                        alert12.show();
                                    } else if(networkResponse.statusCode == 311) {
                                        builder2.setMessage("There seems to be a problem with the AppsUKnow server. Contact the takeaway directly to place your order.");
                                        AlertDialog alert12 = builder2.create();
                                        alert12.show();
                                    } else if(networkResponse.statusCode == 312) {
                                        builderPriceMismatch.setMessage("It looks like the prices of some items in your basket have changed during the order process. Please go back and order again. Sorry for the inconvenience");
                                        AlertDialog alert12 = builderPriceMismatch.create();
                                        alert12.show();
                                    } else {
                                        builder2.setMessage("There was a problem processing your order. If this continues, please contact the takeaway");
                                        AlertDialog alert12 = builder2.create();
                                        alert12.show();
                                    }

                                    submitButton.setEnabled(true);
                                    submitButton.setText(getString(R.string.generic_submit_payment));
                                    submitButton.setBackgroundColor(getResources().getColor(R.color.blue));
                                    submitButton.setTextColor(getResources().getColor(R.color.white));
                                }

                            }) {
                                @Override
                                protected Map<String, String> getParams() {
                                    Map<String, String> MyData = new HashMap<>();

                                    //Single items
                                    Map<String, String> singleItems = new HashMap<>();

                                    List singleItemKeys = new ArrayList(BasketLogic.singleItems.keySet());

                                    for(int i = 0; i < BasketLogic.singleItems.size(); i++) {
                                        singleItems.put(singleItemKeys.get(i).toString(),String.valueOf(BasketLogic.singleItems.get(singleItemKeys.get(i)).quantity));
                                    }

                                    String singleItemsString = String.valueOf(new JSONObject(singleItems));


                                    //Multi items
                                    Map<String, String> multiItems = new HashMap<>();

                                    List multiItemKeys = new ArrayList(BasketLogic.multiItems.keySet());

                                    for(int i = 0; i < BasketLogic.multiItems.size(); i++) {
                                        multiItems.put(multiItemKeys.get(i).toString(),String.valueOf(BasketLogic.multiItems.get(multiItemKeys.get(i)).quantity));
                                    }

                                    String multiItemsString = String.valueOf(new JSONObject(multiItems));


                                    //deals
                                    ArrayList<String> deals = new ArrayList<>();
                                    ArrayList<String> choices = new ArrayList<>();

                                    for(DealsBasket deal : BasketLogic.deals) {
                                        deals.add(deal.name);
                                        for(String choice : deal.choices) {
                                            choices.add(deal.name + " : " + choice);
                                        }
                                    }

                                    String[] dealsArr = new String[deals.size()];
                                    dealsArr = deals.toArray(dealsArr);

                                    String[] choicesArr = new String[choices.size()];
                                    choicesArr = choices.toArray(choicesArr);

                                    //Delivery/Collection doesn't matter
                                    MyData.put("token", token.getId());
                                    MyData.put("currency", "gbp");
                                    MyData.put("description", "Order placed via Android V0.5 app");
                                    MyData.put("note", note);
                                    MyData.put("time", deliveryTime);
                                    MyData.put("name", customerName);
                                    MyData.put("phoneNumber", customerPhone);
                                    MyData.put("singleItems", singleItemsString);
                                    MyData.put("multiItems", multiItemsString);
                                    MyData.put("deals",Arrays.toString(dealsArr));
                                    MyData.put("choices",Arrays.toString(choicesArr));
                                    MyData.put("card", "true");
                                    MyData.put("email",email);
                                    MyData.put("ios","0");
                                    MyData.put("deviceToken",Constants.device_token);

                                    if(!delivery) {
                                        MyData.put("amount", String.valueOf(basketLogic.priceOfAllItems()));
                                        MyData.put("delivery", "Collection");
                                    } else {
                                        MyData.put("amount", String.valueOf(basketLogic.priceOfAllItems() + Double.parseDouble(BasketLogic.deliveryCharge)));
                                        MyData.put("delivery", "Delivery");
                                        MyData.put("street", customerAddress);
                                        MyData.put("city", customerCity);
                                        MyData.put("postcode", customerPostcode);
                                    }

                                    return MyData;
                                }
                            };

                            try {
                                Log.d(TAG,"Called MyRequestQueue");
                                MyStringRequest.setRetryPolicy(new DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                MyRequestQueue.add(MyStringRequest);
                            } catch(Exception e) {
                                Log.d(TAG,e.toString());
                            }

                            //End of work on appsuknow server
                        }

                        public void onError(Exception error) {
                            Log.d(TAG, "Something went wrong creating the token");

                            builder2.setCancelable(false);

                            builder2.setPositiveButton(
                                    "Ok",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                        }
                                    });

                            builder2.setMessage(error.toString());
                            AlertDialog alert12 = builder2.create();
                            alert12.show();

                            submitButton.setEnabled(true);
                            submitButton.setText(getString(R.string.generic_submit_payment));
                            submitButton.setBackgroundColor(getResources().getColor(R.color.blue));
                            submitButton.setTextColor(getResources().getColor(R.color.white));
                        }
                    }
            );
        }
    }
}
