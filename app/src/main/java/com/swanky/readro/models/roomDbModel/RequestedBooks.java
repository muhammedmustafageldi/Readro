package com.swanky.readro.models.roomDbModel;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class RequestedBooks {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "author")
    public String author;

    @ColumnInfo(name = "numberOfPages")
    public int numberOfPages;

    @ColumnInfo(name = "aboutTheBook")
    public String aboutTheBook;

    @ColumnInfo(name = "image")
    public byte[] image;

    public RequestedBooks(String title, String author, int numberOfPages, String aboutTheBook, byte[] image) {
        this.title = title;
        this.author = author;
        this.numberOfPages = numberOfPages;
        this.aboutTheBook = aboutTheBook;
        this.image = image;
    }
}
