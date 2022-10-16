package swdev.wifi.at.fbapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import static swdev.wifi.at.fbapp.EditActiveLogActivity.EXTRA_REPLY_ENDCAT;
import static swdev.wifi.at.fbapp.EditActiveLogActivity.EXTRA_REPLY_ENDDATETIME;
import static swdev.wifi.at.fbapp.EditActiveLogActivity.EXTRA_REPLY_ENDKM;
import static swdev.wifi.at.fbapp.EditActiveLogActivity.EXTRA_REPLY_ENDLOCATION;
import static swdev.wifi.at.fbapp.EditActiveLogActivity.EXTRA_REPLY_ENDNOTE;
import static swdev.wifi.at.fbapp.EditActiveLogActivity.EXTRA_REPLY_ENDTRIPID;
import static swdev.wifi.at.fbapp.EditActiveLogActivity.EXTRA_REPLY_TRIPID;
import static swdev.wifi.at.fbapp.EditActiveLogActivity.EXTRA_REPLY_TRIPSTARTCAT;
import static swdev.wifi.at.fbapp.EditActiveLogActivity.EXTRA_REPLY_TRIPSTARTDATETIME;
import static swdev.wifi.at.fbapp.EditActiveLogActivity.EXTRA_REPLY_TRIPSTARTKM;
import static swdev.wifi.at.fbapp.EditActiveLogActivity.EXTRA_REPLY_TRIPSTARTLOC;
import static swdev.wifi.at.fbapp.EditActiveLogActivity.EXTRA_REPLY_TRIPSTARTNOTE;

public class EditOpenLogActivity extends AppCompatActivity {

    private int tripId;
    private int tripStartKm;
    TextView TV_Info1;
    TextView TV_Info2;
    TextView TV_Info3;
    TextView TV_Infoe1;
    TextView TV_Infoe2;
    TextView TV_Infoe3;

    private AutoCompleteTextView etNote;
    private Switch swCat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_open_trip);

        TV_Info1 = findViewById(R.id.TV_OpenInfo2);
        TV_Info2 = findViewById(R.id.TV_OpenInfo3);
        TV_Info3 = findViewById(R.id.TV_OpenInfo4);
        TV_Infoe1 = findViewById(R.id.TV_OpenInfo6);
        TV_Infoe2 = findViewById(R.id.TV_OpenInfo7);
        TV_Infoe3 = findViewById(R.id.TV_OpenInfo8);
        etNote = findViewById(R.id.ACTV_EndOpenNote);
        swCat = findViewById(R.id.SW_EndOpenCategory);

        tripId = getIntent().getExtras().getInt(EXTRA_REPLY_TRIPID);
        TV_Info1.setText(getIntent().getExtras().getString(EXTRA_REPLY_TRIPSTARTDATETIME));
        TV_Info2.setText(getIntent().getExtras().getString(EXTRA_REPLY_TRIPSTARTLOC));
        TV_Info3.setText("Kilometerstand: " + getIntent().getExtras().getInt(EXTRA_REPLY_TRIPSTARTKM) + " km");

        TV_Infoe1.setText(getIntent().getExtras().getString(EXTRA_REPLY_ENDDATETIME));
        TV_Infoe2.setText(getIntent().getExtras().getString(EXTRA_REPLY_ENDLOCATION));
        TV_Infoe3.setText("Kilometerstand: " + getIntent().getExtras().getInt(EXTRA_REPLY_ENDKM) + " km");

        //FILL IN NOTE AND CAT VALUES
        etNote.setText(getIntent().getExtras().getString(EXTRA_REPLY_TRIPSTARTNOTE));
        swCat.setChecked(getIntent().getExtras().getInt(EXTRA_REPLY_TRIPSTARTCAT) == 1);

        //FILL LIST WITH NOTES FOR AUTOCOMPLETION
        String[] notesList = MainFBActivity.getsNotesArray();
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, notesList);
        etNote.setAdapter(adapter1);

        //SAVE BUTTON CLICK
        final Button button = findViewById(R.id.BT_EndOpenTrip);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //obligatory fields must have data
                //note is obligatory when professional trip
                if (swCat.isChecked()) {
                    if (TextUtils.isEmpty(etNote.getText())) {
                        Toast.makeText(
                                getApplicationContext(),
                                "Bitte zuerst Zweck eintragen (Pflichtfeld für berufliche Fahrten)...",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                Intent replyintent = new Intent();
                replyintent.putExtra(EXTRA_REPLY_ENDTRIPID, tripId);
                if (!TextUtils.isEmpty(etNote.getText())) {
                    replyintent.putExtra(EXTRA_REPLY_ENDNOTE, etNote.getText().toString());
                }
                if (swCat.isChecked()) {
                    replyintent.putExtra(EXTRA_REPLY_ENDCAT, "beruflich");
                } else {
                    replyintent.putExtra(EXTRA_REPLY_ENDCAT, "privat");
                }
                setResult(RESULT_OK, replyintent);
                finish();
            }
        });

    }
}
