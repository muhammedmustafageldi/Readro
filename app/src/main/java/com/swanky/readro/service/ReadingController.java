package com.swanky.readro.service;

import android.content.Context;

import androidx.room.rxjava3.EmptyResultSetException;

import com.swanky.readro.models.roomDbModel.ReadingValues;
import com.swanky.readro.roomdb.BooksDao;
import com.swanky.readro.roomdb.BooksDatabase;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ReadingController {
    private BooksDao dao;
    private CompositeDisposable compositeDisposable;

    public ReadingController(Context context) {
        BooksDatabase database = BooksDatabase.getInstance(context);
        dao = database.booksDao();
        compositeDisposable = new CompositeDisposable();
        getTheReadingValues();
    }

    public long isTimeFinished(ReadingValues readingValues) {
        long selectedTimeInMillis  = (long) readingValues.getSelectedTime() * 60 * 1000;
        long startTimeInMillis  = readingValues.getStartTime();
        long currentTimeInMillis  = System.currentTimeMillis();

        long passingTimeInMillis = currentTimeInMillis  - startTimeInMillis ;

        if (passingTimeInMillis >= selectedTimeInMillis ){
            //Time is finished.
            return 0;
        }else {
            //Time is continues
            return passingTimeInMillis;
        }
    }

    private void getTheReadingValues() {
        compositeDisposable.add(dao.getSingleReadingValues()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse,
                        throwable -> {
                            if (throwable instanceof EmptyResultSetException) {
                                //TODO Kayıt yok veri olmayınca napacaz?
                                System.out.println("Kayıt yok");
                            } else {
                                System.out.println(throwable.getMessage());
                            }
                        }));
    }

    private void handleResponse(ReadingValues readingValues) {
        isTimeFinished(readingValues);
    }


}
