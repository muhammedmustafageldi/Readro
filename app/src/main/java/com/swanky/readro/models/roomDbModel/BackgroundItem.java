package com.swanky.readro.models.roomDbModel;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class BackgroundItem {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "imageResource")
    private int imageResource;
    @ColumnInfo(name = "imagePrice")
    private int imagePrice;
    @ColumnInfo(name = "available")
    private boolean available;

    public BackgroundItem(int imageResource, int imagePrice, boolean available) {
        this.imageResource = imageResource;
        this.imagePrice = imagePrice;
        this.available = available;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public int getImagePrice() {
        return imagePrice;
    }

    public void setImagePrice(int imagePrice) {
        this.imagePrice = imagePrice;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
