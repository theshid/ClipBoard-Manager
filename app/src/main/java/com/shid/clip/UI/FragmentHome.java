package com.shid.clip.UI;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.shid.clip.Adapters.ClipAdapter;
import com.shid.clip.Service.AutoListenService;
import com.shid.clip.Database.AppDatabase;
import com.shid.clip.Database.ClipEntry;
import com.shid.clip.ViewModel.MainViewModel;
import com.shid.clip.R;
import com.shid.clip.Utils.AppExecutor;
import com.shid.clip.Utils.SharedPref;

import java.util.List;

import static androidx.recyclerview.widget.DividerItemDecoration.VERTICAL;

public class FragmentHome extends Fragment implements ClipAdapter.ItemClickListener {
    View view;
    private RecyclerView mRecyclerView;
    private ClipAdapter mAdapter;
    private AppDatabase mDb;
    private SwitchCompat mSwitch;
    private SharedPref sharedPref;
    private boolean isServiceOn = false;
    private TextView emptyView;

    public FragmentHome() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.home_fragment, container, false);

        setUI();
        mDb = AppDatabase.getInstance(getActivity());
        setupViewModel();
        //checkIntent();
        checkPref();
        handleAutoListen();


        return view;


    }

    private void checkIntent() {
        Intent intent = getActivity().getIntent();
        if (intent.getBooleanExtra("service_on", true)) {
            Log.d("Fragment","value of intent " + intent.getBooleanExtra("service_on",true));

                sharedPref.setSwitch(false);
                mSwitch.setChecked(false);
                stopAutoService();


        }
    }

    private void setUI() {
        emptyView = view.findViewById(R.id.empty_view);

        // Set the RecyclerView to its corresponding view
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mSwitch = view.findViewById(R.id.switch1);
        // Set the layout for the RecyclerView to be a linear layout, which measures and
        // positions items within a RecyclerView into a linear list
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new ClipAdapter(getContext(), this);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkEmpty();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                checkEmpty();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkEmpty();
            }

            void checkEmpty() {
                emptyView.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        });
        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), VERTICAL);
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
                        List<ClipEntry> clips = mAdapter.getClipsEntries();
                        mDb.clipDao().deleteClip(clips.get(position));
                    }
                });
                Toast.makeText(getActivity(), "Entry deleted", Toast.LENGTH_LONG).show();
            }
        }).attachToRecyclerView(mRecyclerView);
    }

    private void checkPref() {
        sharedPref = new SharedPref(getActivity());
        if (sharedPref.loadSwitchState()) {
            mSwitch.setChecked(true);
            startAutoService();
        } else {
            mSwitch.setChecked(false);
            stopAutoService();
        }
    }

    private void handleAutoListen() {
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sharedPref.setSwitch(true);
                    startAutoService();
                } else {
                    sharedPref.setSwitch(false);
                    stopAutoService();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        checkIntent();
    }

    private void stopAutoService() {
        isServiceOn = false;
        getActivity().stopService(new Intent(getActivity(), AutoListenService.class));
    }

    private void startAutoService() {
        isServiceOn = true;
        // this.startService(new Intent(MainActivity.this,AutoListenService.class));
        Intent serviceIntent = new Intent(getActivity(), AutoListenService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().startForegroundService(serviceIntent);
        } else {
            getActivity().startService(serviceIntent);
        }
        Toast.makeText(getActivity(), "ShidClip AutoListen enabled...", Toast.LENGTH_SHORT).show();
    }

    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        viewModel.getClips().observe(getActivity(), new Observer<List<ClipEntry>>() {
            @Override
            public void onChanged(@Nullable List<ClipEntry> taskEntries) {
                Log.d("Fragment", "Updating list of tasks from LiveData in ViewModel");
                mAdapter.setClips(taskEntries);
            }
        });
    }

    @Override
    public void onItemClickListener(int itemId) {
        if (isServiceOn) {
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
