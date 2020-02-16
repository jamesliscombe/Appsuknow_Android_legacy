package com.appsuknow.sfcsouthshields;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;

public class NoteTimeActivity extends AppCompatActivity {

    private static final String TAG = "NoteTimeActivity";
    private boolean deliverySelected;
    TextView selectTime;
    EditText note;
    Button payCashCard;
    DateTimeZone zone = DateTimeZone.forID("Europe/London");
    DateTime dt = new DateTime(zone);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_time);
        Toolbar toolbar = findViewById(R.id.toolbar_custom_basket);
        selectTime = findViewById(R.id.text_view_select_delivery_collection_time);
        note = findViewById(R.id.edit_text_note);
        note.setImeOptions(EditorInfo.IME_ACTION_DONE);
        note.setRawInputType(InputType.TYPE_CLASS_TEXT);
        note.setOnEditorActionListener(new DoneOnEditorActionListener());
        payCashCard = findViewById(R.id.button_pay_cash_card);

        getIncomingIntent();
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            if(this.deliverySelected) {
                getSupportActionBar().setTitle("Delivery");
            } else {
                getSupportActionBar().setTitle("Collection");
            }
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        int hours = dt.getHourOfDay();

        if(deliverySelected) {
            if(hours < Constants.deliveryTimeStart) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(this);
                }
                builder.setTitle("Please note...")
                        .setMessage("Delivery starts at 17:30. You can place your delivery order in advance or order for collection if you'd like your meal sooner")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }

    }

    public void payCashCard(View view) {
        Log.d(TAG,"Button pressed");
        if(this.deliverySelected) {
            Bundle bundle = new Bundle();
            Intent intent = new Intent(this,DeliveryDetailsActivity.class);
            bundle.putString("goto","NoteTimeActivity");
            if(this.note != null) {
                intent.putExtra("note",this.note.getText().toString());
            }
            intent.putExtra("selectedTime",this.selectTime.getText());
            intent.putExtras(bundle);
            startActivity(intent);
        } else {
            Bundle bundle = new Bundle();
            Intent intent = new Intent(this, CollectionDetailsActivity.class);
            bundle.putString("goto","NoteTimeActivity");
            if(this.note != null) {
                intent.putExtra("note",this.note.getText().toString());
            }
            intent.putExtra("selectedTime",this.selectTime.getText());
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    public void openListDialog(View view) {
        if(deliverySelected == true) {
            int hours = dt.getHourOfDay();
            int minutes = dt.getMinuteOfHour();
            String[] quarterHours = {"00","15","30","45"};
            List<String> times = new ArrayList<>();

            times.add("Today ASAP");

            if(hours < 18) {
                hours = 18;
                minutes = 0;
            }

            //handle current hour
            if(minutes >= 0 && minutes < 15) {
                Log.d(TAG,"Block 1");
                times.add("Today " + (hours + 1) +":" + "15");
                times.add("Today " + (hours + 1) +":" + "30");
                times.add("Today " + (hours + 1) +":" + "45");
            } else if(minutes >= 15 && minutes < 30) {
                Log.d(TAG,"Block 2");
                times.add("Today " + (hours + 1) +":" + "30");
                times.add("Today " + (hours + 1) +":" + "45");
            } else {
                Log.d(TAG,"Block 3");
                times.add("Today " + (hours + 1) +":" + "45");
            }

            //handle every hour going forward
            for(int i = hours + 2; i < 23; i++) {
                for(int j = 0; j < 4; j++) {
                    String time = i + ":" + quarterHours[j];
                    if(i < 10) {
                        time = "0" + time;
                    }
                    times.add("Today " + time);
                }
            }

            String[] stringArray = new String[times.size()];

            stringArray = times.toArray(stringArray);

            final String[] finalItems = stringArray;

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Time");
            builder.setItems(finalItems, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    selectTime.setText(finalItems[item]);
                }
            });
            AlertDialog alert = builder.create();
            alert.show();

        } else {
            DateTimeZone zone = DateTimeZone.forID("Europe/London");
            DateTime dt = new DateTime(zone);
            int hours = dt.getHourOfDay();
            int minutes = dt.getMinuteOfHour();
            String[] quarterHours = {"00","15","30","45"};
            List<String> times = new ArrayList<>();

            times.add("Today ASAP");

            //handle current hour

            if(hours < 18) {
                hours = 18;
                minutes = 0;
            }

            if(minutes >= 0 && minutes < 15) {
                Log.d(TAG,"Block 1");
                times.add("Today " + (hours) +":" + "45");
            } else if (minutes >= 15 && minutes < 30) {
                times.add("Today " + (hours + 1) +":" + "15");
                times.add("Today " + (hours + 1) +":" + "30");
                times.add("Today " + (hours + 1) +":" + "45");
            } else {
                times.add("Today " + (hours + 1) +":" + "15");
                times.add("Today " + (hours + 1) +":" + "30");
                times.add("Today " + (hours + 1) +":" + "45");
            }

            //handle every hour going forward
            for(int i = hours + 2; i < 23; i++) {
                for(int j = 0; j < 4; j++) {
                    String time = i + ":" + quarterHours[j];
                    if(i < 10) {
                        time = "0" + time;
                    }
                    times.add("Today " + time);
                }
            }

            String[] stringArray = new String[times.size()];

            stringArray = times.toArray(stringArray);

            final String[] finalItems = stringArray;

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Time");
            builder.setItems(finalItems, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    selectTime.setText(finalItems[item]);
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void getIncomingIntent() {
        Log.d(TAG,"Retrieving incoming intent extras.");
        if(getIntent().hasExtra("deliverySelected")) {
            Log.d(TAG,"Found intent extras.");
            this.deliverySelected = getIntent().getExtras().getBoolean("deliverySelected");
        }

        if(getIntent().hasExtra("time")) {
            Log.d(TAG,"Found time intent");
            selectTime.setText(getIntent().getStringExtra("time"));
        }

        if(getIntent().hasExtra("note")) {
            Log.d(TAG,"Found note intent");
            note.setText(getIntent().getStringExtra("note"));
        }
    }
}
