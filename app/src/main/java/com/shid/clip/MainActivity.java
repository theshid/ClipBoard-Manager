package com.shid.clip;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.shid.clip.Database.AppDatabase;
import com.shid.clip.Database.ClipEntry;
import com.shid.clip.Utils.AppExecutor;

import java.util.List;

import static androidx.recyclerview.widget.DividerItemDecoration.VERTICAL;

public class MainActivity extends AppCompatActivity implements ClipAdapter.ItemClickListener {

    // Constant for logging
    private static final String TAG = MainActivity.class.getSimpleName();
    // Member variables for the adapter and RecyclerView
    private RecyclerView mRecyclerView;
    private ClipAdapter mAdapter;
    private Switch mSwitch;
    private boolean isServiceOn= false;

    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSwitch = findViewById(R.id.switch1);

        // Set the RecyclerView to its corresponding view
        mRecyclerView = findViewById(R.id.recyclerView);

        // Set the layout for the RecyclerView to be a linear layout, which measures and
        // positions items within a RecyclerView into a linear list
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new ClipAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        mRecyclerView.addItemDecoration(decoration);

         /*
         Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         and uses callbacks to signal when a user is performing these actions.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Here is where you'll implement swipe to delete
                AppExecutor.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        int position = viewHolder.getAdapterPosition();
                        List<ClipEntry> clips = mAdapter.getTasks();
                        mDb.clipDao().deleteClip(clips.get(position));
                    }
                });
                Toast.makeText(getApplicationContext(),"Entry deleted",Toast.LENGTH_LONG).show();
            }
        }).attachToRecyclerView(mRecyclerView);

        mDb = AppDatabase.getInstance(getApplicationContext());
        setupViewModel();
        handleAutoListen();
    }

    private void handleAutoListen() {
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startAutoService();
                } else {
                    stopAutoService();
                }
            }
        });
    }

    private void stopAutoService() {
        isServiceOn = false;
        this.stopService(new Intent(MainActivity.this, AutoListenService.class));
    }

    private void startAutoService() {
        isServiceOn = true;
        // this.startService(new Intent(MainActivity.this,AutoListenService.class));
        Intent serviceIntent = new Intent(MainActivity.this, AutoListenService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            MainActivity.this.startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        Toast.makeText(MainActivity.this, "ShidClip AutoListen enabled...", Toast.LENGTH_SHORT).show();
    }

    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getClips().observe(this, new Observer<List<ClipEntry>>() {
            @Override
            public void onChanged(@Nullable List<ClipEntry> taskEntries) {
                Log.d(TAG, "Updating list of tasks from LiveData in ViewModel");
                mAdapter.setTasks(taskEntries);
            }
        });
    }

    @Override
    public void onItemClickListener(int itemId) {
    if (isServiceOn = true){
        stopAutoService();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
               startAutoService();
            }
        }, 500);
    }

    }
}
