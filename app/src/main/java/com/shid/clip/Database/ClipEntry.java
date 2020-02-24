package com.shid.clip.Database;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "clip")
public class ClipEntry {

    @PrimaryKey(autoGenerate = true)
    private int clipId;
    private String entry;
    private Date date;
    private int favorite;

    @Ignore
    public ClipEntry(String entry, Date date, int favorite) {
        this.entry = entry;
        this.date = date;
        this.favorite = favorite;
    }


    public ClipEntry(int clipId, String entry, Date date, int favorite) {
        this.clipId = clipId;
        this.entry = entry;
        this.date = date;
        this.favorite = favorite;
    }

    public int getFavorite() {
        return favorite;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }

    public int getClipId() {
        return clipId;
    }

    public void setClipId(int clipId) {
        this.clipId = clipId;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
