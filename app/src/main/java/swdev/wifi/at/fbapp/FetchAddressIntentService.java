package swdev.wifi.at.fbapp;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class FetchAddressIntentService extends IntentService {
    public static final String TAG = FetchAddressIntentService.class.getSimpleName();
    private ResultReceiver receiver;

    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent == null) {
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // Wir verwenden den Receiver, um das Ergebnis zurückzusenden
        receiver = intent.getParcelableExtra(Constants.RECEIVER);
        // Alternativ : receiver = intent.getExtras().getParcelable(Constants.RECEIVER);

        // Location, an der wir uns befinden, aus dem Extra-Objekt holen
        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);

        // Liste mit Ergebnissen des Geocoders
        List<Address> addresses = null;

        String errorMessage = "";

        try {
            // Adresse(n) aus Koordinaten bekommen
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1       // nur 1 Resultat
            );
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "onHandleIntent: Service ist nicht verfügbar", e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "onHandleIntent: Ungültige Long/Lat-Werte", e);
        }


        if (addresses == null || addresses.size() == 0) {
            // Keine Ergebnisse
            deliverResults(Constants.FAILURE_RESULT, "Keine Adresse gefunden");

        } else {
            // Wir haben Ergebnisse
            Address address = addresses.get(0);

            ArrayList<String> addressFragments = new ArrayList<>();

            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }

            String addressLine = TextUtils.join(System.getProperty("line.separator"), addressFragments);
            deliverResults(Constants.SUCCESS_RESULT, addressLine);
        }
    }

    private void deliverResults(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        receiver.send(resultCode, bundle);
    }

    public final class Constants {
        public static final int SUCCESS_RESULT = 0;
        public static final int FAILURE_RESULT = 1;
        public static final String RECEIVER = "receiver";
        public static final String RESULT_DATA_KEY = "result_data_key";
        public static final String LOCATION_DATA_EXTRA = "location_data_extra";
    }

}
