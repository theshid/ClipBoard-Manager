package com.shid.clip.Database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ClipDAO {

    @Query("SELECT * FROM clip ORDER BY date")
    LiveData<List<ClipEntry>> loadAllClips();

    @Insert
    void insertClip(ClipEntry clipEntry);

    @Delete
    void deleteClip(ClipEntry clipEntry);




}
