package com.shid.clip.ViewModel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.shid.clip.Database.AppDatabase;
import com.shid.clip.Database.ClipEntry;
import com.shid.clip.Utils.AppExecutor;

import java.util.Date;
import java.util.List;

public class MainViewModel extends AndroidViewModel {

    // Constant for logging
    private static final String TAG = MainViewModel.class.getSimpleName();

    private LiveData<List<ClipEntry>> clips;
    private LiveData<List<ClipEntry>> favorites;
    private AppDatabase database;

    public MainViewModel(Application application) {
        super(application);
        database = AppDatabase.getInstance(this.getApplication());
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

    public void addTextInDb(final String text){
        AppExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Date date = new Date();
                ClipEntry clipEntry = new ClipEntry(text,date,0);
                database.clipDao().insertClip(clipEntry);
            }
        });
    }
}
