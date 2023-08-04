package com.swanky.readro.models.roomDbModel;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class FinishedBooks {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "author")
    public String author;

    @ColumnInfo(name = "numberOfPages")
    public int numberOfPages;

    @ColumnInfo(name = "endDate")
    public long endDate;

    @ColumnInfo(name = "image")
    public byte[] image;

    public FinishedBooks(String title, String author, int numberOfPages, long endDate, byte[] image) {
        this.title = title;
        this.author = author;
        this.numberOfPages = numberOfPages;
        this.endDate = endDate;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }
}
