package swdev.wifi.at.fbapp.db;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class TripRepository {
    private TripDao mTripDao;
    private LiveData<List<Trip>> mAllTrips;
    private AppDatabase database;
    //private LiveData<List<Trip>> mAllOpenTrips;
    //private LiveData<List<Trip>> mAllActiveTrips;

    TripRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mTripDao = db.tripDao();
        mAllTrips = mTripDao.getAllTrips();
       // mAllOpenTrips = mTripDao.getOpenTrips();
       // mAllActiveTrips = mTripDao.getActiveTrips();
    }

    LiveData<List<Trip>> getAllTrips() {
        return mAllTrips;
    };

    //LiveData<List<Trip>> getAllOpenTrips() {    return mAllOpenTrips;   };

    //LiveData<List<Trip>> getAllActiveTrips() {return mAllActiveTrips;};

    public List<Trip> GetBusinessTripsForTimeFrame(Long dateStart, Long dateEnd) {
        return mTripDao.getBusinessTripsForTimeFrame(dateStart,dateEnd);
    }

    public List<Trip> GetTripsForTimeFrame(Long dateStart, Long dateEnd) {
        return mTripDao.getTripsForTimeFrame(dateStart,dateEnd);
    }

    public List<String> getStartLocations() {return mTripDao.getStartLocations();}

    public List<String> getFinishLocations() {return mTripDao.getFinishLocations();}

    public List<String> getNotes() {return mTripDao.getNotes();}

    public int NrOfActiveTrips() {return mTripDao.getNrOfActiveTrips();};

    public int NrOfOpenTrips() {return  mTripDao.getNrOfOpenTrips();};

    public void addTrip (Trip trip) {
        new insertAsyncTask(mTripDao).execute(trip);
    }

    public void updateTrip (Trip trip) { new editAsyncTask(mTripDao).execute(trip); }

    public Trip getTripById(int id) { return mTripDao.getTripById(id);};

    public Trip getLastTrip() {return mTripDao.getLastTrip(); };

    public void deleteTrip (int id) { new deleteAsyncTask(mTripDao).execute(getTripById(id));};

    public void insert(Trip trip){
        new insertAsyncTask(mTripDao).execute(trip);
    }

    public void update(Trip trip){new UpdateTripTask(mTripDao).execute(trip);}


    private static class UpdateTripTask extends AsyncTask<Trip, Void, Void> {

        private TripDao dao;

        public UpdateTripTask(TripDao dao){this.dao = dao;}

        protected Void doInBackground(Trip... trips) {
            dao.update(trips[0]);
            return null;
        }

    }





    private static class insertAsyncTask extends AsyncTask<Trip, Void, Void> {

        private TripDao mAsyncTaskDao;

        insertAsyncTask(TripDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Trip... params) {
            mAsyncTaskDao.addTrip(params[0]);
            return null;
        }
    }

    /*public void delete(final Trip trips){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                database.tripDao().delete(trips);
                return null;
            }
        }.execute();
    }*/

    public void delete(Trip trip) {new deleteAsyncTask(mTripDao).execute(trip);}

    private static class editAsyncTask extends AsyncTask<Trip, Void, Void> {

        private TripDao mAsyncTaskDao;

        editAsyncTask(TripDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Trip... params) {
            mAsyncTaskDao.updateTrip(params[0]);
            return null;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<Trip, Void, Void> {

        private TripDao mAsyncTaskDao;

        deleteAsyncTask(TripDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Trip... params) {
            mAsyncTaskDao.deleteById(params[0]._id);
            return null;
        }
    }

    private static class delete extends AsyncTask<Trip, Void, Void> {

        private TripDao mAsyncTaskDao;

        public delete(TripDao dao) {
            this.mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Trip... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }


}
