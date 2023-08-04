package com.swanky.readro.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.room.Room;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import com.swanky.readro.R;
import com.swanky.readro.databinding.ActivityAddYourSelfBinding;
import com.swanky.readro.fragments.addFragments.BookDetailsFragment;
import com.swanky.readro.fragments.addFragments.BookDetailsFragmentDirections;
import com.swanky.readro.fragments.addFragments.CategoryFragment;
import com.swanky.readro.fragments.addFragments.PhotoFragment;
import com.swanky.readro.models.roomDbModel.FinishedBooks;
import com.swanky.readro.models.roomDbModel.NowRead;
import com.swanky.readro.models.roomDbModel.RequestedBooks;
import com.swanky.readro.roomdb.BooksDao;
import com.swanky.readro.roomdb.BooksDatabase;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AddYourSelfActivity extends AppCompatActivity implements CategoryFragment.MyCategoryListener, PhotoFragment.MyPhotoListener, BookDetailsFragment.MyDetailsListener {

    private ActivityAddYourSelfBinding binding;
    private int fragmentPosition;
    private NavHostFragment navHostFragment;
    private ArrayList<NavDirections> nextDirections;
    private ArrayList<NavDirections> backDirections;
    private Bitmap imageData;
    private int selectedCategoryNumber;
    private com.swanky.readro.fragments.addFragments.PhotoFragmentDirections.ActionPhotoFragmentToBookDetailsFragment next2;
    private CompositeDisposable compositeDisposable;
    private BooksDao dao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddYourSelfBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    private void init() {
        //------>>>>>> STEP VIEW DEFINING
        Resources res = getResources();
        String[] stepsString = res.getStringArray(R.array.steps);

        ArrayList<String> stepsArray = new ArrayList<>(Arrays.asList(stepsString));
        binding.stepView
                .getState()
                .steps(stepsArray)
                .commit();


        //--------->>>>> NAVIGATION
        fragmentPosition = 0;
        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);

        nextDirections = new ArrayList<>();

        NavDirections next1 = com.swanky.readro.fragments.addFragments.CategoryFragmentDirections.actionCategoryFragmentToPhotoFragment();
        next2 = com.swanky.readro.fragments.addFragments.PhotoFragmentDirections.actionPhotoFragmentToBookDetailsFragment();


        backDirections = new ArrayList<>();
        NavDirections back1 = com.swanky.readro.fragments.addFragments.PhotoFragmentDirections.actionPhotoFragmentToCategoryFragment();
        NavDirections back2 = com.swanky.readro.fragments.addFragments.BookDetailsFragmentDirections.actionBookDetailsFragmentToPhotoFragment();

        backDirections.add(back1);
        backDirections.add(back2);

        nextDirections.add(next1);
        nextDirections.add(next2);

        //IF NEXT BUTTON IS PRESSED
        binding.nextFab.setOnClickListener(view -> {
            if (fragmentPosition < 2) {
                if (fragmentPosition == 1) {
                    binding.nextFab.setVisibility(View.INVISIBLE);
                }
                assert navHostFragment != null;
                navHostFragment.getNavController().navigate(nextDirections.get(fragmentPosition));
                fragmentPosition++;
                binding.stepView.go(fragmentPosition, true);
            }
        });

        //Convert the default image to bitmap
        Drawable drawable = ContextCompat.getDrawable(AddYourSelfActivity.this, R.drawable.readro_book);
        assert drawable != null;
        imageData = ((BitmapDrawable) drawable).getBitmap();

        //Database init
        BooksDatabase database = Room.databaseBuilder(getApplicationContext(), BooksDatabase.class, "Books").build();
        dao = database.booksDao();
    }


    @Override
    public void onBackPressed() {
        if (fragmentPosition == 0) {
            super.onBackPressed();
        } else {
            if (fragmentPosition == 2) {
                binding.nextFab.setVisibility(View.VISIBLE);
            }
            navHostFragment.getNavController().navigate(backDirections.get(fragmentPosition - 1));
            fragmentPosition--;
            binding.stepView.go(fragmentPosition, true);
        }
    }

    @Override
    public void categorySelected(int selectedCategoryNumber) {
        binding.nextFab.setVisibility(View.VISIBLE);
        next2.setSelectedCategoryNumber(selectedCategoryNumber);
        this.selectedCategoryNumber = selectedCategoryNumber;
    }

    @Override
    public void chosenPhoto(Bitmap chosenPhotoData) {
        imageData = chosenPhotoData;
    }

    @Override
    public void sendBookDetails(String bookName, String authorName, String numberOfPage, String otherDetail) {
        // Convert Bitmap to byte Array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageData.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
        byte[] byteArray = outputStream.toByteArray();

        compositeDisposable = new CompositeDisposable();

        switch (selectedCategoryNumber) {
            case 0: {
                //Save to Now Read
                NowRead nowRead = new NowRead(bookName, authorName, Integer.parseInt(numberOfPage), Integer.parseInt(otherDetail), byteArray);
                compositeDisposable.add(dao.insertNowRead(nowRead)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(AddYourSelfActivity.this::saveComplete));
                break;
            }
            case 1: {
                //Save to Requested
                RequestedBooks requestedBooks = new RequestedBooks(bookName, authorName, Integer.parseInt(numberOfPage), otherDetail, byteArray);
                compositeDisposable.add(dao.insertRequestedBooks(requestedBooks)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(AddYourSelfActivity.this::saveComplete));
                break;
            }
            case 2: {
                //Save to Finished
                FinishedBooks finishedBooks = new FinishedBooks(bookName, authorName, Integer.parseInt(numberOfPage), Long.parseLong(otherDetail), byteArray);
                compositeDisposable.add(dao.insertFinishedBook(finishedBooks)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(AddYourSelfActivity.this::saveComplete));
                break;
            }
        }
    }

    private void saveComplete() {
        NavDirections completeDirection = BookDetailsFragmentDirections.actionBookDetailsFragmentToCompleteSaveFragment();
        assert navHostFragment != null;
        navHostFragment.getNavController().navigate(completeDirection);
        binding.stepView.go(3, true);
        binding.stepView.done(true);
        fragmentPosition = 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null){
            compositeDisposable.clear();
        }
    }
}