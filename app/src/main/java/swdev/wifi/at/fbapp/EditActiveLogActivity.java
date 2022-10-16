package swdev.wifi.at.fbapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import swdev.wifi.at.fbapp.db.DateConverters;

import static swdev.wifi.at.fbapp.FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA;
import static swdev.wifi.at.fbapp.FetchAddressIntentService.Constants.RESULT_DATA_KEY;

public class EditActiveLogActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{

    public static final String EXTRA_REPLY_TRIPID = "id";
    public static final String EXTRA_REPLY_TRIPSTARTLOC = "startloc";
    public static final String EXTRA_REPLY_TRIPSTARTDATETIME = "startdt";
    public static final String EXTRA_REPLY_TRIPSTARTKM = "startkm";
    public static final String EXTRA_REPLY_TRIPSTARTNOTE = "startnote";
    public static final String EXTRA_REPLY_TRIPSTARTCAT = "startcat";
    public static final String EXTRA_REPLY_TRIPSTARTDATETIMEASLONG = "startdtaslong";

    public static final String EXTRA_REPLY_ENDLOCATION = "endlocation";
    public static final String EXTRA_REPLY_ENDKM = "endkm";
    public static final String EXTRA_REPLY_ENDDATETIME = "enddatetime";
    public static final String EXTRA_REPLY_ENDNOTE = "endnote";
    public static final String EXTRA_REPLY_ENDCAT = "endcat";
    public static final String EXTRA_REPLY_ENDTRIPID = "endtripid";
    public static final String EXTRA_REPLY_ACTION = "tripaction";

    public static final String EXTRA_ACTION_UPDATE = "update";
    public static final String EXTRA_ACTION_DELETE = "delete";

    AlertDialog.Builder builder;

    private int tripId;
    private int tripStartKm;
    private Date startDate;
    TextView TV_Info1;
    TextView TV_Info2;
    TextView TV_Info3;
    private EditText etEndLocation;
    private EditText etEndDate;
    private EditText etEndTime;
    private EditText etEndKm;
    private AutoCompleteTextView etNote;
    private AutoCompleteTextView etEndAddress;
    private Switch swCat;
    private ImageButton btHome;
    private ImageButton btGpsLocation;
    private ProgressBar progressBar;

    protected Location lastKnowLocation;
    private FusedLocationProviderClient locationClient;
    private EditActiveLogActivity.AddressResultReceiver resultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_active_trip);

        TV_Info1 = findViewById(R.id.TV_ActiveInfo2);
        TV_Info2 = findViewById(R.id.TV_ActiveInfo3);
        TV_Info3 = findViewById(R.id.TV_ActiveInfo4);
        etEndLocation = findViewById(R.id.ET_EndLocation);
        etEndDate = findViewById(R.id.ET_EndDate);
        etEndTime = findViewById(R.id.ET_EndTime);
        etEndKm = findViewById(R.id.ET_EndKm);
        etNote = findViewById(R.id.ACTV_EndNote);
        swCat = findViewById(R.id.SW_EndCategory);
        etEndAddress = findViewById(R.id.ACTV_EndAddress);
        btHome = findViewById(R.id.BT_EndHome);
        btGpsLocation = findViewById(R.id.BT_EndGPSLocation);
        progressBar = findViewById(R.id.progressBar);

        locationClient = new FusedLocationProviderClient(this);
        resultReceiver = new EditActiveLogActivity.AddressResultReceiver(new Handler());

        builder = new AlertDialog.Builder(this);
        builder.setTitle("Aktive Fahrt löschen");
        builder.setMessage("Wollen Sie diesen aktiven Fahrt wirklich löschen?");
        builder.setPositiveButton("JA", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent replyintent = new Intent();
                replyintent.putExtra(EXTRA_REPLY_ACTION, EXTRA_ACTION_DELETE);
                replyintent.putExtra(EXTRA_REPLY_ENDTRIPID, tripId);
                setResult(RESULT_OK, replyintent);
                finish();
            }
        });
        builder.setNegativeButton("Nein", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        tripId = getIntent().getExtras().getInt(EXTRA_REPLY_TRIPID);

        TV_Info2.setText(getIntent().getExtras().getString(EXTRA_REPLY_TRIPSTARTLOC));
        TV_Info1.setText(getIntent().getExtras().getString(EXTRA_REPLY_TRIPSTARTDATETIME));
        TV_Info2.setText(getIntent().getExtras().getString(EXTRA_REPLY_TRIPSTARTLOC));
        TV_Info3.setText("Kilometerstand: " + getIntent().getExtras().getInt(EXTRA_REPLY_TRIPSTARTKM) + " km");
        tripStartKm = getIntent().getExtras().getInt(EXTRA_REPLY_TRIPSTARTKM);
        startDate = DateConverters.fromTimestamp(getIntent().getExtras().getLong(EXTRA_REPLY_TRIPSTARTDATETIMEASLONG));
        /* FEATURE DISABLED
        //IF PRESENT FILL IN ENDLOCATION DATA (RETOUR TRIP)
        String endLocation = getIntent().getExtras().getString(EXTRA_REPLY_ENDLOCATION);
        if (!endLocation.equals("---")) {
            String sData[] = endLocation.split(" - ");
            if (sData.length == 2) {
                etEndAddress.setText(sData[0]);
                etEndLocation.setText(sData[1]);
            }
        }
        */

        //FILL IN CURRENT DATE & TIME BY DEFAULT
        Date d1 = new Date();
        final DateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN);
        final DateFormat tf = new SimpleDateFormat("HH:mm", Locale.GERMAN);
        etEndDate.setText(df.format(d1));
        etEndTime.setText(tf.format(d1));

        //FILL LISTS FOR AUTOCOMPLETE OF ADDRESS AND NOTES
        String[] addlist = MainFBActivity.getsAddressArray();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, addlist);
        etEndAddress.setAdapter(adapter);
        String[] notesList = MainFBActivity.getsNotesArray();
        /*ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, notesList);
        etNote.setAdapter(adapter1);*/

        //FILL IN NOTE AND CAT VALUES
        etNote.setText(getIntent().getExtras().getString(EXTRA_REPLY_TRIPSTARTNOTE));
        swCat.setChecked(getIntent().getExtras().getInt(EXTRA_REPLY_TRIPSTARTCAT) == 1);

        //SAVE BUTTON CLICK
        final Button button = findViewById(R.id.BT_EndTrip);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent replyintent = new Intent();
                Date finishDate;

                //obligatory fields must have data
                if ((TextUtils.isEmpty(etEndLocation.getText())) ||
                        (TextUtils.isEmpty(etEndAddress.getText())) ||
                        (TextUtils.isEmpty(etEndDate.getText())) ||
                        (TextUtils.isEmpty(etEndTime.getText())) ||
                        (TextUtils.isEmpty(etEndKm.getText()))) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Bitte zuerst pflichtfelder eintragen...",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                //finishkm must be > startkm
                if (Integer.parseInt(etEndKm.getText().toString()) <= tripStartKm) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Kilometerstand ungültig (<= Km.Stand Abfahrt)...",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                //finish datetime must be > startdate
                /*try {
                    DateFormat dtf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.GERMAN);
                    finishDate = dtf.parse(etEndDate.getText().toString() + " " + etEndTime.getText().toString());
                    if (!finishDate.after(startDate)) {
                        Toast.makeText(
                                getApplicationContext(),
                                "Datum-Zeit ungültig (<= Startzeit)...",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    Toast.makeText(
                            getApplicationContext(),
                            "Problem mit Datum/Zeit ...",
                            Toast.LENGTH_LONG).show();
                    return;
                }*/

                replyintent.putExtra(EXTRA_REPLY_ACTION, EXTRA_ACTION_UPDATE);
                replyintent.putExtra(EXTRA_REPLY_ENDTRIPID, tripId);
                replyintent.putExtra(EXTRA_REPLY_ENDLOCATION, etEndAddress.getText().toString() + " - " + etEndLocation.getText().toString());
                replyintent.putExtra(EXTRA_REPLY_ENDKM, etEndKm.getText().toString());
                if (!TextUtils.isEmpty(etNote.getText())) {
                    replyintent.putExtra(EXTRA_REPLY_ENDNOTE, etNote.getText().toString());
                }
                if (swCat.isChecked()) {
                    replyintent.putExtra(EXTRA_REPLY_ENDCAT, "beruflich");
                } else {
                    replyintent.putExtra(EXTRA_REPLY_ENDCAT, "privat");
                }

                //we store date and time as a long
                /*replyintent.putExtra(EXTRA_REPLY_ENDDATETIME, finishDate.getTime());*/

                setResult(RESULT_OK, replyintent);
                finish();
            }
        });

        etEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date;
                try {
                    DateFormat dtf = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN);
                    date = dtf.parse(etEndDate.getText().toString());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH);
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    DatePickerDialog datePickerDialog = new DatePickerDialog(EditActiveLogActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            etEndDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                        }
                    }, year, month, day);
                    datePickerDialog.show();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        });

        etEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date;
                try {
                    DateFormat dtf = new SimpleDateFormat("HH:mm", Locale.GERMAN);
                    date = dtf.parse(etEndTime.getText().toString());
                    Calendar mcurrentTime = Calendar.getInstance();
                    mcurrentTime.setTime(date);
                    int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = mcurrentTime.get(Calendar.MINUTE);
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(EditActiveLogActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            etEndTime.setText(selectedHour + ":" + selectedMinute);
                        }
                    }, hour, minute, true);//Yes 24 hour time
                    mTimePicker.setTitle("Select Time");
                    mTimePicker.show();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        });

        //DELETE BUTTON CLICK

        final ImageButton btDelete = findViewById(R.id.BT_Delete);
        btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        //HOME BUTTON CLICK
        btHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                etEndAddress.setText(preferences.getString("heimat_adresse", ""));
                etEndLocation.setText(preferences.getString("heimat_ort", ""));
            }
        });

        //GPS BUTTON CLICK
        btGpsLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetGPSLocation();
            }
        });

        etEndAddress.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    //try to find loaction for given address in list
                    String s = MainFBActivity.FindLocation4Address(etEndAddress.getText().toString());
                    if (!s.isEmpty()) {
                        etEndLocation.setText(s);
                    }
                }
            }
        });


        findViewById(R.id.ACTV_EndAddress).requestFocus();

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        etEndDate.setText(dayOfMonth+"/"+(month+1)+"/"+year);
    }

    private void GetGPSLocation() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        proceedGPSLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            proceedGPSLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void proceedGPSLocation() {
        locationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        lastKnowLocation = location;

                        if (lastKnowLocation == null) {
                            Toast.makeText(getApplicationContext(), "Keine Location verfügbar! GPS aktiv?", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (!Geocoder.isPresent()) {
                            Toast.makeText(getApplicationContext(), "Geocoder nicht verfügbar", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        progressBar.setVisibility(View.VISIBLE);

                        Intent intent = new Intent(getApplicationContext(), FetchAddressIntentService.class);
                        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, resultReceiver);
                        intent.putExtra(LOCATION_DATA_EXTRA, lastKnowLocation);

                        startService(intent);
                    }
                });
    }


    private class AddressResultReceiver extends ResultReceiver {

        /**
         * Create a new ResultReceive to receive results.  Your
         * {@link #onReceiveResult} method will be called from the thread running
         * <var>handler</var> if given, or from an arbitrary thread if null.
         *
         * @param handler
         */
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            //button.setEnabled(true);
            progressBar.setVisibility(View.INVISIBLE);

            if (resultData == null) {
                return;
            }

            String addressOutput = resultData.getString(RESULT_DATA_KEY);

            if (addressOutput == null) {
                addressOutput = "";
            }

            //Toast.makeText(getApplicationContext(), "Adresse: " + addressOutput, Toast.LENGTH_SHORT).show();

            String[] sData;
            sData = addressOutput.split(",");
            if (sData.length == 2) {
                etEndAddress.setText(sData[0]);
                etEndLocation.setText(sData[1]);
            } else if (sData.length == 3) {
                etEndAddress.setText(sData[0]);
                etEndLocation.setText(sData[1] + " " + sData[2]);
            } else {
                Toast.makeText(
                        getApplicationContext(),
                        "Adresse konnte nicht erfasst werden:...",
                        Toast.LENGTH_SHORT).show();
            }

        }
    }

}
