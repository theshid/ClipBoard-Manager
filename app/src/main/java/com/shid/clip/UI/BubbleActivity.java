package com.shid.clip.UI;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.shid.clip.Adapters.ClipAdapter;
import com.shid.clip.Database.AppDatabase;
import com.shid.clip.Database.ClipEntry;
import com.shid.clip.R;
import com.shid.clip.ViewModel.MainViewModel;

import java.util.List;

import static androidx.recyclerview.widget.DividerItemDecoration.VERTICAL;

public class BubbleActivity extends AppCompatActivity implements ClipAdapter.ItemClickListener{
    private RecyclerView mRecycleView;
    private ClipAdapter mAdapter;
    private AppDatabase mDb;
    private List<ClipEntry> clips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bubble);

        mRecycleView = findViewById(R.id.recyclerView);
        mRecycleView.setLayoutManager(new LinearLayoutManager(this));
        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new ClipAdapter(this, this);
        mRecycleView.setAdapter(mAdapter);
        DividerItemDecoration decoration = new DividerItemDecoration(this, VERTICAL);
        mRecycleView.addItemDecoration(decoration);
        clips = mAdapter.getClipsEntries();
        mDb = AppDatabase.getInstance(this);
        setupViewModel();
    }

    private void setupViewModel() {
        MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getClips().observe(this, new Observer<List<ClipEntry>>() {
            @Override
            public void onChanged(@Nullable List<ClipEntry> taskEntries) {
                Log.d("Fragment", "Updating list of tasks from LiveData in ViewModel");
                mAdapter.setClips(taskEntries);
            }
        });
    }

    @Override
    public void onItemClickListener(int itemId) {

    }
}