package swdev.wifi.at.fbapp.db;


import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Database(entities = {Trip.class}, version = 1)
@TypeConverters(DateConverters.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TripDao tripDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        Log.d("TAG","DBINIT");

        synchronized (AppDatabase.class) {
            if (INSTANCE==null) {
                INSTANCE = Room.databaseBuilder(context, AppDatabase.class, "trip_db")
                        .allowMainThreadQueries()
                        .addCallback(sAppDatabaseCallback)
                        .build();
            }
            return INSTANCE;
        }
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    private static AppDatabase.Callback sAppDatabaseCallback = new AppDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new PopulateDbAsync(INSTANCE).execute();
        }
    };

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final TripDao mDao;

        PopulateDbAsync(AppDatabase db) {
            mDao = db.tripDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            /*mDao.deleteAll();*/
            Date d1; // = new Date();
            DateFormat dtf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.GERMAN);

            /*try {
                d1 = dtf.parse("15/05/2018 12:30");
                Trip trip = new Trip(d1,"Stainz bei Straden 113 - 8345 Straden",104);
                trip.setFinishLocation("Körblergasse 111 - 8010 Graz");
                try {
                    d1 = dtf.parse("15/05/2018 13:15");
                    trip.setFinish(d1);
                    trip.setFinishKm(187);
                    trip.setSavedAt(d1);
                    trip.setCategory(1);
                    trip.setNote("Firma XYC: Projektbesprechung");
                    mDao.addTrip(trip);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            try {
                d1 = dtf.parse("17/05/2018 12:30");
                Trip trip = new Trip(d1,"Stainz bei Straden 113 - 8345 Straden",187);
                trip.setFinishLocation("Hauptplatz 1 - 8010 Graz");
                try {
                    d1 = dtf.parse("17/05/2018 13:45");
                    trip.setFinish(d1);
                    trip.setFinishKm(265);
                    trip.setSavedAt(d1);
                    trip.setCategory(0);
                    mDao.addTrip(trip);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }


            try {
                d1 = dtf.parse("03/06/2018 12:30");
                Trip trip = new Trip(d1,"Stainz bei Straden 113 - 8345 Straden",298);
                trip.setFinishLocation("Körblergasse 111 - 8010 Graz");
                try {
                    d1 = dtf.parse("03/06/2018 13:15");
                    trip.setFinish(d1);
                    trip.setFinishKm(375);
                    trip.setSavedAt(d1);
                    trip.setCategory(1);
                    trip.setNote("WIFI Graz: Java Kurs");
                    mDao.addTrip(trip);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            try {
                d1 = dtf.parse("05/06/2018 14:10");
                Trip trip2 = new Trip(d1,"Grazerstraße 23 - 8344 Bad Gleichenberg",375);
                trip2.setFinishLocation("Mühldorfstraße 123 - 8330 Feldbach");
                try {
                    d1 = dtf.parse("05/06/2018 14:53");
                    trip2.setFinish(d1);
                    trip2.setCategory(0);
                    trip2.setSavedAt(d1);
                    trip2.setFinishKm(441);
                    mDao.addTrip(trip2);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            try {
                d1 = dtf.parse("05/09/2017 14:10");
                Trip trip2 = new Trip(d1,"Grazerstraße 23 - 8344 Bad Gleichenberg",10);
                trip2.setFinishLocation("Mühldorfstraße 123 - 8330 Feldbach");
                try {
                    d1 = dtf.parse("05/09/2017 14:53");
                    trip2.setFinish(d1);
                    trip2.setCategory(1);
                    trip2.setSavedAt(d1);
                    trip2.setFinishKm(97);
                    mDao.addTrip(trip2);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }*/


            return null;
        }
    }

}
