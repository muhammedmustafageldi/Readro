package com.swanky.readro.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.swanky.readro.models.roomDbModel.BackgroundItem;
import com.swanky.readro.models.roomDbModel.FinishedBooks;
import com.swanky.readro.models.roomDbModel.NowRead;
import com.swanky.readro.models.roomDbModel.ReadingValues;
import com.swanky.readro.models.roomDbModel.RequestedBooks;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface BooksDao {

    //QUERY'S
    @Query("SELECT * FROM NowRead")
    Flowable<List<NowRead>> getAllNowRead();

    @Query("SELECT * FROM RequestedBooks")
    Flowable<List<RequestedBooks>> getAllRequested();

    @Query("SELECT * FROM FinishedBooks")
    Flowable<List<FinishedBooks>> getAllFinished();


    //SINGLE QUERY'S
    @Query("SELECT * FROM NowRead WHERE id = :id")
    io.reactivex.rxjava3.core.Single<NowRead> getSingleNowRead(int id);

    @Query("SELECT * FROM FinishedBooks WHERE id = :id")
    io.reactivex.rxjava3.core.Single<FinishedBooks> getSingleFinishedBooks(int id);

    @Query("SELECT * FROM RequestedBooks WHERE id = :id")
    io.reactivex.rxjava3.core.Single<RequestedBooks> getSingleRequestedBooks(int id);

    //ASCENDING ORDER QUERY
    @Query("SELECT * FROM FinishedBooks ORDER BY endDate ASC")
    Flowable<List<FinishedBooks>> getAllFinishedAscending();

    //LAST 30 DAYS Finished
    @Query("SELECT * FROM FinishedBooks WHERE endDate >= strftime('%s', 'now', '-30 days') * 1000")
    Flowable<List<FinishedBooks>> getLast30Days();


    //INSERT TRANSACTIONS
    @Insert
    Completable insertNowRead(NowRead nowRead);

    @Insert
    Completable insertFinishedBook(FinishedBooks finishedBooks);

    @Insert
    Completable insertRequestedBooks(RequestedBooks requestedBooks);


    //DELETE TRANSACTIONS
    @Delete
    Completable deleteNowRead(NowRead nowRead);

    @Delete
    Completable deleteRequested(RequestedBooks requestedBooks);

    @Delete
    Completable deleteFinished(FinishedBooks finishedBooks);


    @Query("DELETE FROM NowRead WHERE id = :id")
    Completable deleteByIdNowRead(int id);

    @Query("DELETE FROM RequestedBooks WHERE id = :id")
    Completable deleteByIdRequested(int id);

    @Query("DELETE FROM FinishedBooks WHERE id = :id")
    Completable deleteByIdFinished(int id);


    //UPDATE TRANSACTION
    @Update
    Completable updateNowRead(NowRead nowRead);

    @Update
    Completable updateRequested(RequestedBooks requestedBooks);

    @Update
    Completable updateFinished(FinishedBooks finishedBooks);

    //Reading Transactions
    @Query("SELECT * FROM ReadingValues WHERE id = 1")
    io.reactivex.rxjava3.core.Single<ReadingValues> getSingleReadingValues();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertReadingValues(ReadingValues readingValues);

    @Delete
    Completable deleteReadingValues(ReadingValues readingValues);

    //Background Items Transactions
    @Query("SELECT * FROM BackgroundItem")
    Flowable<List<BackgroundItem>> getBackgroundItems();

    @Insert
    Completable insertBackgroundItem(BackgroundItem backgroundItem);

    @Update
    Completable updateBackgroundItem(BackgroundItem backgroundItem);

}
