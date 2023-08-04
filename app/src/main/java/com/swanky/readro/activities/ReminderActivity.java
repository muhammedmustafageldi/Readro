package com.swanky.readro.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.swanky.readro.R;
import com.swanky.readro.databinding.ActivityReminderBinding;
import com.swanky.readro.service.ReminderBroadcast;
import java.util.Calendar;

public class ReminderActivity extends AppCompatActivity {

    private ActivityReminderBinding binding;
    public static boolean isItSet;
    private int selectedHour;
    private int selectedMinute;
    private String reminderHour;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReminderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isItSet = false;
        sharedPreferences = getSharedPreferences("Readro", Context.MODE_PRIVATE);
        setFirstAppearance();

        binding.createReminderButton.setOnClickListener(view -> setLayoutAnim());
        binding.setAlarmClockButton.setOnClickListener(view1 -> showTimerDialog());

        binding.buttonSetAlarm.setOnClickListener(view2 -> {
            if (isItSet){
                setNotificationReminder();
            }else if (!isItSet && reminderHour != null){
                Toast.makeText(ReminderActivity.this, R.string.alreadyExistReminder, Toast.LENGTH_SHORT).show();
                YoYo.with(Techniques.Flash)
                        .duration(1000)
                        .playOn(binding.setTimeTxt);
            }else{
                Toast.makeText(ReminderActivity.this, R.string.selectHourString, Toast.LENGTH_SHORT).show();
                YoYo.with(Techniques.Shake)
                        .duration(1000)
                        .playOn(binding.setTimeTxt);
            }
        });

        binding.switchRandom.setOnCheckedChangeListener((compoundButton, status) -> isItSet = status);

        binding.deleteReminderButton.setOnClickListener(view3 -> {
            deleteReminder();
        });
    }


    private void setLayoutAnim(){
        Animation invisibleAnim = AnimationUtils.loadAnimation(ReminderActivity.this, R.anim.invisible_anim);

        invisibleAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.noReminderLayout.setVisibility(View.GONE);
                binding.addAlarmConstraint.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeIn)
                        .duration(1000)
                        .playOn(binding.addAlarmConstraint);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        binding.noReminderLayout.startAnimation(invisibleAnim);
    }

    @SuppressLint("SetTextI18n")
    private void setFirstAppearance(){
         reminderHour = sharedPreferences.getString("reminderHour", null);
        if (reminderHour != null){
            binding.noReminderLayout.setVisibility(View.GONE);
            binding.addAlarmConstraint.setVisibility(View.VISIBLE);
            binding.setTimeTxt.setText(reminderHour);
            binding.setTimeTxt.setTextColor(Color.parseColor("#FFB059"));
        }else {
            binding.setTimeTxt.setText("00 : 00");
        }
    }

    private void showTimerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        @SuppressLint("SetTextI18n") TimePickerDialog timePickerDialog = new TimePickerDialog(ReminderActivity.this, R.style.MyTimePickerDialogTheme, (timePicker, hour, minute1) -> {
            binding.setTimeTxt.setText(hour + " : " + minute1);
            binding.setTimeTxt.setTextColor(Color.parseColor("#FFB059"));
            selectedHour = hour;
            selectedMinute = minute1;
            isItSet = true;
        }, hourOfDay, minute, true);

        timePickerDialog.show();
    }

    private void setNotificationReminder(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
        calendar.set(Calendar.MINUTE, selectedMinute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Intent intent = new Intent(ReminderActivity.this, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ReminderActivity.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        sharedPreferences.edit().putString("reminderHour",binding.setTimeTxt.getText().toString()).apply();

        Toast.makeText(ReminderActivity.this, R.string.reminderCreated, Toast.LENGTH_SHORT).show();

        Intent intent1 = new Intent(ReminderActivity.this, MainActivity.class);
        startActivity(intent1);
        finish();
    }

    @SuppressLint("SetTextI18n")
    private void deleteReminder(){
        reminderHour =  sharedPreferences.getString("reminderHour",null);
        if (reminderHour != null){
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(ReminderActivity.this, ReminderBroadcast.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(ReminderActivity.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            alarmManager.cancel(pendingIntent);

            sharedPreferences.edit().remove("reminderHour").apply();
            reminderHour = null;
            isItSet = false;
            binding.setTimeTxt.setText("00 : 00");

            Animation invisibleAnim = AnimationUtils.loadAnimation(ReminderActivity.this, R.anim.invisible_anim);

            invisibleAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    binding.addAlarmConstraint.setVisibility(View.GONE);
                    binding.noReminderLayout.setVisibility(View.VISIBLE);
                    YoYo.with(Techniques.FadeIn)
                            .duration(1000)
                            .playOn(binding.noReminderLayout);
                    Toast.makeText(ReminderActivity.this, R.string.reminderDeleted, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });

            binding.addAlarmConstraint.startAnimation(invisibleAnim);
        }else{
            Toast.makeText(this, R.string.noReminderDeleted, Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}