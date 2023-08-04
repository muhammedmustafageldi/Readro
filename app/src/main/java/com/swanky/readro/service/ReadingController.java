package com.swanky.readro.service;

import android.content.SharedPreferences;

import com.swanky.readro.models.roomDbModel.NowRead;

public class ReadingController {

    private NowRead nowRead;
    private long enteredTime;
    private SharedPreferences sharedPreferences;

    public ReadingController(NowRead nowRead, long enteredTime) {
        this.nowRead = nowRead;
        this.enteredTime = enteredTime;
    }



}
