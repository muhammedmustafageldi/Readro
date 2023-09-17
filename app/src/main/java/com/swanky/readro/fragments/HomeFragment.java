package com.swanky.readro.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.makeramen.roundedimageview.RoundedImageView;
import com.swanky.readro.R;
import com.swanky.readro.adapters.HomeItemsAdapter;
import com.swanky.readro.databinding.FragmentHomeBinding;
import com.swanky.readro.models.HomePageItem;
import com.swanky.readro.models.roomDbModel.NowRead;
import com.swanky.readro.models.roomDbModel.ReadingValues;
import com.swanky.readro.roomdb.BooksDao;
import com.swanky.readro.roomdb.BooksDatabase;
import com.swanky.readro.service.ProgressBarAnimation;
import com.swanky.readro.service.ReadingController;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private CompositeDisposable compositeDisposable;
    private ReadingController readingController;
    private BooksDao dao;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        messageToUser();
        setHomeRecyclerAdapter(setHomeRecyclerItems());

        BooksDatabase database = BooksDatabase.getInstance(requireContext());
        dao = database.booksDao();

        readControl();
    }

    private void messageToUser() {
        Calendar rightNow = Calendar.getInstance();
        int hour = rightNow.get(Calendar.HOUR_OF_DAY);
        if (hour >= 7 && hour <= 11) {
            binding.goodTxt.setText(getString(R.string.morningString));
        } else if (hour > 11 && hour <= 17) {
            binding.goodTxt.setText(getString(R.string.afternoonString));
        } else if (hour > 17 && hour <= 22) {
            binding.goodTxt.setText(getString(R.string.eveningString));
        } else {
            binding.goodTxt.setText(getString(R.string.nightString));
        }
    }

    private ArrayList<HomePageItem> setHomeRecyclerItems() {
        ArrayList<HomePageItem> homePageItems = new ArrayList<>();
        homePageItems.add(new HomePageItem(getString(R.string.currentlyReadString), R.drawable.ic_currentlyread));
        homePageItems.add(new HomePageItem(getString(R.string.wishListString), R.drawable.ic_wishlist));
        homePageItems.add(new HomePageItem(getString(R.string.finishedBooksString), R.drawable.ic_finishedbook));
        homePageItems.add(new HomePageItem(getString(R.string.readReminderString), R.drawable.ic_reminder));

        return homePageItems;
    }

    private void setHomeRecyclerAdapter(ArrayList<HomePageItem> homePageItems) {
        NavHostFragment navHostFragment = (NavHostFragment) requireActivity().getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
        RecyclerView homeRecycler = binding.homePageRecycler;
        homeRecycler.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        HomeItemsAdapter adapter = new HomeItemsAdapter(requireContext(), homePageItems, navHostFragment);
        recyclerItemAnimation(homeRecycler);
        homeRecycler.setAdapter(adapter);
    }

    private void recyclerItemAnimation(RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);
        recyclerView.setLayoutAnimation(controller);
        recyclerView.scheduleLayoutAnimation();
    }

    private void readControl() {
        readingController = new ReadingController(requireContext());
        readingController.getReadingData(new ReadingController.ReadingCallback() {
            @Override
            public void onReadingDataAvailable(ReadingValues readingValues, long timeRemaining) {
                // Data is available. Get the read book
                compositeDisposable = new CompositeDisposable();

                compositeDisposable.add(dao.getSingleNowRead(readingValues.getNowReadId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                nowRead -> actionOfReading(nowRead, readingValues, timeRemaining),
                                Throwable::printStackTrace
                        ));
            }

            @Override
            public void onDataNotAvailable() {
                System.out.println("No save!");
            }
        });
    }

    private void actionOfReading(NowRead nowRead, ReadingValues readingValues, long timeRemaining) {
        boolean focusMode = readingValues.isFocusMode();
        if (timeRemaining > 0) {

            // Read continues ->
            if (focusMode) {
                // Show details and cancel if the user wants to.
                showOngoingFocusDialog(nowRead, timeRemaining);
            } else {
                // Go to read room.
                goToReadRoomAndContinue(nowRead, timeRemaining, false);
            }

        } else {

            // Read finished ->
            if (focusMode) {
                // Cancel the reading. Failed. Show details.
                showFailedDialog(nowRead.title);
            } else {
                // Reading complete. Show details.
                showFinishedFreeDialogFirst(nowRead);
            }
            // Delete the reading values.

        }
        readingController.deleteReading(readingValues);
    }

    private void showOngoingFocusDialog(NowRead nowRead, long timeRemaining) {
        BottomSheetDialog bottomSheetDialog = createBottomSheetDialog(R.layout.bottom_sheet_continue_focus, false);

        Button focus_keep_going = bottomSheetDialog.findViewById(R.id.focus_keep_going);
        Button focus_give_up = bottomSheetDialog.findViewById(R.id.focus_give_up);
        RoundedImageView continue_focus_Image = bottomSheetDialog.findViewById(R.id.continue_focus_book);

        Bitmap imageBitmap = BitmapFactory.decodeByteArray(nowRead.image, 0, nowRead.image.length);

        if (focus_keep_going != null && focus_give_up != null && continue_focus_Image != null) {
            continue_focus_Image.setImageBitmap(imageBitmap);
            focus_keep_going.setOnClickListener(View -> {
                goToReadRoomAndContinue(nowRead, timeRemaining, true);
                bottomSheetDialog.dismiss();
            });
            focus_give_up.setOnClickListener(View -> bottomSheetDialog.dismiss());
        }
        bottomSheetDialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void showFinishedFreeDialogFirst(NowRead nowRead) {
        BottomSheetDialog bottomSheetDialog = createBottomSheetDialog(R.layout.bottom_sheet_complete_dialog, false);

        // Define components of dialog
        LinearLayout complete_layout_first = bottomSheetDialog.findViewById(R.id.complete_layout_first);
        LinearLayout complete_layout_second = bottomSheetDialog.findViewById(R.id.complete_layout_second);
        assert complete_layout_second != null;
        assert complete_layout_first != null;

        // Define components in first layout ->
        RoundedImageView complete_book_image = bottomSheetDialog.findViewById(R.id.complete_book_image);
        TextView desc_complete_txt = bottomSheetDialog.findViewById(R.id.desc_complete_txt);
        TextInputEditText how_many_txt = bottomSheetDialog.findViewById(R.id.how_many_pages_txt);
        Button complete_next_button = bottomSheetDialog.findViewById(R.id.complete_next_button);
        Button completely_finished_button = bottomSheetDialog.findViewById(R.id.completely_finished_button);

        // Other first layout transactions ->
        if (complete_book_image != null && desc_complete_txt != null && complete_next_button != null && how_many_txt != null && completely_finished_button != null) {

            Bitmap bitmap = BitmapFactory.decodeByteArray(nowRead.image, 0, nowRead.image.length);
            complete_book_image.setImageBitmap(bitmap);

            desc_complete_txt.setText(nowRead.title + " kitabı için okuma tamamlandı. Bu okumada kaç sayfa okudunuz?");
            int unreadPages = nowRead.numberOfPages - nowRead.pageCountRead;

            complete_next_button.setOnClickListener(view -> {
                String number_of_pages_str = Objects.requireNonNull(how_many_txt.getText()).toString();
                if (number_of_pages_str.isEmpty()) {
                    how_many_txt.setError("This value cannot be empty");
                } else {
                    int number_of_pages_read = Integer.parseInt(number_of_pages_str);
                    if (number_of_pages_read > unreadPages) {
                        how_many_txt.setError("If you have finished the book, use the button below");
                    } else {
                        NowRead newNowRead = readingController.updateNowRead(nowRead, number_of_pages_read);
                        //Hide first layout and show second layout
                        animationFromFirstToSecond(complete_layout_first, complete_layout_second, bottomSheetDialog, newNowRead);
                    }
                }
            });

            completely_finished_button.setOnClickListener(view -> {
                // Delete in nowRead and transfer to finished
                NowRead newNowRead = readingController.transferTheBook(nowRead, unreadPages);
                //Hide first layout and show second layout
                animationFromFirstToSecond(complete_layout_first, complete_layout_second, bottomSheetDialog, newNowRead);
            });
        }
        bottomSheetDialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void showFinishedFreeDialogSecond(BottomSheetDialog bottomSheetDialog, NowRead newNowRead) {
        // Define components in second layout ->
        LottieAnimationView completeAnim = bottomSheetDialog.findViewById(R.id.complete_anim);
        ProgressBar complete_dialog_progress = bottomSheetDialog.findViewById(R.id.complete_dialog_progress);
        TextView complete_dialog_progress_txt = bottomSheetDialog.findViewById(R.id.complete_dialog_progress_txt);
        TextView dialog_readro_points = bottomSheetDialog.findViewById(R.id.dialog_readro_points);
        Button complete_dialog_okay = bottomSheetDialog.findViewById(R.id.complete_dialog_okay);

        bottomSheetDialog.setCancelable(true);

        int calculatedProgress = calculatePercentage(newNowRead.numberOfPages, newNowRead.pageCountRead);

        assert complete_dialog_progress != null;
        complete_dialog_progress.setProgress(calculatedProgress);
        assert complete_dialog_progress_txt != null;
        complete_dialog_progress_txt.setText("%" + calculatedProgress);

        ProgressBarAnimation anim = new ProgressBarAnimation(complete_dialog_progress, 0, calculatedProgress);
        anim.setDuration(1500);
        complete_dialog_progress.startAnimation(anim);

        assert complete_dialog_okay != null;
        complete_dialog_okay.setOnClickListener(view -> bottomSheetDialog.dismiss());

        bottomSheetDialog.setOnDismissListener(dialogInterface -> {
            assert completeAnim != null;
            completeAnim.cancelAnimation();
        });
    }

    @SuppressLint("SetTextI18n")
    private void showFailedDialog(String bookName) {
        BottomSheetDialog bottomSheetDialog = createBottomSheetDialog(R.layout.bottom_sheet_failed, true);

        // Define components ->
        LottieAnimationView failedAnim = bottomSheetDialog.findViewById(R.id.failed_anim);
        TextView failedTxt = bottomSheetDialog.findViewById(R.id.failed_dialog_txt);
        Button okayButton = bottomSheetDialog.findViewById(R.id.failed_okay_button);

        assert failedTxt != null;
        failedTxt.setText("Focus modda başlatılan süre sona erdi. \nMaalesef " + bookName + " için okuma tamamlanamadı. \uD83D\uDE41");
        assert okayButton != null;
        okayButton.setOnClickListener(view -> bottomSheetDialog.dismiss());

        bottomSheetDialog.setOnDismissListener(dialogInterface -> {
            assert failedAnim != null;
            failedAnim.cancelAnimation();
        });
        bottomSheetDialog.show();
    }

    private void goToReadRoomAndContinue(NowRead nowRead, long timeRemaining, boolean focusMode) {
        NavHostFragment navHostFragment = (NavHostFragment) requireActivity().getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
        NavDirections homeToReadRoom = HomeFragmentDirections.actionHomeFragmentToReadRoomFragment(nowRead)
                .setTimeRemaining(timeRemaining)
                .setFocusMode(focusMode);
        if (navHostFragment != null) {
            navHostFragment.getNavController().navigate(homeToReadRoom);
        }
    }

    private BottomSheetDialog createBottomSheetDialog(int layoutId, boolean cancelable) {
        View bottomSheetView = getLayoutInflater().inflate(layoutId, null);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.setCancelable(cancelable);

        return bottomSheetDialog;
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

    private void animationFromFirstToSecond(LinearLayout firstLayout, LinearLayout secondLayout, BottomSheetDialog bottomSheetDialog, NowRead newNowRead) {
        Animation invisibleAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.invisible_anim);
        Animation visibleAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.visible_anim);

        invisibleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                firstLayout.setVisibility(View.INVISIBLE);
                secondLayout.setVisibility(View.VISIBLE);
                secondLayout.startAnimation(visibleAnimation);
                showFinishedFreeDialogSecond(bottomSheetDialog, newNowRead);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        firstLayout.startAnimation(invisibleAnimation);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
        if (compositeDisposable != null) {
            compositeDisposable.clear();
            readingController.clearDisposable();
        }
    }


}