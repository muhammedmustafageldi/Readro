package com.swanky.readro.models.roomDbModel;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ReadingValues {

    @PrimaryKey
    private int id = 1;

    @ColumnInfo(name = "nowReadId")
    private int nowReadId;

    @ColumnInfo(name = "selectedTime")
    private int selectedTime;

    @ColumnInfo(name = "startTime")
    private long startTime;

    @ColumnInfo(name = "focusMode")
    private boolean focusMode;

    public ReadingValues(int nowReadId, int selectedTime, long startTime, boolean focusMode) {
        this.nowReadId = nowReadId;
        this.selectedTime = selectedTime;
        this.startTime = startTime;
        this.focusMode = focusMode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNowReadId() {
        return nowReadId;
    }

    public void setNowReadId(int nowReadId) {
        this.nowReadId = nowReadId;
    }

    public int getSelectedTime() {
        return selectedTime;
    }

    public void setSelectedTime(int selectedTime) {
        this.selectedTime = selectedTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public boolean isFocusMode() {
        return focusMode;
    }

    public void setFocusMode(boolean focusMode) {
        this.focusMode = focusMode;
    }
}
