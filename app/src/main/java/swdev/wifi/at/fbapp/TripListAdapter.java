package swdev.wifi.at.fbapp;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import swdev.wifi.at.fbapp.db.Trip;


public class TripListAdapter extends RecyclerView.Adapter<TripListAdapter.TripViewHolder> {

    private View.OnClickListener viewClickListener;
    private OnItemClickListener listener;

    private Context context;

    class TripViewHolder extends RecyclerView.ViewHolder {
        private final TextView tripItemView;
        private final TextView TV_StartLoc;
        private final TextView TV_EndLoc;
        private final TextView TV_Summary;
        private final ImageButton BT_Save;


        private TripViewHolder(View itemView) {
            super(itemView);
            tripItemView = itemView.findViewById(R.id.TV_itemtest);
            TV_StartLoc = itemView.findViewById(R.id.TV_itemstartloc);
            TV_EndLoc = itemView.findViewById(R.id.TV_itemendloc);
            TV_Summary = itemView.findViewById(R.id.TV_itemsummary);
            BT_Save = itemView.findViewById(R.id.BT_itemsave);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(mTrips.get(position));
                    }
                }
            });
        }
    }

    private final LayoutInflater mInflater;
    private List<Trip> mTrips; // Cached copy of trips
    //private List<Trip> mActiveTrips;
    private DateFormat df = new SimpleDateFormat("EEE dd MMM yyyy, HH:mm",
            Locale.GERMAN);

    public TripListAdapter(Context context, View.OnClickListener viewonClickListener) {
        mInflater = LayoutInflater.from(context);
        viewClickListener = viewonClickListener;
    }

    public Context getContext() {
        return context;
    }

    public Trip getNoteAt(int position) {
        if (mTrips != null) {
            return mTrips.get(position);
        }
        return null;
    }


    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new TripViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final TripViewHolder holder, int position) {
        if (mTrips != null) {
            final Trip trip = mTrips.get(position);
            holder.tripItemView.setText(df.format(trip.getStart()));
            holder.TV_StartLoc.setText(trip.getStartLocation());
            holder.TV_EndLoc.setText(trip.getFinishLocation());
            String snote = trip.getNote();
            String scat;
            if (trip.getCategory() == 1) {
                scat = "beruflich - ";
            } else {
                scat = "";
            }

            //depending on saved or not we show 'edit' button for this trip
            Date dSaved = trip.getSavedAt();
            if (dSaved != null) {
                //holder.TV_EndLoc.setText(df.format(dSaved)+trip.getStartLocation());   //trip.getStartLocation());
                holder.BT_Save.setVisibility(View.GONE);
            } else {
                holder.BT_Save.setVisibility(View.VISIBLE);
                //bind edit button to onclicklistener
                holder.BT_Save.setTag(trip);
                holder.BT_Save.setOnClickListener(viewClickListener);
            }

            //define trip length
            String tripLength;
            if (trip.getFinishKm() > 0) {
                tripLength = (trip.getFinishKm() - trip.getStartKm()) + " km";
            } else {
                tripLength = "??? km";
            }

            //define summary text using trip length, category and note
            if (snote != null && !snote.isEmpty()) {
                if (snote.length() > 20) {
                    holder.TV_Summary.setText(tripLength + " - " + scat + snote.substring(0, 20) + "...");
                } else {
                    holder.TV_Summary.setText(tripLength + " - " + scat + snote);
                }
            } else {
                if (scat.length() > 2) {
                    holder.TV_Summary.setText(tripLength + " - " + scat.substring(0, 9));
                } else {
                    holder.TV_Summary.setText(tripLength);
                }
            }

            //alternating row background colors
            if (position % 2 == 1) {
                holder.itemView.setBackgroundColor(Color.parseColor("#EEEEEE"));
            } else {
                holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }

            //overwrite default background for open trip (only 1 open trip possible)
            int iFinishKm = trip.getFinishKm();
            if (dSaved == null && trip.getFinishKm() == 0) {
                int myColor = ContextCompat.getColor(holder.tripItemView.getContext(), R.color.colorAccentLight);
                holder.itemView.setBackgroundColor(myColor);
            }

        } else {
            holder.tripItemView.setText("KEINE DATEN...");
        }
    }


    public void setTrips(List<Trip> trips) {
        this.mTrips = trips;
        notifyDataSetChanged();
    }


    public Trip getTripAt(int position) {
        return mTrips.get(position);
    }


    // getItemCount() is called many times, and when it is first called,
    // mTrips has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mTrips != null)
            return mTrips.size();
        else return 0;
    }

    public interface OnItemClickListener {
        void onItemClick(Trip trip);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


}
