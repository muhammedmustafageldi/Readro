package com.swanky.readro.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.swanky.readro.R;
import com.swanky.readro.databinding.FragmentReadRoomBinding;
import com.swanky.readro.models.roomDbModel.NowRead;
import com.swanky.readro.models.roomDbModel.ReadingValues;
import com.swanky.readro.roomdb.BooksDao;
import com.swanky.readro.roomdb.BooksDatabase;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ReadRoomFragment extends Fragment {

    private FragmentReadRoomBinding binding;
    private BooksDao dao;
    private CompositeDisposable compositeDisposable;
    private boolean focusMode = true;
    private int selectedTime;
    private NowRead nowRead;

    public ReadRoomFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentReadRoomBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init();
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        //Init the database objects.
        BooksDatabase database = BooksDatabase.getInstance(requireContext());
        dao = database.booksDao();
        compositeDisposable = new CompositeDisposable();


        if (getArguments() != null) {
            ReadRoomFragmentArgs bundle = ReadRoomFragmentArgs.fromBundle(getArguments());
            nowRead = bundle.getNowReadObject();

            //Show the book details
            // Convert byteArray to bitmap
            byte[] imageByteArray = nowRead.image;
            int numberOfPages = nowRead.numberOfPages;
            int pageCountRead = nowRead.pageCountRead;
            int calculatedProgress = calculatePercentage(numberOfPages, pageCountRead);

            Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
            binding.readRoomImage.setImageBitmap(imageBitmap);

            binding.readRoomProgress.setProgress(calculatedProgress);
            binding.readRoomProgressTxt.setText("%" + calculatedProgress);
        }

        binding.readRoomButton.setOnClickListener(view2 -> showReadModeDialog());

        getBackground();
    }


    private void getBackground() {
        if (getActivity() != null) {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Background", Context.MODE_PRIVATE);
            String backgroundName = sharedPreferences.getString("backgroundImage", null);
            // If the user has already set a background
            if (backgroundName != null) {
                @SuppressLint("DiscouragedApi") int resourceId = getResources().getIdentifier(backgroundName, "drawable", getActivity().getPackageName());
                binding.readRoomConstraint.setBackgroundResource(resourceId);
            }
        }
    }

    private int calculatePercentage(int firstNumber, int secondNumber) {
        if (secondNumber == 0) {
            return 0;
        } else {
            double ratio = (double) secondNumber / firstNumber;
            double percentage = ratio * 100;
            return (int) percentage;
        }
    }

    private void showReadModeDialog() {
        Dialog dialog = new Dialog(requireContext(), R.style.DialogTheme);
        @SuppressLint("InflateParams") View readModeDialog = getLayoutInflater().inflate(R.layout.read_mode_dialog, null);
        Button button15min = readModeDialog.findViewById(R.id.button15min);
        Button button30min = readModeDialog.findViewById(R.id.button30min);
        TextView descriptionTxt = readModeDialog.findViewById(R.id.modeDescriptionTxt);
        RadioGroup modeRadioGroup = readModeDialog.findViewById(R.id.modeRadioGroup);

        dialog.setContentView(readModeDialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        modeRadioGroup.setOnCheckedChangeListener((radioGroup, id) -> {
            if (id == R.id.focusModeRadio) {
                focusMode = true;
                descriptionTxt.setText(getString(R.string.readModeFocusDesc));
            } else if (id == R.id.freeModeRadio) {
                focusMode = false;
                descriptionTxt.setText(getString(R.string.readModeFreeDesc));
            }
        });

        button15min.setOnClickListener(view -> {
            selectedTime = 15;
            switchToReadingMode(900000);
            dialog.cancel();
        });

        button30min.setOnClickListener(view -> {
            selectedTime = 30;
            switchToReadingMode(1800000);
            dialog.cancel();
        });
    }

    private void saveCurrentReading() {
        ReadingValues readingValues = new ReadingValues(nowRead.id, selectedTime, System.currentTimeMillis(), focusMode);
        compositeDisposable.add(dao.insertReadingValues(readingValues)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());
    }

    private void switchToReadingMode(long milliSeconds) {
        //Save the currently reading book in the database.
        saveCurrentReading();

        //And start time timer.
        new CountDownTimer(milliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateTimerText(millisUntilFinished);
            }

            @Override
            public void onFinish() {

            }
        }.start();
    }

    private void updateTimerText(long millisUntilFinished) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;

        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        binding.readRoomTimerTxt.setText(timeFormatted);
    }


    @Override
    public void onDestroy() {
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
        super.onDestroy();
    }
}