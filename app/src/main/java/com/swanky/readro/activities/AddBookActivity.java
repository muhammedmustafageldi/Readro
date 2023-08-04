package com.swanky.readro.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.makeramen.roundedimageview.RoundedDrawable;
import com.squareup.picasso.Picasso;
import com.swanky.readro.R;
import com.swanky.readro.databinding.ActivityAddBookBinding;
import com.swanky.readro.models.Book;
import com.swanky.readro.models.roomDbModel.FinishedBooks;
import com.swanky.readro.models.roomDbModel.NowRead;
import com.swanky.readro.models.roomDbModel.RequestedBooks;
import com.swanky.readro.roomdb.BooksDao;
import com.swanky.readro.roomdb.BooksDatabase;
import com.swanky.readro.service.VisibilityAnimator;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AddBookActivity extends AppCompatActivity {

    private ActivityAddBookBinding binding;
    private Book selectedBook;
    private int oldPosition;
    private ActivityResultLauncher<String> galleryPermissionLauncher;
    private ActivityResultLauncher<Intent> galleryIntentLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<Void> cameraLauncher;
    private Bitmap imageBitmap;
    private long selectedUnixTime;
    private CompositeDisposable compositeDisposable;
    private String categoryName;
    private MediaPlayer player;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        selectedBook = (Book) getIntent().getSerializableExtra("Book");

        oldPosition = 0;

        init();
        setLayout();
        registerLauncher();
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        binding.addTitle.setText(selectedBook.getTitle());
        binding.addAuthor.setText(selectedBook.getAuthor());
        binding.addLanguage.setText(getString(R.string.languageString) + selectedBook.getLanguage());
        binding.addPageCount.setText(getString(R.string.pageCountString) + selectedBook.getPageCount());
        binding.addPublishDate.setText(getText(R.string.publishedDate) + selectedBook.getPublishedDate());

        binding.addBookNameEdit.setText(selectedBook.getTitle());
        binding.addPageCountEdit.setText(String.valueOf(selectedBook.getPageCount()));

        Picasso.get().load(selectedBook.getImageLink())
                .resize(100, 160)
                .placeholder(R.drawable.loading)
                .into(binding.addImage);

        binding.addBookNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.addTitle.setText(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        binding.addPageCountEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.addPageCount.setText(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        binding.addImage.setOnClickListener(this::goToCameraOrPickImage);

        binding.addBookSaveButton.setOnClickListener(view -> save());

        //Convert the default image to bitmap
        Drawable drawable = binding.addImage.getDrawable();
        RoundedDrawable roundedDrawable = (RoundedDrawable) drawable;
        imageBitmap = roundedDrawable.getSourceBitmap();
        imageBitmap = makeSmallerImage(imageBitmap, 300);
    }

    private void save() {
        TextInputEditText bookNameTxt = binding.addBookNameEdit;
        TextInputEditText numberOfPagesTxt = binding.addPageCountEdit;
        TextInputEditText visibleTxt;

        String bookName = Objects.requireNonNull(bookNameTxt.getText()).toString();

        // Convert Bitmap to byte Array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
        byte[] byteArray = outputStream.toByteArray();

        //Database init
        BooksDatabase database = Room.databaseBuilder(getApplicationContext(), BooksDatabase.class, "Books").build();
        BooksDao dao = database.booksDao();
        compositeDisposable = new CompositeDisposable();

        switch (oldPosition) {
            case 0:
                //Save to Now Read
                categoryName = getString(R.string.currentlyReadString);
                visibleTxt = binding.numberOfPagesFinished;

                if (validator(bookNameTxt, numberOfPagesTxt, visibleTxt)) {
                    int numberOfPages = Integer.parseInt(Objects.requireNonNull(numberOfPagesTxt.getText()).toString());
                    NowRead nowRead = new NowRead(bookName, selectedBook.getAuthor(), numberOfPages, Integer.parseInt(Objects.requireNonNull(visibleTxt.getText()).toString()), byteArray);
                    compositeDisposable.add(dao.insertNowRead(nowRead)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(AddBookActivity.this::saveComplete));
                }
                break;
            case 1:
                //Save to Requested
                categoryName = getString(R.string.wishListString);
                visibleTxt = binding.noteEditTxt;

                if (validator(bookNameTxt, numberOfPagesTxt, visibleTxt)) {
                    int numberOfPages = Integer.parseInt(Objects.requireNonNull(numberOfPagesTxt.getText()).toString());
                    RequestedBooks requestedBooks = new RequestedBooks(bookName, selectedBook.getAuthor(), numberOfPages, Objects.requireNonNull(visibleTxt.getText()).toString(), byteArray);
                    compositeDisposable.add(dao.insertRequestedBooks(requestedBooks)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(AddBookActivity.this::saveComplete));
                }
                break;
            case 2:
                //Save to Finished
                categoryName = getString(R.string.finishedBooksString);
                visibleTxt = binding.dateOfFinishTxt;

                if (validator(bookNameTxt, numberOfPagesTxt, visibleTxt)) {
                    int numberOfPages = Integer.parseInt(Objects.requireNonNull(numberOfPagesTxt.getText()).toString());
                    FinishedBooks finishedBooks = new FinishedBooks(bookName, selectedBook.getAuthor(), numberOfPages, selectedUnixTime, byteArray);
                    compositeDisposable.add(dao.insertFinishedBook(finishedBooks)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(AddBookActivity.this::saveComplete));
                }
                break;
        }
    }

    private boolean validator(TextInputEditText bookNameTxt, TextInputEditText numberOfPagesTxt, TextInputEditText visibleTxt) {
        if (Objects.requireNonNull(bookNameTxt.getText()).toString().isEmpty()) {
            bookNameTxt.setError("The name of the book cannot be left blank.");
            return false;
        } else if (Objects.requireNonNull(numberOfPagesTxt.getText()).toString().isEmpty()) {
            numberOfPagesTxt.setError("Number of pages read cannot be left blank.");
            return false;
        } else if (Objects.requireNonNull(visibleTxt.getText()).toString().isEmpty()) {
            if (visibleTxt == binding.numberOfPagesFinished) {
                visibleTxt.setError("This field cannot be left blank");
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @SuppressLint("SetTextI18n")
    private void goToCameraOrPickImage(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddBookActivity.this, R.style.DialogTheme);

        @SuppressLint("InflateParams") View dialogView = getLayoutInflater().inflate(R.layout.layout_warning_dailog, null);
        builder.setView(dialogView);

        ImageView alertIcon = dialogView.findViewById(R.id.alertIcon);
        Button buttonCamera = dialogView.findViewById(R.id.buttonNegative);
        Button buttonGallery = dialogView.findViewById(R.id.buttonPositive);
        TextView alertTitle = dialogView.findViewById(R.id.alertTitleTxt);
        TextView alertMessage = dialogView.findViewById(R.id.alertMessageTxt);

        alertIcon.setImageResource(R.drawable.ico_pick_image);
        alertTitle.setText("Select photo");
        alertMessage.setText("You can choose a photo for your book from the gallery or take it from the camera.");
        buttonCamera.setText("Camera");
        buttonGallery.setText("Gallery");

        final AlertDialog alertDialog = builder.create();

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        buttonCamera.setOnClickListener(view1 -> {
            goToCamera(view1);
            alertDialog.cancel();
        });

        buttonGallery.setOnClickListener(view1 -> {
            goToGallery(view1);
            alertDialog.cancel();
        });

        alertDialog.show();
    }

    private void goToGallery(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //For android version 33+
            if (ContextCompat.checkSelfPermission(AddBookActivity.this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(AddBookActivity.this, Manifest.permission.READ_MEDIA_IMAGES)) {
                    Snackbar.make(view, getString(R.string.permission_explanation), Snackbar.LENGTH_INDEFINITE).setAction(R.string.permission_button, view1 -> {
                        //Request Permission
                        galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                    }).show();
                } else {
                    //Request permission
                    galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                }
            } else {
                //Go to gallery
                Intent goToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryIntentLauncher.launch(goToGallery);
            }
        } else {
            //For android version until 33
            if (ContextCompat.checkSelfPermission(AddBookActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(AddBookActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Snackbar.make(view, getString(R.string.permission_explanation), Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.permission_button), view1 -> {
                        //Request Permission
                        galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }).show();
                } else {
                    //Request permission
                    galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            } else {
                //Permission is granted goTo Gallery
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryIntentLauncher.launch(intentToGallery);
            }
        }
    }

    private void goToCamera(View view) {
        if (ContextCompat.checkSelfPermission(AddBookActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(AddBookActivity.this, Manifest.permission.CAMERA)) {
                Snackbar.make(view, R.string.permission_explanation_camera, Snackbar.LENGTH_INDEFINITE).setAction(R.string.permission_button, view1 -> {
                    //Request permission
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                }).show();
            } else {
                //Request Permission
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        } else {
            //Permission is granted. You can go to the camera
            cameraLauncher.launch(null);
        }
    }

    private void registerLauncher() {
        galleryPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result) {
                //Permission Granted
                Intent goToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryIntentLauncher.launch(goToGallery);
            } else {
                Toast.makeText(AddBookActivity.this, R.string.permission_explanation, Toast.LENGTH_SHORT).show();
            }
        });

        cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result) {
                cameraLauncher.launch(null);
            } else {
                Toast.makeText(AddBookActivity.this, R.string.permission_explanation_camera, Toast.LENGTH_SHORT).show();
            }
        });

        galleryIntentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent intentFromResult = result.getData();
                if (intentFromResult != null) {
                    Uri imageData = intentFromResult.getData();
                    //Convert the bitmap
                    try {

                        if (Build.VERSION.SDK_INT >= 28) {
                            ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageData);
                            imageBitmap = makeSmallerImage(ImageDecoder.decodeBitmap(source), 300);
                            binding.addImage.setImageBitmap(imageBitmap);
                        } else {
                            imageBitmap = makeSmallerImage(MediaStore.Images.Media.getBitmap(getContentResolver(), imageData), 300);
                            binding.addImage.setImageBitmap(imageBitmap);
                        }

                    } catch (Exception e) {
                        Log.e("Image Log: ", e.getMessage());
                        Toast.makeText(AddBookActivity.this, R.string.galleryIntentErrorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), result -> {
            if (result != null) {
                binding.addImage.setImageBitmap(result);
                imageBitmap = makeSmallerImage(result, 300);
            } else {
                Toast.makeText(AddBookActivity.this, getString(R.string.error_camera), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLayout() {
        Animation invisibleAnimation = AnimationUtils.loadAnimation(this, R.anim.invisible_anim);
        Animation visibleAnimation = AnimationUtils.loadAnimation(this, R.anim.visible_anim);

        VisibilityAnimator animator = new VisibilityAnimator(invisibleAnimation, visibleAnimation, binding.numberFinishedLayout, binding.noteLayout, binding.dateOfFinishLayout);

        binding.radioGroup.setOnCheckedChangeListener((radioGroup, selectedId) -> {
            if (selectedId == R.id.radioNowRead) {
                //Selected now read
                animator.startAnimation(oldPosition, 0);
                oldPosition = 0;
            } else if (selectedId == R.id.radioWant) {
                //Selected Want
                animator.startAnimation(oldPosition, 1);
                oldPosition = 1;
            } else {
                //Selected Finished
                animator.startAnimation(oldPosition, 2);
                oldPosition = 2;

                binding.dateOfFinishTxt.setOnClickListener(view -> showDatePickerDialog());
            }
        });

    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        Calendar minDate = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        minDate.set(1999, 0, 1);
        maxDate.set(year, month, day);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.MyDatePickerDialogTheme, (view, year1, monthOfYear, dayOfMonth) -> {
            String selectedDateString = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
            binding.dateOfFinishTxt.setText(selectedDateString);

            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year1, monthOfYear, dayOfMonth);
            selectedUnixTime = selectedDate.getTimeInMillis() / 1000L;

        }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        datePickerDialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void saveComplete() {
        player = MediaPlayer.create(AddBookActivity.this, R.raw.successful);
        player.start();
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.save_complete_dialog, null);
        Button backToMain = view.findViewById(R.id.backToMainDialog);
        Button otherSearchButton = view.findViewById(R.id.otherSearchButton);
        TextView descriptionTxt = view.findViewById(R.id.dialogDescriptionTxt);

        Dialog dialog = new Dialog(AddBookActivity.this);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        descriptionTxt.setText("Book selected in the '" + categoryName + "' category added.");


        backToMain.setOnClickListener(view1 -> {
            //Back to main menu
            dialog.cancel();
            player.stop();
            Intent intent = new Intent(AddBookActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        otherSearchButton.setOnClickListener(view2 -> {
            //Go to other search
            dialog.cancel();
            player.stop();
            finish();
        });

        dialog.show();
    }

    public Bitmap makeSmallerImage(Bitmap image, int maximumSize) {

        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) {
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
        binding = null;
    }
}