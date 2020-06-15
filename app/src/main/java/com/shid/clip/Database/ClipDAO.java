package com.shid.clip.Database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ClipDAO {

    @Query("SELECT * FROM clip ORDER BY date DESC")
    LiveData<List<ClipEntry>> loadAllClips();

    @Insert
    void insertClip(ClipEntry clipEntry);

    @Delete
    void deleteClip(ClipEntry clipEntry);

    @Query("SELECT * FROM clip WHERE favorite = 1 ORDER BY date")
    LiveData<List<ClipEntry>> loadFavoriteClips();

    @Query("UPDATE clip SET favorite = :fav WHERE clipId = :id")
    void update(int fav,int id);

    @Query("UPDATE clip SET entry = :ent WHERE clipId = :id")
    void updateClip(String ent, int id);




}
