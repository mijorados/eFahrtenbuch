package swdev.wifi.at.fbapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ExportActivity extends AppCompatActivity {

    public static final String EXTRA_REPLY_EXPORTFROM = "fromdate";
    public static final String EXTRA_REPLY_EXPORTTILL = "tilldate";
    public static final String EXTRA_REPLY_EXPORTCATEGORY = "category";
    public static final String EXTRA_REPLY_EMAIL = "email";

    private EditText etFrom;
    private EditText etTill;
    private EditText etEmail;
    private Spinner spCat;
    private Button btExport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        etFrom = findViewById(R.id.ET_ExportStartDate);
        etTill = findViewById(R.id.ET_ExportEndDate);
        etEmail = findViewById(R.id.ET_ExportEmail);
        spCat = findViewById(R.id.SP_ExportSpin);
        btExport = findViewById(R.id.BT_ExportStart);

        //BY DEFAULT:
        // FILL IN CURRENT DATE AS TILL DATE
        Date d1 = new Date();
        final DateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN);
        etTill.setText(df.format(d1));
        // FILL IN DEFAULT EXPORT MAIL ADDRESS
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String email = preferences.getString("export_email", "");
        etEmail.setText(email);

        //EXPORT BUTTON CLICK
        btExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent replyintent = new Intent();
                Date d;

                //obligatory fields must have data
                if ((TextUtils.isEmpty(etFrom.getText())) ||
                        (TextUtils.isEmpty(etTill.getText())) ||
                        (TextUtils.isEmpty(etEmail.getText())) ||
                        (TextUtils.isEmpty(spCat.getSelectedItem().toString()))) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Bitte zuerst alle Felder eintragen...",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                //we pass dates as long
                try {
                    DateFormat dtf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.GERMAN);
                    d = dtf.parse(etFrom.getText().toString() + " 00:00");
                    replyintent.putExtra(EXTRA_REPLY_EXPORTFROM, d.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                try {
                    DateFormat dtf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.GERMAN);
                    d = dtf.parse(etTill.getText().toString() + " 23:59");
                    replyintent.putExtra(EXTRA_REPLY_EXPORTTILL, d.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                replyintent.putExtra(EXTRA_REPLY_EMAIL, etEmail.getText().toString());
                replyintent.putExtra(EXTRA_REPLY_EXPORTCATEGORY, spCat.getSelectedItem().toString());
                setResult(RESULT_OK, replyintent);
                finish();
            }
        });

        //START DATE  CLICK
        final DatePickerDialog.OnDateSetListener from_dateListener = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker arg0, int year, int month, int dayOfMonth) {
                etFrom.setText(dayOfMonth+"/"+(month+1)+"/"+year);
            }
        };
        etFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date;
                try {
                    DateFormat dtf = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN);
                    if (!TextUtils.isEmpty(etFrom.getText())) {
                        date = dtf.parse(etFrom.getText().toString());
                    } else {
                        date = new Date();
                    }
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH);
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    DatePickerDialog datePickerDialog = new DatePickerDialog(ExportActivity.this, from_dateListener, year, month, day);
                    datePickerDialog.show();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        //END DATE BUTTON CLICK
        final DatePickerDialog.OnDateSetListener till_dateListener = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker arg0, int year, int month, int dayOfMonth) {
                etTill.setText(dayOfMonth+"/"+(month+1)+"/"+year);
            }
        };
        etTill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date;
                try {
                    DateFormat dtf = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN);
                    if (!TextUtils.isEmpty(etTill.getText())) {
                        date = dtf.parse(etTill.getText().toString());
                    } else {
                        date = new Date();
                    }
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH);
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    DatePickerDialog datePickerDialog = new DatePickerDialog(ExportActivity.this, till_dateListener, year, month, day);
                    datePickerDialog.show();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

    }

}
