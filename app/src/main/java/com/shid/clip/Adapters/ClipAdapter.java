package com.shid.clip.Adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.shid.clip.Database.AppDatabase;
import com.shid.clip.Database.ClipEntry;
import com.shid.clip.R;
import com.shid.clip.Utils.AppExecutor;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkEventListener;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ClipAdapter extends RecyclerView.Adapter<ClipAdapter.ClipViewHolder> {

    // Constant for date format
    private static final String DATE_FORMAT = "dd/MM/yyy";
    private AppDatabase mDb;

    // Member variable to handle item clicks
    final private ItemClickListener mItemClickListener;

    // Class variables for the List that holds task data and the Context
    private List<ClipEntry> mClipEntries;
    private Context mContext;
    // Date formatter
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

    public ClipAdapter(Context context, ItemClickListener listener) {
        mContext = context;
        mItemClickListener = listener;
    }

    @Override
    public ClipViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the task_layout to a view
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.clip_layout, parent, false);

        return new ClipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClipViewHolder holder, int position) {
        mDb = AppDatabase.getInstance(mContext);
        // Determine the values of the wanted data
        final ClipEntry clipEntry = mClipEntries.get(position);
        String clip = clipEntry.getEntry();
        String date = dateFormat.format(clipEntry.getDate());
        final int clip_id = clipEntry.getClipId();
        int favorite_status = clipEntry.getFavorite();
        Log.d("Adapter","value of fav" + favorite_status);

        //Set values
        holder.clipView.setText(clip);
        holder.dateClip.setText(date);
     //Set the button active or inactive depending if the clips was bookmarked or not
        if (favorite_status ==0){
            holder.favorite.setChecked(false);
            Log.d("Adapter","value of button "+holder.favorite.isChecked());
        } else{
            holder.favorite.setChecked(true);
            Log.d("Adapter","value of button "+holder.favorite.isChecked());
        }
        //If the clip is bookmarked or unchecked,change the status in the database
        holder.favorite.setEventListener(new SparkEventListener() {
            @Override
            public void onEvent(ImageView button, boolean buttonState) {
                if (buttonState){
                    //clipEntry.setFavorite(1);
                    AppExecutor.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            mDb.clipDao().update(1,clip_id);

                        }
                    });
                    Log.d("Adapter","new value active"+clipEntry.getFavorite());
                } else{
                    //clipEntry.setFavorite(0);
                    AppExecutor.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            mDb.clipDao().update(0,clip_id);

                        }
                    });
                    Log.d("Adapter","new value inactive"+clipEntry.getFavorite());
                }
            }

            @Override
            public void onEventAnimationEnd(ImageView button, boolean buttonState) {

            }

            @Override
            public void onEventAnimationStart(ImageView button, boolean buttonState) {

            }
        });

    }

    /**
     * Returns the number of items to display.
     */
    @Override
    public int getItemCount() {
        if (mClipEntries == null) {
            return 0;
        }
        return mClipEntries.size();
    }

    public List<ClipEntry> getClipsEntries() {
        return mClipEntries;
    }

    /**
     * When data changes, this method updates the list of clipEntries
     * and notifies the adapter to use the new values on it
     */
    public void setClips(List<ClipEntry> clipEntries) {
        mClipEntries = clipEntries;
        notifyDataSetChanged();
    }

    public interface ItemClickListener {
        void onItemClickListener(int itemId);
    }

    // Inner class for creating ViewHolders
    class ClipViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Class variables for the task description and priority TextViews
        TextView clipView;
        TextView dateClip;
        SparkButton favorite;


        /**
         * Constructor for the TaskViewHolders.
         *
         * @param itemView The view inflated in onCreateViewHolder
         */
        public ClipViewHolder(View itemView) {
            super(itemView);

            clipView = itemView.findViewById(R.id.clip_entry);
            dateClip = itemView.findViewById(R.id.clipDate);
            favorite = itemView.findViewById(R.id.favorite_button);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int elementId = getAdapterPosition();
            mItemClickListener.onItemClickListener(elementId);

        }
    }


}
