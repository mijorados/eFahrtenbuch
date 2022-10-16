package swdev.wifi.at.fbapp.db;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.View;

import java.util.List;

public class TripViewModel extends AndroidViewModel {
    private TripRepository mRepository;
    private LiveData<List<Trip>> mAllTrips;
    private AppDatabase database;
    //private LiveData<List<Trip>> mAllOpenTrips;
    //private LiveData<List<Trip>> mAllActiveTrips;
    public TripViewModel(@NonNull Application application) {
        super(application);
        mRepository = new TripRepository(application);
        mAllTrips = mRepository.getAllTrips();
  //      mAllOpenTrips = mRepository.getAllOpenTrips();
  //      mAllActiveTrips = mRepository.getAllActiveTrips();
    }

    public LiveData<List<Trip>> getAllTrips() {
        return mAllTrips;
    }

    public List<Trip> GetBusinessTripsForTimeFrame(Long dateStart, Long dateEnd) { return mRepository.GetBusinessTripsForTimeFrame(dateStart,dateEnd); }

    public List<Trip> GetTripsForTimeFrame(Long dateStart, Long dateEnd) { return mRepository.GetTripsForTimeFrame(dateStart,dateEnd); }

    public List<String> getStartLocations() { return mRepository.getStartLocations(); }

    public List<String> getFinishLocations() { return mRepository.getFinishLocations(); }

    public List<String> getNotes() {return mRepository.getNotes(); }

//    public LiveData<List<Trip>> getAllOpenTrips() { return mAllOpenTrips;}

//    public LiveData<List<Trip>> getAllActiveTrips() { return mAllActiveTrips;}

    public boolean activeTrips() {return (mRepository.NrOfActiveTrips() > 0 ? true : false);}

    public boolean openTrips() { return  (mRepository.NrOfOpenTrips() > 0 ? true : false);}

    public Trip getTripById(int id) { return mRepository.getTripById(id);};

    public Trip getLastTrip() { return  mRepository.getLastTrip();};

    public void addTrip(Trip trip) {
        mRepository.addTrip(trip);
    }

    public void deleteTrip(int id) {mRepository.deleteTrip(id);}

    public void insert(Trip trip){mRepository.insert(trip);}

    public void delete(Trip trip) {mRepository.delete(trip);}

    public void update(Trip trip){mRepository.update(trip);}

    public void updateTrip(Trip trip) { mRepository.updateTrip(trip); }
}
