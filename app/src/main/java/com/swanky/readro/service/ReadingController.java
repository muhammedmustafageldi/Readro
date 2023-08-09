package com.swanky.readro.service;

import android.content.Context;
import android.util.Log;

import androidx.room.rxjava3.EmptyResultSetException;

import com.swanky.readro.models.roomDbModel.ReadingValues;
import com.swanky.readro.roomdb.BooksDao;
import com.swanky.readro.roomdb.BooksDatabase;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ReadingController {
    private final BooksDao dao;
    private final CompositeDisposable compositeDisposable;

    public interface ReadingCallback {
        void onReadingDataAvailable(ReadingValues readingValues, long timeRemaining);

        void onDataNotAvailable();
    }

    public ReadingController(Context context) {
        BooksDatabase database = BooksDatabase.getInstance(context);
        dao = database.booksDao();
        compositeDisposable = new CompositeDisposable();
    }

    public void getReadingData(ReadingCallback callback) {
        compositeDisposable.add(dao.getSingleReadingValues()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(readingValues -> {
                            long timeRemaining = isTimeFinished(readingValues);
                            callback.onReadingDataAvailable(readingValues, timeRemaining);
                        },
                        throwable -> {
                            if (throwable instanceof EmptyResultSetException) {
                                callback.onDataNotAvailable();
                            } else {
                                // Handle other errors
                                Log.e("Get Reading Error: ", throwable.getMessage());
                            }
                        }));
    }

    private long isTimeFinished(ReadingValues readingValues) {
        long selectedTimeInMillis = (long) readingValues.getSelectedTime() * 60 * 1000;
        long startTimeInMillis = readingValues.getStartTime();
        long currentTimeInMillis = System.currentTimeMillis();

        long passingTimeInMillis = currentTimeInMillis - startTimeInMillis;

        if (passingTimeInMillis >= selectedTimeInMillis) {
            // Time is finished.
            return 0;
        } else {
            // Time is continues
            return selectedTimeInMillis - passingTimeInMillis;
        }
    }

    public void clearDisposable() {
        compositeDisposable.clear();
    }
}
