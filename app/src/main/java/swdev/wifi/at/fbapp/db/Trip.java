package swdev.wifi.at.fbapp.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;

@Entity(tableName = "trips")
public class Trip {

    @PrimaryKey(autoGenerate = true)
    public int _id;

    @NonNull
    public Date start;

    @NonNull
    @ColumnInfo(name="start_location")
    private String startLocation;

    @NonNull
    @ColumnInfo(name="start_km")
    private int startKm;

    private Date finish;

    @ColumnInfo(name="finish_location")
    private String finishLocation;

    @ColumnInfo(name="finish_km")
    private int finishKm;

    private int category;

    private String note;

    @ColumnInfo(name="saved_at")
    private Date savedAt;


    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    @NonNull
    public Date getStart() {
        return start;
    }

    public void setStart(@NonNull Date start) {
        this.start = start;
    }

    @NonNull
    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(@NonNull String startLocation) {
        this.startLocation = startLocation;
    }

    public int getStartKm() {
        return startKm;
    }

    public void setStartKm(int startKm) {
        this.startKm = startKm;
    }

    public Date getFinish() {
        return finish;
    }

    public void setFinish(Date finish) {
        this.finish = finish;
    }

    public String getFinishLocation() {
        return finishLocation;
    }

    public void setFinishLocation(String finishLocation) {
        this.finishLocation = finishLocation;
    }

    public int getFinishKm() {
        return finishKm;
    }

    public void setFinishKm(int finishKm) {
        this.finishKm = finishKm;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(Date savedAt) {
        this.savedAt = savedAt;
    }

    public Trip(@NonNull Date start, @NonNull String startLocation, @NonNull int startKm) {
        this.start = start;
        this.startLocation = startLocation;
        this.startKm = startKm;
    }

    public Trip(@NonNull Date start, @NonNull String startLocation, @NonNull int startKm, String startcat, String startnote) {
        this.start = start;
        this.startLocation = startLocation;
        this.startKm = startKm;
        if(startnote != null && !startnote.isEmpty()) {
            this.note = startnote;
        }
        this.category = 0;
        // cat = 1 for professional trips
        if(startcat != null && !startcat.isEmpty() && (startcat.equals("beruflich"))) {
            this.category = 1;
        }
    }

    @Override
    public String toString() {
        return "Trip{" +
                "_id=" + _id +
                ", start=" + start +
                ", startLocation='" + startLocation + '\'' +
                ", startKm=" + startKm +
                ", savedAt=" + savedAt +
                '}';
    }

}
