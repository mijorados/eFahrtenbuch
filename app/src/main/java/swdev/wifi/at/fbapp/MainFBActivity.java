package swdev.wifi.at.fbapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
/*import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;*/

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


import swdev.wifi.at.fbapp.db.ChartActivity;
import swdev.wifi.at.fbapp.db.DateConverters;
import swdev.wifi.at.fbapp.db.Trip;
import swdev.wifi.at.fbapp.db.TripViewModel;

public class MainFBActivity extends AppCompatActivity {

    private TripViewModel mTripViewModel;
    public static final int NEW_LOG_ACTIVITY_REQUEST_CODE = 123;
    public static final int EDIT_ACTIVELOG_ACTIVITY_REQUEST_CODE = 456;
    public static final int EDIT_OPENLOG_ACTIVITY_REQUEST_CODE = 789;
    public static final int EXPORT_ACTIVITY_REQUEST_CODE = 012;
    private boolean activeTrips;
    private Trip recentlyDeleted;
    private DateFormat df = new SimpleDateFormat("EEE dd MMM yyyy, HH:mm",
            Locale.GERMAN);

    private String exportEmail;
    private Long exportFrom;
    private Long exportTill;
    private String  exportCat;

    private static String[] sAddressArray;
    private static List<String> sFullAddressList;

    private static String[] sNotesArray;

    private View.OnClickListener itemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Trip trip = (Trip) v.getTag();
            //depending on tripdata we show activity for ACTIVE or OPEN trip
            //OPEN TRIP
            if (trip.getFinishKm() > 0) {
                Intent intent = new Intent(MainFBActivity.this, EditOpenLogActivity.class);
                intent.putExtra(EditActiveLogActivity.EXTRA_REPLY_TRIPID, trip._id);
                intent.putExtra(EditActiveLogActivity.EXTRA_REPLY_TRIPSTARTLOC, trip.getStartLocation());
                intent.putExtra(EditActiveLogActivity.EXTRA_REPLY_TRIPSTARTDATETIME, df.format(trip.getStart()));
                intent.putExtra(EditActiveLogActivity.EXTRA_REPLY_TRIPSTARTKM, trip.getStartKm());
                intent.putExtra(EditActiveLogActivity.EXTRA_REPLY_TRIPSTARTNOTE, trip.getNote());
                intent.putExtra(EditActiveLogActivity.EXTRA_REPLY_TRIPSTARTCAT, trip.getCategory());
                intent.putExtra(EditActiveLogActivity.EXTRA_REPLY_ENDLOCATION, trip.getFinishLocation());
                intent.putExtra(EditActiveLogActivity.EXTRA_REPLY_ENDKM, trip.getFinishKm());
                intent.putExtra(EditActiveLogActivity.EXTRA_REPLY_ENDDATETIME, df.format(trip.getFinish()));

                startActivityForResult(intent, EDIT_OPENLOG_ACTIVITY_REQUEST_CODE);
            //ACTIVE TRIP
            } else {
                //prepare a list of all used addresses (used for autocompletion)
                PrepareAddressRefLists();

                Intent intent = new Intent(MainFBActivity.this, EditActiveLogActivity.class);
                intent.putExtra(EditActiveLogActivity.EXTRA_REPLY_TRIPID, trip._id);
                intent.putExtra(EditActiveLogActivity.EXTRA_REPLY_TRIPSTARTLOC, trip.getStartLocation());
                intent.putExtra(EditActiveLogActivity.EXTRA_REPLY_TRIPSTARTDATETIME, df.format(trip.getStart()));
                intent.putExtra(EditActiveLogActivity.EXTRA_REPLY_TRIPSTARTDATETIMEASLONG, DateConverters.dateToTimestamp(trip.getStart()));
                intent.putExtra(EditActiveLogActivity.EXTRA_REPLY_TRIPSTARTKM, trip.getStartKm());
                intent.putExtra(EditActiveLogActivity.EXTRA_REPLY_TRIPSTARTNOTE, trip.getNote());
                intent.putExtra(EditActiveLogActivity.EXTRA_REPLY_TRIPSTARTCAT, trip.getCategory());
                /*
                FEATURE DISABLED
                //pass endlocation if present (retourtrip)
                if (trip.getFinishLocation() != null && !trip.getFinishLocation().isEmpty()) {
                    intent.putExtra(EditActiveTripActivity.EXTRA_REPLY_ENDLOCATION, trip.getFinishLocation());
                } else {
                    intent.putExtra(EditActiveTripActivity.EXTRA_REPLY_ENDLOCATION, "---");
                }
                */
                startActivityForResult(intent, EDIT_ACTIVELOG_ACTIVITY_REQUEST_CODE);
            }
        }
    };





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_fb);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //in order to avoid the following error on Android 8:
        //android.os.FileUriExposedException: file:///storage/emulated/0/Download/fahrtenbuchdata.csv exposed beyond app through ClipData.Item.getUri()
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //we can not start new trip when active trip(s) present
                if (mTripViewModel.activeTrips()) { //(mTripViewModel.activeTrips()) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Hinzufügen nicht möglich, es gibt ein aktive Fahrt...",
                            Toast.LENGTH_LONG).show();
                } else {
                    //prepare a list of all used addresses (used for autocompletion)
                    PrepareAddressRefLists();
                    PrepareNotesRefList();

                    //we retrieve data of last trip and past to newtripactivity
                    Intent intent = new Intent(MainFBActivity.this, NewTripActivity.class);
                    Trip lastTrip = mTripViewModel.getLastTrip();
                    if (lastTrip != null) {
                        intent.putExtra(NewTripActivity.EXTRA_LASTSTARTLOCATION,lastTrip.getStartLocation());
                        intent.putExtra(NewTripActivity.EXTRA_LASTENDKM, lastTrip.getFinishKm());
                        intent.putExtra(NewTripActivity.EXTRA_LASTENDLOCATION,lastTrip.getFinishLocation());
                    } else {
                        intent.putExtra(NewTripActivity.EXTRA_LASTSTARTLOCATION,"---");
                    }

                    startActivityForResult(intent, NEW_LOG_ACTIVITY_REQUEST_CODE);
                }
            }
        });

        RecyclerView recyclerView = findViewById(R.id.RV_alltrips);
        final TripListAdapter adapter = new TripListAdapter(this, itemClickListener);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get a new or existing ViewModel from the ViewModelProvider.
        mTripViewModel = ViewModelProviders.of(this).get(TripViewModel.class);

        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        mTripViewModel.getAllTrips().observe(this, new Observer<List<Trip>>() {
            @Override
            public void onChanged(@Nullable final List<Trip> trips) {
                // Update the cached copy of the words in the adapter.
                adapter.setTrips(trips);
            }
        });


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                int position = viewHolder.getAdapterPosition();

                recentlyDeleted = adapter.getTripAt(position);

                mTripViewModel.delete(recentlyDeleted);

                showUndo(viewHolder.itemView);

                /*mTripViewModel.delete(adapter.getTripAt(viewHolder.getAdapterPosition()));
                Toast.makeText(MainFBActivity.this, "Fahrt gelöscht", Toast.LENGTH_SHORT).show();*/
            }
        }).attachToRecyclerView(recyclerView);

        /*adapter.setOnItemClickListener(new TripListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Trip trip) {
                Intent intent = new Intent(MainFBActivity.this, UpdateActivity.class);

            }
        });*/

    }
    private void showUndo(View view){
        Snackbar.make(view, "Fahrt gelöscht", Snackbar.LENGTH_LONG)
                .setAction("Rückgängig", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mTripViewModel.insert(recentlyDeleted);
                    }
                }).show();
    }




    public static String FindLocation4Address(final String address) {
        String s = "";
        for (String str : sFullAddressList) {
            if (str.trim().contains(address)) {
                s = str;
                break;
            }
        }
        if (!s.isEmpty()) {
            s = s.split(" - ")[1];
        }
        return s;
    }

    public static String[] getsAddressArray() {
        return sAddressArray;
    }

    public static String[] getsNotesArray() {
        return sNotesArray;
    }

    private void PrepareNotesRefList() {
        List<String> NotesList = mTripViewModel.getNotes();
        sNotesArray = new String[NotesList.size()];
        NotesList.toArray(sNotesArray);
    }

    private void PrepareAddressRefLists() {
        //get unique list of all places in db (start and finish)
        sFullAddressList = mTripViewModel.getStartLocations();
        List<String> sListFinish = mTripViewModel.getFinishLocations();
        //merge lists
        for (String s : sListFinish) {
            if (!sFullAddressList.contains(s)) {
                sFullAddressList.add(s);
            }
        }
        //extract addresses from lists (address without location)
        sAddressArray = new String[sFullAddressList.size()];
        String address;
        int i = 0;
        for (String  s : sFullAddressList) {
            if (s != null && !s.isEmpty()) {
                address = (s.lastIndexOf(" - ") > -1) ? s.substring(0, s.lastIndexOf(" - ")) : s;
                sAddressArray[i] = address;
                i += 1;
            } else {
                sAddressArray[i] = "";
                i += 1;
            }
        }

        /* TESTING PURPOSES
        String[] sListAsArray = new String[sList.size()];
        sList.toArray(sListAsArray);
        StringBuilder sb = new StringBuilder();
        for (String s : sListAsArray)
            sb.append(s + "").append(";");
        String ss = sb.substring(0, sb.length() - 1);
        Log.d("TAG", ss);
        sb.setLength(0);
        for (String s : sAddressArray)
            sb.append(s + "").append(";");
        ss = sb.substring(0, sb.length() - 1);
        Log.d("TAG", ss);
        */
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_export) {
            exportData();
            return true;
        } else if (id == R.id.action_stat) {
            createChart4Past12Months();
            return true;
        } else if (id == R.id.action_info) {
            ShowInfoDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    public void ShowInfoDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Fahrtenbuch Info");
        alertDialog.setMessage("Fahrtenbuch App - V1.00\nFür Android SDK29 und höher\n\nWIFI Abschlussprojekt 2021\n'Software Developer Java'\nMijo Rados");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Trip trip;
        Date d1;
        Date dnow;
        int tripid;
        String tripNote;

        if (requestCode == NEW_LOG_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                d1 = new Date(data.getLongExtra(NewTripActivity.EXTRA_REPLY_STARTDATETIME, 0));
                trip = new Trip(d1,
                        data.getStringExtra(NewTripActivity.EXTRA_REPLY_STARTLOCATION),
                        Integer.parseInt(data.getStringExtra(NewTripActivity.EXTRA_REPLY_STARTKM)),
                        data.getStringExtra(NewTripActivity.EXTRA_REPLY_STARTCAT),
                        data.getStringExtra(NewTripActivity.EXTRA_REPLY_STARTNOTE));
                String endLoc = data.getStringExtra(NewTripActivity.EXTRA_REPLY_ENDLOCATION);
            /*FEATURE DISABLED
            //if we also received an endlocation (retourtrip) we store this in the new trip
            if (!endLoc.equals("---")) {
                trip.setFinishLocation(endLoc);
            }
            */
                mTripViewModel.addTrip(trip);
            } else {
                Toast.makeText(
                        getApplicationContext(),
                        "Leere Fahrt nicht gespeichert...",
                        Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == EDIT_ACTIVELOG_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                d1 = new Date(data.getLongExtra(EditActiveLogActivity.EXTRA_REPLY_ENDDATETIME, 0));
                tripid = data.getIntExtra(EditActiveLogActivity.EXTRA_REPLY_ENDTRIPID, 0);
                //VALID TRIPID
                if (tripid > 0) {
                    //UPDATE TRIP
                    if (EditActiveLogActivity.EXTRA_ACTION_UPDATE.equals(data.getStringExtra(EditActiveLogActivity.EXTRA_REPLY_ACTION))) {
                        trip = mTripViewModel.getTripById(tripid);
                        d1 = new Date(data.getLongExtra(EditActiveLogActivity.EXTRA_REPLY_ENDDATETIME, 0));
                        trip.setFinish(d1);
                        trip.setFinishKm(Integer.parseInt(data.getStringExtra(EditActiveLogActivity.EXTRA_REPLY_ENDKM)));
                        trip.setFinishLocation(data.getStringExtra(EditActiveLogActivity.EXTRA_REPLY_ENDLOCATION));
                        String sCat = data.getStringExtra(EditActiveLogActivity.EXTRA_REPLY_ENDCAT);
                        if (sCat.equals("beruflich")) {
                            trip.setCategory(1);
                        } else {
                            trip.setCategory(0);
                        }
                        tripNote = data.getStringExtra(EditActiveLogActivity.EXTRA_REPLY_ENDNOTE);
                        if (tripNote != null && !tripNote.isEmpty()) {
                            trip.setNote(tripNote);
                            //if note also present then we can finish this trip (ie set saved_at date)
                            dnow = new Date();
                            trip.setSavedAt(dnow);
                        } else {
                            //if no note present and private trip (cat=0) then we can finish this trip (note is optional for private trips)
                            if (trip.getCategory() == 0) {
                                dnow = new Date();
                                trip.setSavedAt(dnow);
                            }
                        }

                        mTripViewModel.updateTrip(trip);
                        //DELETE TRIP
                    } else if (EditActiveLogActivity.EXTRA_ACTION_DELETE.equals(data.getStringExtra(EditActiveLogActivity.EXTRA_REPLY_ACTION))) {
                        mTripViewModel.deleteTrip(tripid);
                        Toast.makeText(
                                getApplicationContext(),
                                "Aktive Fahrt wurde gelöscht.",
                                Toast.LENGTH_LONG).show();
                    }


                } else {
                    Toast.makeText(
                            getApplicationContext(),
                            "Änderungen aktive Fahrt NICHT gespeichert, unbekannte ID.",
                            Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(
                        getApplicationContext(),
                        "Änderungen aktive Fahrt wurden nicht gespeichert...",
                        Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == EDIT_OPENLOG_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                tripid = data.getIntExtra(EditActiveLogActivity.EXTRA_REPLY_ENDTRIPID, 0);
                //VALID TRIPID
                if (tripid > 0) {
                    //UPDATE TRIP
                    trip = mTripViewModel.getTripById(tripid);
                    String sCat = data.getStringExtra(EditActiveLogActivity.EXTRA_REPLY_ENDCAT);
                    if (sCat.equals("beruflich")) {
                        trip.setCategory(1);
                    } else {
                        trip.setCategory(0);
                    }
                    tripNote = data.getStringExtra(EditActiveLogActivity.EXTRA_REPLY_ENDNOTE);
                    if (tripNote != null && !tripNote.isEmpty()) {
                        trip.setNote(tripNote);
                        //if note also present then we can finish this trip (ie set saved_at date)
                        dnow = new Date();
                        trip.setSavedAt(dnow);
                    } else {
                        //if no note present and private trip (cat=0) then we can finish this trip (note is optional for private trips)
                        if (trip.getCategory() == 0) {
                            dnow = new Date();
                            trip.setSavedAt(dnow);
                        }
                    }
                    mTripViewModel.updateTrip(trip);

                } else {
                    Toast.makeText(
                            getApplicationContext(),
                            "Änderungen offene Fahrt NICHT gespeichert, unbekannte ID.",
                            Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(
                        getApplicationContext(),
                        "Änderungen offene Fahrt NICHT gespeichert...",
                        Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == EXPORT_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                exportEmail = data.getStringExtra(ExportActivity.EXTRA_REPLY_EMAIL);
                exportFrom = data.getLongExtra(ExportActivity.EXTRA_REPLY_EXPORTFROM,0);
                exportTill = data.getLongExtra(ExportActivity.EXTRA_REPLY_EXPORTTILL,0);
                exportCat = data.getStringExtra(ExportActivity.EXTRA_REPLY_EXPORTCATEGORY);
                startExport();
            } else {
                Toast.makeText(
                        getApplicationContext(),
                        "Kein Export...",
                        Toast.LENGTH_LONG).show();
            }

        }

        }


    public void exportData() {
        if (mTripViewModel.openTrips()) { //(mTripViewModel.activeTrips()) {
            Toast.makeText(
                    getApplicationContext(),
                    "Export nicht möglich, nicht alle Fahrten sind gespeichert...",
                    Toast.LENGTH_LONG).show();
        } else {
            //we retrieve data of last trip and past to newtripactivity
            Intent intent = new Intent(MainFBActivity.this, ExportActivity.class);
            Trip lastTrip = mTripViewModel.getLastTrip();
            startActivityForResult(intent, EXPORT_ACTIVITY_REQUEST_CODE);
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks permission */
    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG","Permission is granted");
                return true;
            } else {
                Log.v("TAG","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("TAG","Permission is granted");
            return true;
        }
    }

    private void startExport() {
        if (isExternalStorageWritable() && (isStoragePermissionGranted())) {
                proceedExport();
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    "Export nicht möglich: externe Speicher nicht verfügbar / keine Berechtingung",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            proceedExport();
        }
    }

    private void proceedExport() {
        //we have permission to proceed export
        String dataString;
        String catString;

        File file = null;
        File root = Environment.getExternalStorageDirectory();
        if (root.canWrite()){
            final DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.GERMAN);
            //RETRIEVE TRIPDATA
            List<Trip> trips;
            if (exportCat.equals("nur berufliche Fahrten")) {
                trips = mTripViewModel.GetBusinessTripsForTimeFrame(exportFrom, exportTill);
            } else {
                trips = mTripViewModel.GetTripsForTimeFrame(exportFrom, exportTill);
            }

            if (trips.size() == 0) {
                Toast.makeText(
                        getApplicationContext(),
                        "Keine Fahrten zu exportieren...",
                        Toast.LENGTH_LONG).show();
            } else {
                //CREATE CSV FILE AND EXPORT DATA
                File directoryDownload = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                file = new File(directoryDownload, "fahrtenbuchdata.csv");
                file.delete();
                file = new File(directoryDownload, "fahrtenbuchdata.csv");

                BufferedWriter bw = null;
                try {
                    bw = new BufferedWriter(new FileWriter(file, true));
                    bw.write("Abfahrt,Ort Abfahrt,Km Abfahrt,Ankunf,Ort Ankunft,Km Ankunft, Gefahrene Km, Beruflich");
                    bw.newLine();
                    for (Trip trip : trips) {
                        if (trip.getCategory() == 1) {
                            catString = "X";
                        } else {
                            catString = "";
                        }
                        dataString = df.format(trip.getStart()) + "," +
                                trip.getStartLocation() + "," +
                                trip.getStartKm() + "," +
                                df.format(trip.getFinish()) + "," +
                                trip.getFinishLocation() + "," +
                                trip.getFinishKm() + "," +
                                (trip.getFinishKm() - trip.getStartKm()) + "," +
                                catString;
                        bw.write(dataString);
                        bw.newLine();
                    }

                    //bw.write("sadföslkj ewrwr,kölfjsf ösfkj,12/03/2018 12:45,45353,243" + "\n");
                    bw.flush();
                    bw.close();
                } catch (IOException e) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Export fehlgeschlagen",
                            Toast.LENGTH_LONG).show();

                }
                //SEND EMAIL WITH ATTACHMENT
                try {
                    Uri u1 = null;
                    u1 = Uri.fromFile(file);
                    if (u1 != null) {
                        //ATTENTION
                        //as of android 6 in gmail app
                        //Settings->Apps->Gmail->Permissions and enable the "Storage" permission manually,
                        //otherwise attachment does not work!!!
                        final DateFormat df2 = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN);
                        Intent sendIntent = new Intent(Intent.ACTION_SEND);
                        sendIntent.setType("plain/text");
                        String to[] = {exportEmail};
                        sendIntent.putExtra(Intent.EXTRA_EMAIL, to);
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Fahrtenbuch Export " +
                                df2.format(DateConverters.fromTimestamp(exportFrom)) +
                                " - " +
                                df2.format(DateConverters.fromTimestamp(exportTill)));
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "Fahrtenbuch daten:\n"+
                                exportCat +
                                "\n"+
                                df2.format(DateConverters.fromTimestamp(exportFrom)) +
                                " - " +
                                df2.format(DateConverters.fromTimestamp(exportTill)));
                        sendIntent.putExtra(Intent.EXTRA_STREAM, u1);
                        startActivity(sendIntent);

                        Toast.makeText(
                                getApplicationContext(),
                                "Export fertig",
                                Toast.LENGTH_SHORT).show();

                    }
                } catch (Throwable t) {
                    t.printStackTrace();

                    Toast.makeText(
                            getApplicationContext(),
                            "Email fehlgeschlagen",
                            Toast.LENGTH_LONG).show();
                }

            }

        } else {
            Toast.makeText(
                    getApplicationContext(),
                    "Export fehlgeschlagen:keine Schreibrechte",
                    Toast.LENGTH_LONG).show();
        }


    }


    private void createChart4Past12Months() {
        if (mTripViewModel.openTrips()) { //(mTripViewModel.activeTrips()) {
            Toast.makeText(
                    getApplicationContext(),
                    "Grafik nicht möglich, nicht alle Fahrten sind gespeichert...",
                    Toast.LENGTH_LONG).show();
        } else {
            //DEFINE TIMEFRAME (PAST 12 MONTHS)
            List<Trip> tripsLastYear;
            Date dNow = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(dNow);
            Calendar c2 = Calendar.getInstance();
            c2.set(Calendar.DATE, 1);
            c2.set(Calendar.YEAR, c.get(Calendar.YEAR) - 1);
            c2.set(Calendar.MONTH, c.get(Calendar.MONTH) + 1);
            Date dStart = c2.getTime();
            final DateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN);
            Log.d("TAG", df.format(dStart));

            //RETRIEVE TRIPDATA FOR PAST 12 MONTHS
            tripsLastYear = mTripViewModel.GetTripsForTimeFrame(DateConverters.dateToTimestamp(dStart), DateConverters.dateToTimestamp(dNow));
            if (tripsLastYear.size() == 0) {
                Toast.makeText(
                        getApplicationContext(),
                        "Keine Fahrten verfügbar für Grafik...",
                        Toast.LENGTH_LONG).show();
            } else {
                //PREPARE DATA FOR CHART
                int[] busTrips = new int[12];
                int[] privTrips = new int[12];
                int tripMonth;
                String sBusTrips;
                String sPrivTrips;
                for (Trip trip : tripsLastYear) {
                    //define month
                    c.setTime(trip.getStart());
                    tripMonth = c.get(Calendar.MONTH) + 1; //months are 0 based!!!
                    //add #km to month
                    if (trip.getCategory() == 1) {
                        busTrips[tripMonth - 1] += trip.getFinishKm() - trip.getStartKm();
                    } else {
                        privTrips[tripMonth - 1] += trip.getFinishKm() - trip.getStartKm();
                    }
                }
                StringBuilder sb = new StringBuilder();
                for (int i : busTrips)
                    sb.append(i + "").append(";");
                sBusTrips = sb.substring(0, sb.length() - 1);
                sb.setLength(0);
                for (int i : privTrips)
                    sb.append(i + "").append(";");
                sPrivTrips = sb.substring(0, sb.length() - 1);
                //Log.d("TAG", sBusTrips);
                //Log.d("TAG", sPrivTrips);

                //GOTO CHART:  INTENT AND PASS TRIPDATA
                Intent intent = new Intent(this, ChartActivity.class);
                intent.putExtra(ChartActivity.EXTRA_CHART_BUSTRIPDATA, sBusTrips);
                intent.putExtra(ChartActivity.EXTRA_CHART_PRIVTRIPDATA, sPrivTrips);
                startActivity(intent);

            }
        }
    }


}
