package com.swanky.readro.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import com.swanky.readro.R;
import com.swanky.readro.activities.MainActivity;
import com.swanky.readro.activities.ReminderActivity;

public class SendNotification {

    private final Context context;


    public SendNotification (Context context){
        this.context = context;
    }

    public void sendReminderNotification(){
        if (ReminderActivity.isItSet){
            //Random quotes



        }else{

            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent, PendingIntent.FLAG_IMMUTABLE);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            String channelId = "myChannelId";
            String channelName = "myChannelName";
            String channelDescription = "myChannelDescription";
            int channelImportant = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(channelId,channelName,channelImportant);
            channel.setDescription(channelDescription);

            notificationManager.createNotificationChannel(channel);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context,channelId)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentText("Hedeflenen saat için okuma zamanı.")
            .setContentTitle("Okuma Zamanı.")
            .setSmallIcon(R.drawable.r)
            .setContentIntent(pendingIntent);

            notificationManager.notify(0,builder.build());
        }
    }

}
