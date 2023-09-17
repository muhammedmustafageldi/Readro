package com.swanky.readro.service;

import android.content.Context;
import android.util.Log;

import androidx.room.rxjava3.EmptyResultSetException;

import com.swanky.readro.models.roomDbModel.FinishedBooks;
import com.swanky.readro.models.roomDbModel.NowRead;
import com.swanky.readro.models.roomDbModel.ReadingValues;
import com.swanky.readro.roomdb.BooksDao;
import com.swanky.readro.roomdb.BooksDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
                            long newRemainingTime = isTimeFinished(readingValues);
                            callback.onReadingDataAvailable(readingValues, newRemainingTime);
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
        long timeRemaining = readingValues.getTimeRemaining();
        long startTimeInMillis = readingValues.getStartTime();
        long currentTimeInMillis = System.currentTimeMillis();

        Date date = new Date(startTimeInMillis);

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String formattedTime = dateFormat.format(date);

        System.out.println("Start time: " + formattedTime);

        long passingTimeInMillis = currentTimeInMillis - startTimeInMillis;

        if (passingTimeInMillis >= timeRemaining) {
            // Time is finished.
            return 0;
        } else {
            // Time is continues
            return timeRemaining - passingTimeInMillis;
        }
    }


    public void deleteReading(ReadingValues readingValues) {
        compositeDisposable.add(dao.deleteReadingValues(readingValues)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());
    }

    public NowRead updateNowRead(NowRead nowRead, int number_of_pages_read) {
        nowRead.pageCountRead += number_of_pages_read;
        compositeDisposable.add(dao.updateNowRead(nowRead)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());
        return nowRead;
    }

    public NowRead transferTheBook(NowRead nowRead, int number_of_pages_read) {
        nowRead.pageCountRead += number_of_pages_read;
        FinishedBooks finishedBooks = new FinishedBooks(nowRead.title, nowRead.author, nowRead.numberOfPages, (System.currentTimeMillis() / 1000L), nowRead.image);
        compositeDisposable.add(dao.insertFinishedBook(finishedBooks)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());
        compositeDisposable.add(dao.deleteNowRead(nowRead)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());
        return nowRead;
    }

    public void clearDisposable() {
        compositeDisposable.clear();
    }
}
