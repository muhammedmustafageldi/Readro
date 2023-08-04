package com.swanky.readro.models;

import com.google.gson.annotations.SerializedName;

public class ImageLinks {
    @SerializedName("thumbnail")
    private String thumbnail;

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
