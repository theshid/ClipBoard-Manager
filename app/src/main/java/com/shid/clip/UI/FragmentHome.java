package com.shid.clip.UI;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.shid.clip.Adapters.ClipAdapter;
import com.shid.clip.Service.AutoListenService;
import com.shid.clip.Database.AppDatabase;
import com.shid.clip.Database.ClipEntry;
import com.shid.clip.ViewModel.MainViewModel;
import com.shid.clip.R;
import com.shid.clip.Utils.AppExecutor;
import com.shid.clip.Utils.SharedPref;

import java.io.Serializable;
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
    private List<ClipEntry> clips;
    private LinearLayout rootLayout;





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
            Log.d("Fragment", "value of intent " + intent.getBooleanExtra("service_on", true));

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
        rootLayout = view.findViewById(R.id.rootLayout);
        // Set the layout for the RecyclerView to be a linear layout, which measures and
        // positions items within a RecyclerView into a linear list
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new ClipAdapter(getContext(), this);
        mRecyclerView.setAdapter(mAdapter);
        clips = mAdapter.getClipsEntries();

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
                        clips = mAdapter.getClipsEntries();
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
        MainViewModel viewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);
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
        showOptionDialog(itemId);
    }

    private void showOptionDialog(final int clipId) {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Menu");
        dialog.setMessage("Choose your option");
        Log.d("TAG", "value of position: " + clipId);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View option_layout = inflater.inflate(R.layout.dialog_layout, null);

        dialog.setView(option_layout);


        dialog.setPositiveButton("Copy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                List<ClipEntry> clips = mAdapter.getClipsEntries();
                ClipEntry clipEntry = clips.get(clipId);
                String clipName = clipEntry.getEntry();

                ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Copied text", clipName);
                clipboardManager.setPrimaryClip(clipData);
                Snackbar.make(rootLayout, "Text has been copied", Snackbar.LENGTH_SHORT)
                        .show();
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
        });

        dialog.setNegativeButton("Edit Clip", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showEditDialog(clipId);
            }
        });


        dialog.show();

    }

    private void showEditDialog(final int clipId) {
        List<ClipEntry> clips = mAdapter.getClipsEntries();
        Intent intent = new Intent(getContext(), EditActivity.class);

        intent.putExtra("position", clipId);

        intent.putExtra("list", (Serializable) clips);
        startActivity(intent);
    }
}
