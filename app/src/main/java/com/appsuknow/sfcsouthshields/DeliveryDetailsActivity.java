package com.appsuknow.sfcsouthshields;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeliveryDetailsActivity extends AppCompatActivity {

    private static final String TAG = "DeliveryDetailsActivity";
    RadioButton radioButtonCard;
    RadioButton radioButtonCash;
    EditText customerName;
    EditText customerPhone;
    EditText customerAddress;
    EditText customerCity;
    EditText customerPostcode;
    EditText customerEmail;
    Button goToStripe;
    RequestQueue MyRequestQueue;
    BasketLogic basketLogic = new BasketLogic();
    int deliverableArea;
    AlertDialog.Builder builderPriceMismatch;
    AlertDialog.Builder builder2;

    private String note;
    private String selectedTime;
    boolean card;

    public static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "started onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_details);
        Toolbar toolbar = findViewById(R.id.toolbar_custom_basket);
        radioButtonCard = findViewById(R.id.radio_button_card);
        radioButtonCash = findViewById(R.id.radio_button_cash);
        customerName = findViewById(R.id.edit_text_delivery_customer_name);
        customerPhone = findViewById(R.id.edit_text_delivery_customer_phone);
        customerAddress = findViewById(R.id.edit_text_delivery_customer_address);
        customerCity = findViewById(R.id.edit_text_delivery_customer_city);
        customerPostcode = findViewById(R.id.edit_text_delivery_customer_postcode);
        customerEmail = findViewById(R.id.edit_text_delivery_customer_email);
        goToStripe = findViewById(R.id.button_continue_to_stripe);
        radioButtonCard.toggle();
        this.goToStripe.setEnabled(true);
        card = true;
        getIncomingIntent();
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        builderPriceMismatch = new AlertDialog.Builder(this);
        builder2 = new AlertDialog.Builder(this);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        MyRequestQueue = Volley.newRequestQueue(this);
    }

    @Override
    protected void onResume() {
        this.goToStripe.setEnabled(true);
        super.onResume();

        goToStripe.setEnabled(true);
        goToStripe.setText(getString(R.string.generic_place_order));
        goToStripe.setBackgroundColor(getResources().getColor(R.color.blue));
        goToStripe.setTextColor(getResources().getColor(R.color.white));
    }

    public void stripeButtonPressed(View view) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setCancelable(false);

        goToStripe.setEnabled(false);
        goToStripe.setText(getString(R.string.generic_process_order));
        goToStripe.setBackgroundColor(getResources().getColor(R.color.blueDisabled));
        goToStripe.setTextColor(getResources().getColor(R.color.whiteDisabled));

        builder1.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        if (customerPostcode.length() == 0) {
            builder1.setMessage("Please ensure all delivery details are entered.");
            AlertDialog alert11 = builder1.create();
            alert11.show();

            goToStripe.setEnabled(true);
            goToStripe.setText(getString(R.string.generic_place_order));
            goToStripe.setBackgroundColor(getResources().getColor(R.color.blue));
            goToStripe.setTextColor(getResources().getColor(R.color.white));

        } else {
            new StripeContinueAsync().execute();
        }
    }

    private class StripeContinueAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler httpHandler = new HttpHandler();

            String postcodeArea = customerPostcode.getText().toString().replace(" ", "");
            Log.d(TAG, "Postcode Area: " + postcodeArea);
            postcodeArea = postcodeArea.substring(0, postcodeArea.length() - 3);

            String urlStr = Constants.baseURL+"postcode/" + postcodeArea;

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


            String postcodeCountStr;

            if(url != null) {
                postcodeCountStr = httpHandler.makeServiceCall(url.toString());
                Log.d(TAG, "Response from JSON: " + postcodeCountStr);
            } else {
                postcodeCountStr = null;
            }

            if (postcodeCountStr != null) {
                Log.d(TAG, "Result: " + postcodeCountStr);
                deliverableArea = Integer.valueOf(postcodeCountStr.trim());
            } else {
                Log.d(TAG, "TO-DO Error communicating with server");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            continueToStripe();
        }
    }

    public void continueToStripe() {
        Log.d(TAG, "Continue to Stripe button pressed");

        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setCancelable(false);

        builder1.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        if (customerName.length() == 0) {
            builder1.setMessage("Please enter a name.");
            AlertDialog alert11 = builder1.create();
            alert11.show();

            goToStripe.setEnabled(true);
            goToStripe.setText(getString(R.string.generic_place_order));
            goToStripe.setBackgroundColor(getResources().getColor(R.color.blue));
            goToStripe.setTextColor(getResources().getColor(R.color.white));

        } else if (customerPhone.length() == 0) {
            builder1.setMessage("Please enter a phone number.");
            AlertDialog alert11 = builder1.create();
            alert11.show();

            goToStripe.setEnabled(true);
            goToStripe.setText(getString(R.string.generic_place_order));
            goToStripe.setBackgroundColor(getResources().getColor(R.color.blue));
            goToStripe.setTextColor(getResources().getColor(R.color.white));

        } else if (customerAddress.length() == 0) {
            builder1.setMessage("Please enter a valid address.");
            AlertDialog alert11 = builder1.create();
            alert11.show();

            goToStripe.setEnabled(true);
            goToStripe.setText(getString(R.string.generic_place_order));
            goToStripe.setBackgroundColor(getResources().getColor(R.color.blue));
            goToStripe.setTextColor(getResources().getColor(R.color.white));

        } else if (customerCity.length() == 0) {
            builder1.setMessage("Please enter a valid city/town.");
            AlertDialog alert11 = builder1.create();
            alert11.show();

            goToStripe.setEnabled(true);
            goToStripe.setText(getString(R.string.generic_place_order));
            goToStripe.setBackgroundColor(getResources().getColor(R.color.blue));
            goToStripe.setTextColor(getResources().getColor(R.color.white));

        } else if (customerPostcode.length() == 0) {
            builder1.setMessage("Please enter a postcode.");
            AlertDialog alert11 = builder1.create();
            alert11.show();

            goToStripe.setEnabled(true);
            goToStripe.setText(getString(R.string.generic_place_order));
            goToStripe.setBackgroundColor(getResources().getColor(R.color.blue));
            goToStripe.setTextColor(getResources().getColor(R.color.white));

        }else if (customerEmail.length() == 0) {
            builder1.setMessage("Please enter an email address. This will only be used to send an order confirmation.");
            AlertDialog alert11 = builder1.create();
            alert11.show();

            goToStripe.setEnabled(true);
            goToStripe.setText(getString(R.string.generic_place_order));
            goToStripe.setBackgroundColor(getResources().getColor(R.color.blue));
            goToStripe.setTextColor(getResources().getColor(R.color.white));

        } else if (deliverableArea == 0) {
            builder1.setMessage("Whoops, this isn't a postcode area that we deliver to.");
            AlertDialog alert11 = builder1.create();
            alert11.show();

            goToStripe.setEnabled(true);
            goToStripe.setText(getString(R.string.generic_place_order));
            goToStripe.setBackgroundColor(getResources().getColor(R.color.blue));
            goToStripe.setTextColor(getResources().getColor(R.color.white));
        } else {
            this.goToStripe.setEnabled(false);

            if(!isValidEmail(customerEmail.getText().toString())) {
                builder1.setMessage("Invalid email format");
                AlertDialog alert11 = builder1.create();
                alert11.show();
            } else {
                if (card == true) {
                    this.goToStripe.setEnabled(false);
                    Intent intent = new Intent(this, StripeActivity.class);
                    intent.putExtra("customerName", customerName.getText().toString());
                    intent.putExtra("customerPhone", customerPhone.getText().toString());
                    intent.putExtra("customerAddress", customerAddress.getText().toString());
                    intent.putExtra("customerCity", customerCity.getText().toString());
                    intent.putExtra("customerPostcode", customerPostcode.getText().toString());
                    intent.putExtra("note", note);
                    intent.putExtra("deliveryTime", selectedTime);
                    intent.putExtra("email",customerEmail.getText().toString());
                    startActivity(intent);
                } else {
                    String url = Constants.baseURL+"charge";
                    StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
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

                            goToStripe.setEnabled(false);
                            goToStripe.setText(getString(R.string.generic_place_order));
                            goToStripe.setBackgroundColor(getResources().getColor(R.color.blue));
                            goToStripe.setTextColor(getResources().getColor(R.color.white));
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

                            goToStripe.setEnabled(true);
                            goToStripe.setText(getString(R.string.generic_place_order));
                            goToStripe.setBackgroundColor(getResources().getColor(R.color.blue));
                            goToStripe.setTextColor(getResources().getColor(R.color.white));
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> MyData = new HashMap<>();

                            //Single items
                            Map<String, String> singleItems = new HashMap<>();

                            List singleItemKeys = new ArrayList(BasketLogic.singleItems.keySet());

                            for (int i = 0; i < BasketLogic.singleItems.size(); i++) {
                                singleItems.put(singleItemKeys.get(i).toString(), String.valueOf(BasketLogic.singleItems.get(singleItemKeys.get(i)).quantity));
                            }

                            String singleItemsString = String.valueOf(new JSONObject(singleItems));


                            //Multi items
                            Map<String, String> multiItems = new HashMap<>();

                            List multiItemKeys = new ArrayList(BasketLogic.multiItems.keySet());

                            for (int i = 0; i < BasketLogic.multiItems.size(); i++) {
                                multiItems.put(multiItemKeys.get(i).toString(), String.valueOf(BasketLogic.multiItems.get(multiItemKeys.get(i)).quantity));
                            }

                            String multiItemsString = String.valueOf(new JSONObject(multiItems));


                            //deals
                            ArrayList<String> deals = new ArrayList<>();
                            ArrayList<String> choices = new ArrayList<>();

                            for (DealsBasket deal : BasketLogic.deals) {
                                deals.add(deal.name);
                                for (String choice : deal.choices) {
                                    choices.add(deal.name + " : " + choice);
                                }
                            }

                            String[] dealsArr = new String[deals.size()];
                            dealsArr = deals.toArray(dealsArr);

                            String[] choicesArr = new String[choices.size()];
                            choicesArr = choices.toArray(choicesArr);

                            //Delivery/Collection doesn't matter
                            MyData.put("amount", String.valueOf(basketLogic.priceOfAllItems() + Double.parseDouble(BasketLogic.deliveryCharge)));
                            MyData.put("currency", "gbp");
                            MyData.put("description", "Order placed via Android V0.5 app");
                            MyData.put("note", note);
                            MyData.put("time", selectedTime);
                            MyData.put("name", customerName.getText().toString());
                            MyData.put("phoneNumber", customerPhone.getText().toString());
                            MyData.put("street", customerAddress.getText().toString());
                            MyData.put("city", customerCity.getText().toString());
                            MyData.put("postcode", customerPostcode.getText().toString());
                            MyData.put("singleItems", singleItemsString);
                            MyData.put("multiItems", multiItemsString);
                            MyData.put("deals", Arrays.toString(dealsArr));
                            MyData.put("choices", Arrays.toString(choicesArr));
                            MyData.put("card", "false");
                            MyData.put("delivery", "Delivery");
                            MyData.put("email",customerEmail.getText().toString());
                            MyData.put("ios","0");
                            MyData.put("deviceToken",Constants.device_token);
                            return MyData;
                        }
                    };

                    try {
                        Log.d(TAG, "Called MyRequestQueue");
                        MyStringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        MyRequestQueue.add(MyStringRequest);
                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                    }
                }
            }

        }
    }

    public void radioButtonCashClicked(View view) {
        card = false;
    }

    public void radioButtonCardClicked(View view) {
        card = true;
    }

    private void getIncomingIntent() {
        Log.d(TAG, "Retrieving incoming intent extras.");
        if (getIntent().hasExtra("note")) {
            Log.d(TAG, "Found note intent.");
            note = getIntent().getStringExtra("note");
        }

        if (getIntent().hasExtra("selectedTime")) {
            Log.d(TAG, "Found time intent.");
            selectedTime = getIntent().getStringExtra("selectedTime");
        }
    }
}
