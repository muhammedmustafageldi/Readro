package com.swanky.readro.fragments.addFragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.swanky.readro.R;
import com.swanky.readro.databinding.FragmentBookDetailsBinding;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class BookDetailsFragment extends Fragment {

    private FragmentBookDetailsBinding binding;
    private int selectedNumber;
    private MyDetailsListener listener;
    private long selectedUnixTime;

    public BookDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBookDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //visibility settings by selection
        ArrayList<TextInputLayout> layouts = new ArrayList<>();

        layouts.add(binding.finishedPageSelfLayout);
        layouts.add(binding.noteLayoutSelf);
        layouts.add(binding.dateOfFinishLayoutSelf);

        if (getArguments() != null) {
            selectedNumber = BookDetailsFragmentArgs.fromBundle(getArguments()).getSelectedCategoryNumber();
            layouts.get(selectedNumber).setVisibility(View.VISIBLE);
            layouts.remove(selectedNumber);
            layouts.get(0).setVisibility(View.INVISIBLE);
            layouts.get(1).setVisibility(View.INVISIBLE);
        }

        binding.fabCompleteSave.setOnClickListener(view1 -> {
            TextInputEditText visibleTxt;
            switch (selectedNumber) {
                case 0:
                    visibleTxt = binding.readPageCountSelf;
                    break;

                case 1:
                    visibleTxt = binding.noteEditTxtSelf;
                    break;

                case 2:
                    visibleTxt = binding.dateOfFinishTxtSelf;
                    break;

                default:
                    visibleTxt = null;
            }

            if (validator(binding.addSelfBookTitle, binding.addSelfAuthor, binding.numberOfPagesTxt, visibleTxt)) {
                String bookName = Objects.requireNonNull(binding.addSelfBookTitle.getText()).toString();
                String authorName = Objects.requireNonNull(binding.addSelfAuthor.getText()).toString();
                String numberOfPages = Objects.requireNonNull(binding.numberOfPagesTxt.getText()).toString();
                String otherDetail;

                if (selectedNumber == 2){
                    otherDetail = String.valueOf(selectedUnixTime);
                }else{
                    assert visibleTxt != null;
                    otherDetail = Objects.requireNonNull(visibleTxt.getText()).toString();
                }

                saveComplete(bookName, authorName,numberOfPages, otherDetail);
            }
        });

        binding.dateOfFinishTxtSelf.setOnClickListener(view1 -> showDatePickerDialog());
    }

    private boolean validator(TextInputEditText bookNameTxt, TextInputEditText authorNameTxt, TextInputEditText readPagesTxt, TextInputEditText visibleTxt) {

        if (Objects.requireNonNull(bookNameTxt.getText()).toString().isEmpty()) {
            bookNameTxt.setError("The name of the book cannot be left blank.");
            return false;
        } else if (Objects.requireNonNull(authorNameTxt.getText()).toString().isEmpty()) {
            authorNameTxt.setError("The name of the author cannot be left blank.");
            return false;
        } else if (Objects.requireNonNull(readPagesTxt.getText()).toString().isEmpty()) {
            readPagesTxt.setError("Number of pages read cannot be left blank.");
            return false;
        } else if (Objects.requireNonNull(visibleTxt.getText()).toString().isEmpty()) {

            if (visibleTxt == binding.readPageCountSelf) {
                visibleTxt.setError("This field cannot be left blank");
                return false;
            } else {
                return true;
            }
        }else {
            return true;
        }

    }

    private void saveComplete(String bookName, String authorName, String numberOfPage, String otherDetail) {
        listener.sendBookDetails(bookName, authorName, numberOfPage, otherDetail);
    }

    public interface MyDetailsListener {
        void sendBookDetails(String bookName, String authorName,String numberOfPage, String otherDetail);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MyDetailsListener) {
            listener = (MyDetailsListener) context;
        } else {
            throw new RuntimeException(context + " must implement MyFragmentListener");
        }
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

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), R.style.MyDatePickerDialogTheme, (view, year1, monthOfYear, dayOfMonth) -> {
            String selectedDateString = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
            binding.dateOfFinishTxtSelf.setText(selectedDateString);

            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year1,monthOfYear,dayOfMonth);
            selectedUnixTime = selectedDate.getTimeInMillis() /1000L;

        }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        datePickerDialog.show();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}