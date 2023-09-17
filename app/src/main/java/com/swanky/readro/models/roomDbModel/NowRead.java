package com.swanky.readro.models.roomDbModel;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class NowRead implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "author")
    public String author;

    @ColumnInfo(name = "numberOfPages")
    public int numberOfPages;

    @ColumnInfo(name = "pageCountRead")
    public int pageCountRead;

    @ColumnInfo(name = "image")
    public byte[] image;


    public NowRead(String title, String author, int numberOfPages, int pageCountRead, byte[] image) {
        this.title = title;
        this.author = author;
        this.numberOfPages = numberOfPages;
        this.pageCountRead = pageCountRead;
        this.image = image;
    }



}
