package swdev.wifi.at.fbapp.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface TripDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addTrip (Trip trip);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateTrip (Trip trip);

    @Delete
    void delete(Trip trip);

    @Update
    void update(Trip trip);

    @Query("SELECT * from trips order by start DESC")
    LiveData<List<Trip>> getAllTrips();

    @Query("SELECT COUNT(*) from trips where (saved_at is null) and (finish_km = 0)")
    int getNrOfActiveTrips();

    @Query("SELECT COUNT(*) from trips where (saved_at is null)")
    int getNrOfOpenTrips();

    @Query("SELECT * from trips where saved_at is null") //" and finish_km is null")
    LiveData<List<Trip>> getActiveTrips();

    @Query("SELECT * from trips where saved_at is null and not(finish_km is null) order by start DESC")
    LiveData<List<Trip>> getOpenTrips();

    @Query("DELETE FROM trips")
    void deleteAll();

    @Query("DELETE FROM trips WHERE _id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM trips WHERE _id = :id")
    Trip getTripById(int id);

    @Query("SELECT * FROM trips ORDER BY start DESC LIMIT 1")
    Trip getLastTrip();

    @Query("SELECT * FROM trips WHERE category = 1 AND start BETWEEN :daystart AND :dayend")
    List<Trip> getBusinessTripsForTimeFrame(long daystart, long dayend);

    @Query("SELECT * FROM trips WHERE start BETWEEN :daystart AND :dayend")
    List<Trip> getTripsForTimeFrame(long daystart, long dayend);

    @Query("SELECT DISTINCT start_location FROM trips")
    List<String> getStartLocations();

    @Query("SELECT DISTINCT finish_location FROM trips")
    List<String> getFinishLocations();

    @Query("SELECT DISTINCT note FROM trips WHERE NOT note is null")
    List<String> getNotes();

}

