package com.swanky.readro.roomdb;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.swanky.readro.R;
import com.swanky.readro.models.roomDbModel.BackgroundItem;
import com.swanky.readro.models.roomDbModel.FinishedBooks;
import com.swanky.readro.models.roomDbModel.NowRead;
import com.swanky.readro.models.roomDbModel.ReadingValues;
import com.swanky.readro.models.roomDbModel.RequestedBooks;
import java.util.concurrent.Executors;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Database(entities = {NowRead.class, RequestedBooks.class, FinishedBooks.class, ReadingValues.class, BackgroundItem.class}, version = 1)
public abstract class BooksDatabase extends RoomDatabase {
    public abstract BooksDao booksDao();

    private static volatile BooksDatabase INSTANCE;

    public static BooksDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (BooksDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    BooksDatabase.class, "Books")
                            .addCallback(roomCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }


    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Executors.newSingleThreadExecutor().execute(() -> {
                // Save default value of background items.
                BooksDao dao = INSTANCE.booksDao();
                CompositeDisposable compositeDisposable = new CompositeDisposable();
                compositeDisposable.add(dao.insertBackgroundItem(new BackgroundItem(R.drawable.bg1, 100, true))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe());

                compositeDisposable.add(dao.insertBackgroundItem(new BackgroundItem(R.drawable.bg2, 150, false))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe());

                compositeDisposable.add(dao.insertBackgroundItem(new BackgroundItem(R.drawable.bg3, 200, false))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe());

                compositeDisposable.add(dao.insertBackgroundItem(new BackgroundItem(R.drawable.bg4, 250, false))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe());

                compositeDisposable.add(dao.insertBackgroundItem(new BackgroundItem(R.drawable.bg6, 300, false))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe());

                compositeDisposable.add(dao.insertBackgroundItem(new BackgroundItem(R.drawable.bg7, 350, false))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe());
            });
        }

    };


}
