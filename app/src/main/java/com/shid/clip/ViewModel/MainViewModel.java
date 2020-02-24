package com.shid.clip.ViewModel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.shid.clip.Database.AppDatabase;
import com.shid.clip.Database.ClipEntry;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    // Constant for logging
    private static final String TAG = MainViewModel.class.getSimpleName();

    private LiveData<List<ClipEntry>> clips;
    private LiveData<List<ClipEntry>> favorites;

    public MainViewModel(Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving the tasks from the DataBase");
        clips = database.clipDao().loadAllClips();
        favorites = database.clipDao().loadFavoriteClips();
    }

    public LiveData<List<ClipEntry>> getClips() {
        return clips;
    }

    public LiveData<List<ClipEntry>> getFavorites(){
        return favorites;
    }
}
