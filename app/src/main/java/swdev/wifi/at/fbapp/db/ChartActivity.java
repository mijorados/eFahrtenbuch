package swdev.wifi.at.fbapp.db;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import swdev.wifi.at.fbapp.R;

public class ChartActivity extends AppCompatActivity {

    public static final String EXTRA_CHART_BUSTRIPDATA = "xtrabustripdata";
    public static final String EXTRA_CHART_PRIVTRIPDATA = "xtraprivtripdata";

    BarChart chart ;
    ArrayList<BarEntry> BARENTRY ;

    ArrayList<BarEntry> group1 ;
    ArrayList<BarEntry> group2 ;

    private String busTripData;
    private String privTripData;
    private String[] busTripList;
    private String[] privTripList;

    ArrayList<String> BarEntryLabels ;
    BarDataSet Bardataset ;
    BarDataSet Bardataset1 ;
    BarDataSet Bardataset2 ;
    ArrayList<BarDataSet> dataSets;


    BarData BARDATA ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        //GET TRIPDATA FROM INTENT
        busTripData = getIntent().getExtras().getString(EXTRA_CHART_BUSTRIPDATA);
        privTripData = getIntent().getExtras().getString(EXTRA_CHART_PRIVTRIPDATA);
        busTripList = busTripData.split(";");
        privTripList = privTripData.split(";");
        if ((busTripList.length != 12) || (privTripList.length != 12)) {
            Toast.makeText(
                    getApplicationContext(),
                    "Fehler bei Berechnung Daten...",
                    Toast.LENGTH_LONG).show();
            finish();
        }

        //PREPARE labels and bardata with past 12 months
        ArrayList<BarEntry> bargroup1 = new ArrayList<>();
        ArrayList<BarEntry> bargroup2 = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<String>();

        Date d = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        int month = c.get(Calendar.MONTH) + 1; //months are 0-based
        DateFormatSymbols dfs = new DateFormatSymbols(Locale.GERMANY);
        String[] germanyMonths = dfs.getMonths();
        int barEntryIndex = 0;
        for (int i = month + 1; i < germanyMonths.length; i++) {
            labels.add(germanyMonths[i - 1]);
            bargroup1.add(new BarEntry(Integer.parseInt(busTripList[i-1]), barEntryIndex));
            bargroup2.add(new BarEntry(Integer.parseInt(privTripList[i-1]), barEntryIndex));
            barEntryIndex += 1;
        }
        for (int i = 1; i <= month; i++) {
            labels.add(germanyMonths[i - 1]);
            bargroup1.add(new BarEntry(Integer.parseInt(busTripList[i-1]), barEntryIndex));
            bargroup2.add(new BarEntry(Integer.parseInt(privTripList[i-1]), barEntryIndex));
            barEntryIndex += 1;
        }

        // creating dataset for Bar Group1
        BarDataSet barDataSet1 = new BarDataSet(bargroup1, "berufliche Fahrten");
        barDataSet1.setColor(Color.rgb(0, 155, 0));
        barDataSet1.setDrawValues(false);

        // creating dataset for Bar Group 2
        BarDataSet barDataSet2 = new BarDataSet(bargroup2, "private Fahrten");
        barDataSet2.setDrawValues(false);

        ArrayList<BarDataSet> dataSets = new ArrayList<>();  // combined all dataset into an arraylist
        dataSets.add(barDataSet1);
        dataSets.add(barDataSet2);

        // initialize the Bardata with argument labels and dataSets
        BarData data = new BarData(labels, dataSets);
        chart = (BarChart) findViewById(R.id.chart1);
        chart.setData(data);
        chart.setDescription("Gefahrene Km (letzte 12 Monate)");
        chart.animateY(3000);
    }

}
