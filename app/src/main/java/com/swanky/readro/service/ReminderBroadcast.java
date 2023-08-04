package com.swanky.readro.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SendNotification sendNotification = new SendNotification(context);
        sendNotification.sendReminderNotification();
    }
}
