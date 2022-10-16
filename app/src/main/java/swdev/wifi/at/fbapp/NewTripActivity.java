package swdev.wifi.at.fbapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
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

import static swdev.wifi.at.fbapp.FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA;
import static swdev.wifi.at.fbapp.FetchAddressIntentService.Constants.RESULT_DATA_KEY;

public class NewTripActivity extends AppCompatActivity  {

    public static final String EXTRA_REPLY_STARTLOCATION = "startlocation";
    public static final String EXTRA_REPLY_STARTKM = "startkm";
    public static final String EXTRA_REPLY_STARTDATETIME = "startdatetime";
    public static final String EXTRA_REPLY_STARTNOTE = "startnote";
    public static final String EXTRA_REPLY_STARTCAT = "startcat";
    public static final String EXTRA_REPLY_ENDLOCATION = "endlocation";

    public static final String EXTRA_LASTSTARTLOCATION = "laststartlocation";
    public static final String EXTRA_LASTENDKM = "laststartkm";
    public static final String EXTRA_LASTENDLOCATION = "lastendlocation";

    private EditText etStartLocation;
    private EditText etStartDate;
    private EditText etStartTime;
    private EditText etStartKm;
    private AutoCompleteTextView etNote;
    private AutoCompleteTextView etStartAddress;
    private Switch swCat;
    private ImageButton btRetourTrip;
    private ImageButton btHome;
    private ImageButton btGpsLocation;
    private ImageButton btLastKm;

    private String lastStartLocation;
    private String lastEndLocation;
    private int lastEndKm;
    private String replyEndLocation;

    protected Location lastKnowLocation;
    private FusedLocationProviderClient locationClient;
    private AddressResultReceiver resultReceiver;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_trip);

        etStartLocation = findViewById(R.id.ET_StartLocation);
        etStartDate = findViewById(R.id.ET_StartDate);
        etStartTime = findViewById(R.id.ET_StartTime);
        etStartKm = findViewById(R.id.ET_StartKm);
        etNote = findViewById(R.id.ACTV_Note);
        swCat = findViewById(R.id.SW_Category);
        etStartAddress = findViewById(R.id.ACTV_StartAddress);
        btRetourTrip = findViewById(R.id.BT_RetourTrip);
        btHome = findViewById(R.id.BT_Home);
        btGpsLocation = findViewById(R.id.BT_GPSLocation);
        btLastKm = findViewById(R.id.BT_LastKm);
        replyEndLocation = "---";
        progressBar = findViewById(R.id.progressBar2);

        locationClient = new FusedLocationProviderClient(this);
        resultReceiver = new AddressResultReceiver(new Handler());

        //RETRIEVE LAST TRIP INFO AND IF PRESENT THEN DISPLAY RETOURTRIP BUTTON
        lastStartLocation = getIntent().getExtras().getString(EXTRA_LASTSTARTLOCATION);
        if (!lastStartLocation.equals("---")) {
            lastEndLocation = getIntent().getExtras().getString(EXTRA_LASTENDLOCATION);
            lastEndKm = getIntent().getExtras().getInt(EXTRA_LASTENDKM);
            btRetourTrip.setVisibility(View.VISIBLE);
            btLastKm.setVisibility(View.VISIBLE);
        } else {
            btRetourTrip.setVisibility(View.GONE);
            btLastKm.setVisibility(View.GONE);
        }

        //FILL IN CURRENT DATE & TIME BY DEFAULT
        Date d1 = new Date();
        final DateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN);
        final DateFormat tf = new SimpleDateFormat("HH:mm", Locale.GERMAN);
        etStartDate.setText(df.format(d1));
        etStartTime.setText(tf.format(d1));

        //FILL LISTS FOR AUTOCOMPLETE OF ADDRESS AND NOTES
        String[] addlist = MainFBActivity.getsAddressArray();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, addlist);
        etStartAddress.setAdapter(adapter);
        String[] notesList = MainFBActivity.getsNotesArray();
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, notesList);
        etNote.setAdapter(adapter1);

        //SAVE BUTTON CLICK
        final Button button = findViewById(R.id.BT_StartTrip);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent replyintent = new Intent();
                Date dstart;

                //obligatory fields must have data
                if ((TextUtils.isEmpty(etStartLocation.getText())) ||
                        (TextUtils.isEmpty(etStartAddress.getText())) ||
                        (TextUtils.isEmpty(etStartDate.getText())) ||
                        (TextUtils.isEmpty(etStartTime.getText())) ||
                        (TextUtils.isEmpty(etStartKm.getText()))) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Bitte zuerst pflichtfelder eintragen...",
                            Toast.LENGTH_LONG).show();
                    return;
                }


                replyintent.putExtra(EXTRA_REPLY_STARTLOCATION, etStartAddress.getText().toString() + " - " + etStartLocation.getText().toString());
                replyintent.putExtra(EXTRA_REPLY_STARTKM, etStartKm.getText().toString());
                if (!TextUtils.isEmpty(etNote.getText())) {
                    replyintent.putExtra(EXTRA_REPLY_STARTNOTE, etNote.getText().toString());
                }
                if (swCat.isChecked()) {
                    replyintent.putExtra(EXTRA_REPLY_STARTCAT, "beruflich");
                } else {
                    replyintent.putExtra(EXTRA_REPLY_STARTCAT, "privat");
                }
                //we store date and time as a long
                try {
                    DateFormat dtf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.GERMAN);
                    dstart = dtf.parse(etStartDate.getText().toString() + " " + etStartTime.getText().toString());
                    replyintent.putExtra(EXTRA_REPLY_STARTDATETIME, dstart.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                replyintent.putExtra(EXTRA_REPLY_ENDLOCATION, replyEndLocation); //if no endlocation yet then it is defaultvalue "---"

                setResult(RESULT_OK, replyintent);

                finish();
            }
        });

        etStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date;
                try {
                    DateFormat dtf = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN);
                    date = dtf.parse(etStartDate.getText().toString());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH);
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    DatePickerDialog datePickerDialog = new DatePickerDialog(NewTripActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            etStartDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                        }
                    }, year, month, day);
                    datePickerDialog.show();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        etStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date;
                try {
                    DateFormat dtf = new SimpleDateFormat("HH:mm", Locale.GERMAN);
                    date = dtf.parse(etStartTime.getText().toString());

                    Calendar mcurrentTime = Calendar.getInstance();
                    mcurrentTime.setTime(date);
                    int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = mcurrentTime.get(Calendar.MINUTE);
                    TimePickerDialog mTimePicker;

                    mTimePicker = new TimePickerDialog(NewTripActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            etStartTime.setText(selectedHour + ":" + selectedMinute);
                        }
                    }, hour, minute, true);//Yes 24 hour time
                    mTimePicker.setTitle("Select Time");
                    mTimePicker.show();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        });

        //RETOURTRIP BUTTON CLICK
        btRetourTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] sData;
                sData = lastEndLocation.split(" - ");
                if (sData.length != 2) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Fehler bei einlesen Daten Retourfahrt...",
                            Toast.LENGTH_LONG).show();
                } else {
                    etStartAddress.setText(sData[0]);
                    etStartLocation.setText(sData[1]);
                    etStartKm.setText("" + lastEndKm);
                    replyEndLocation = lastStartLocation;
                }
                Toast.makeText(
                        getApplicationContext(),
                        "Ankunftdaten letzte Fahrt übernommen...",
                        Toast.LENGTH_SHORT).show();
            }
        });


        //HOME BUTTON CLICK
        btHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                etStartAddress.setText(preferences.getString("heimat_adresse", ""));
                etStartLocation.setText(preferences.getString("heimat_ort", ""));
            }
        });

        etStartAddress.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    //try to find loaction for given address in list
                    String s = MainFBActivity.FindLocation4Address(etStartAddress.getText().toString());
                    if (!s.isEmpty()) {
                        etStartLocation.setText(s);
                    }
                }
            }
        });

        //GPS BUTTON CLICK
        btGpsLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetGPSLocation();
            }
        });

        //LASTKM BUTTON CLICK
        btLastKm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etStartKm.setText("" + lastEndKm);
            }
        });

        //findViewById(R.id.ET_StartAddress).requestFocus();
        findViewById(R.id.ACTV_StartAddress).requestFocus();

    }


    private void GetGPSLocation() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE,  Manifest.permission.INTERNET}, 1);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        proceedGPSLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
                etStartAddress.setText(sData[0]);
                etStartLocation.setText(sData[1]);
            } else if (sData.length == 3) {
                etStartAddress.setText(sData[0]);
                etStartLocation.setText(sData[1] + " " + sData[2]);
            } else {
                Toast.makeText(
                        getApplicationContext(),
                        "Adresse konnte nicht erfasst werden:...",
                        Toast.LENGTH_SHORT).show();
            }

        }
    }
}
