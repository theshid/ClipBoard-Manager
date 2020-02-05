package com.shid.clip;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.shid.clip.Database.ClipEntry;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ClipAdapter extends RecyclerView.Adapter<ClipAdapter.ClipViewHolder> {

    // Constant for date format
    private static final String DATE_FORMAT = "dd/MM/yyy";

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
        // Determine the values of the wanted data
        ClipEntry clipEntry = mClipEntries.get(position);
        String clip = clipEntry.getEntry();
        String date = dateFormat.format(clipEntry.getDate());

        //Set values
        holder.clipView.setText(clip);
        holder.dateClip.setText(date);

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

    public List<ClipEntry> getTasks() {
        return mClipEntries;
    }

    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    public void setTasks(List<ClipEntry> clipEntries) {
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


        /**
         * Constructor for the TaskViewHolders.
         *
         * @param itemView The view inflated in onCreateViewHolder
         */
        public ClipViewHolder(View itemView) {
            super(itemView);

            clipView = itemView.findViewById(R.id.clip_entry);
            dateClip = itemView.findViewById(R.id.clipDate);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int elementId = mClipEntries.get(getAdapterPosition()).getClipId();
            mItemClickListener.onItemClickListener(elementId);
            TextView name = view.findViewById(R.id.clip_entry);
            String entry = name.getText().toString();

            ClipboardManager clipboardManager = (ClipboardManager)mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("Copied text",entry);
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(view.getContext(),"Le texte a été copié",Toast.LENGTH_SHORT).show();
        }
    }


}
