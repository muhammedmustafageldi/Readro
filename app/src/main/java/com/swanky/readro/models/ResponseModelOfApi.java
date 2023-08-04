package com.swanky.readro.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ResponseModelOfApi {
    @SerializedName("items")
    private List<Item> items;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

}
