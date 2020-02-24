package com.shid.clip.UI;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.shid.clip.Adapters.ClipAdapter;
import com.shid.clip.Service.AutoListenService;
import com.shid.clip.Database.AppDatabase;
import com.shid.clip.Database.ClipEntry;
import com.shid.clip.ViewModel.MainViewModel;
import com.shid.clip.R;

import java.util.List;

import static androidx.recyclerview.widget.DividerItemDecoration.VERTICAL;

public class FragmentFavorite extends Fragment implements ClipAdapter.ItemClickListener {
    View view;
    private RecyclerView mRecyclerView;
    private ClipAdapter mAdapter;
    private AppDatabase mDb;
    private TextView emptyView;

    public FragmentFavorite() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.favorite_fragment, container, false);

        setUI();
        mDb = AppDatabase.getInstance(getActivity());
        setupViewModel();

        return view;
    }

    private void setUI() {
        emptyView = view.findViewById(R.id.empty_view);
        // Set the RecyclerView to its corresponding view
        mRecyclerView = view.findViewById(R.id.recyclerView);
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
    }

    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        viewModel.getFavorites().observe(getActivity(), new Observer<List<ClipEntry>>() {
            @Override
            public void onChanged(@Nullable List<ClipEntry> taskEntries) {
                Log.d("Fragment", "Updating list of tasks from LiveData in ViewModel");
                mAdapter.setClips(taskEntries);

            }
        });
    }

    @Override
    public void onItemClickListener(int itemId) {
        if (AutoListenService.isServiceRunning) {
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

    private void startAutoService() {
        Intent serviceIntent = new Intent(getActivity(), AutoListenService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().startForegroundService(serviceIntent);
        } else {
            getActivity().startService(serviceIntent);
        }
        Toast.makeText(getActivity(), "ShidClip AutoListen enabled...", Toast.LENGTH_SHORT).show();
    }

    private void stopAutoService() {
        getActivity().stopService(new Intent(getActivity(), AutoListenService.class));
    }
}
