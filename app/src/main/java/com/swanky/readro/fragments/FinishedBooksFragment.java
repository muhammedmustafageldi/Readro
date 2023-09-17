package com.swanky.readro.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.swanky.readro.R;
import com.swanky.readro.activities.MainActivity;
import com.swanky.readro.adapters.FinishedBooksAdapter;
import com.swanky.readro.databinding.FragmentFinishedBooksBinding;
import com.swanky.readro.models.roomDbModel.FinishedBooks;
import com.swanky.readro.roomdb.BooksDao;
import com.swanky.readro.roomdb.BooksDatabase;
import com.swanky.readro.service.CustomItemAnimator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FinishedBooksFragment extends Fragment {

    private FragmentFinishedBooksBinding binding;
    private FinishedBooksAdapter finishedBooksAdapter;
    private BottomNavigationView bottomNavigationView;
    private CompositeDisposable compositeDisposable;
    private BooksDao dao;
    private List<FinishedBooks> finishedBooks;
    private List<FinishedBooks> filteredList;
    private List<FinishedBooks> alphabeticList;
    private List<FinishedBooks> lastWeekList;
    private List<FinishedBooks> lastMonthList;
    private boolean lastWeekFilter = false;
    private boolean lastMonthFilter = false;


    public FinishedBooksFragment() {
        // Required empty public constructor

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentFinishedBooksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomNavigationView.setVisibility(View.GONE);

        BooksDatabase database = Room.databaseBuilder(requireContext().getApplicationContext(), BooksDatabase.class, "Books").build();
        dao = database.booksDao();

        getData();

        binding.searchViewFinished.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (lastWeekFilter) {
                    searchBooks(lastWeekList, query);
                } else if (lastMonthFilter) {
                    searchBooks(lastMonthList, query);
                } else {
                    if (finishedBooks != null) {
                        searchBooks(finishedBooks, query);
                    } else {
                        Snackbar.make(binding.searchViewFinished, "Aranacak veri bulunmuyor.", Snackbar.LENGTH_SHORT).show();
                        binding.searchViewFinished.clearFocus();
                    }
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (lastWeekFilter) {
                    searchBooks(lastWeekList, query);
                } else if (lastMonthFilter) {
                    searchBooks(lastMonthList, query);
                } else {
                    if (finishedBooks != null) {
                        searchBooks(finishedBooks, query);
                    } else {
                        Snackbar.make(binding.searchViewFinished, "Aranacak veri bulunmuyor.", Snackbar.LENGTH_SHORT).show();
                        binding.searchViewFinished.clearFocus();
                    }
                }
                return true;
            }
        });

        binding.filterTxt.setOnClickListener(view2 -> {
            if (finishedBooks == null) {
                Snackbar.make(view2, "Filtrelenecek veri bulunmuyor.", Snackbar.LENGTH_SHORT).show();
            } else {
                PopupMenu popupMenu = new PopupMenu(requireContext(), view2);
                popupMenu.setGravity(Gravity.END);
                popupMenu.getMenuInflater().inflate(R.menu.my_filter_menu, popupMenu.getMenu());
                popupMenu.show();


                popupMenu.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.menu_alphabetical) {
                        alphabeticalSort(finishedBooks);
                        return true;
                    } else if (itemId == R.id.menu_last_week) {
                        lastWeekFilter(finishedBooks);
                        lastWeekFilter = true;
                        lastMonthFilter = false;
                        return true;
                    } else if (itemId == R.id.menu_last_month) {
                        lastMonthFilter(finishedBooks);
                        lastMonthFilter = true;
                        lastWeekFilter = false;
                        return true;
                    } else if (itemId == R.id.menu_none_filter) {
                        finishedBooksAdapter.setFilteredList(finishedBooks);
                        lastWeekFilter = false;
                        lastMonthFilter = false;
                        binding.finishedTitle.setText(getString(R.string.finishedBooksTitle) + finishedBooks.size() + ")");
                        return true;
                    } else {
                        return false;
                    }
                });
            }
        });
    }

    private void getData() {
        compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(dao.getAllFinished()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        finishedBooks -> {
                            if (finishedBooks.size() == 0) {
                                whenNoData();
                            } else {
                                initRecycler(finishedBooks);
                            }
                        }));
    }

    private void whenNoData() {
        binding.finishedBooksRecycler.setVisibility(View.GONE);
        binding.notFoundFinished.setVisibility(View.VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    private void initRecycler(List<FinishedBooks> booksList) {
        finishedBooks = booksList;
        binding.finishedTitle.setText(getString(R.string.finishedBooksTitle) + finishedBooks.size() + ")");
        RecyclerView recyclerView = binding.finishedBooksRecycler;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setHasFixedSize(true);
        finishedBooksAdapter = new FinishedBooksAdapter(requireContext(), finishedBooks);
        recyclerView.setAdapter(finishedBooksAdapter);
        recyclerView.setItemAnimator(new CustomItemAnimator());

        finishedBooksAdapter.setListener((book, position) -> {
            finishedBooks.remove(book);
            // If the search was made
            if (filteredList != null) {
                filteredList.remove(book);
            }
            // If the sort was made
            if (alphabeticList != null) {
                alphabeticList.remove(book);
            }
            //If the filter was made
            if (lastWeekList != null) {
                lastWeekList.remove(book);
            }
            //If the filter was made
            if (lastMonthList != null) {
                lastMonthList.remove(book);
            }

            finishedBooksAdapter.notifyItemRemoved(position);
            finishedBooksAdapter.notifyItemRangeChanged(position, finishedBooks.size());
            binding.finishedTitle.setText(getString(R.string.finishedBooksTitle) + finishedBooks.size() + ")");
        });

    }

    private void searchBooks(List<FinishedBooks> finishedBooks, String query) {
        filteredList = new ArrayList<>();

        String queryLowerCase = query.toLowerCase();

        for (FinishedBooks book : finishedBooks) {
            if (book.title.toLowerCase().contains(queryLowerCase)) {
                filteredList.add(book);
            }
        }
        finishedBooksAdapter.setFilteredList(filteredList);
    }

    @SuppressLint("SetTextI18n")
    private void alphabeticalSort(List<FinishedBooks> theListShown) {
        alphabeticList = theListShown;
        alphabeticList.sort(Comparator.comparing(FinishedBooks::getTitle, String.CASE_INSENSITIVE_ORDER));
        finishedBooksAdapter.setFilteredList(alphabeticList);
        binding.finishedTitle.setText(getString(R.string.alphabeticalTitle) + alphabeticList.size() + ")");
    }

    @SuppressLint("SetTextI18n")
    private void lastWeekFilter(List<FinishedBooks> finishedBooks) {
        lastWeekList = new ArrayList<>();

        // take now time
        long currentTimeMillis = System.currentTimeMillis();

        // Calculate the time one week ago
        long oneWeekMillis = 7 * 24 * 60 * 60 * 1000;
        long oneWeekAgoMillis = currentTimeMillis - oneWeekMillis;

        // Check each item in the list
        for (FinishedBooks book : finishedBooks) {
            long endDateMillis = book.endDate * 1000;

            // If the due date falls within the last 1 week, add the item to the filteredList
            if (endDateMillis >= oneWeekAgoMillis && endDateMillis <= currentTimeMillis) {
                lastWeekList.add(book);
            }
        }
        finishedBooksAdapter.setFilteredList(lastWeekList);
        binding.finishedTitle.setText(getString(R.string.lastWeekTitle) + lastWeekList.size() + ")");
    }


    @SuppressLint("SetTextI18n")
    private void lastMonthFilter(List<FinishedBooks> finishedBooks) {
        lastMonthList = new ArrayList<>();

        // take now time
        long currentTimeMillis = System.currentTimeMillis();

        // Calculate the time one week ago
        long oneMonthMillis = 30L * 24 * 60 * 60 * 1000;
        long oneWeekAgoMillis = currentTimeMillis - oneMonthMillis;

        // Check each item in the list
        for (FinishedBooks book : finishedBooks) {
            long endDateMillis = book.endDate * 1000;

            // If the due date falls in the last month, add the item to the filteredList
            if (endDateMillis >= oneWeekAgoMillis && endDateMillis <= currentTimeMillis) {
                lastMonthList.add(book);
            }
        }
        finishedBooksAdapter.setFilteredList(lastMonthList);
        binding.finishedTitle.setText(getString(R.string.lastMonthTitle) + lastMonthList.size() + ")");
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            bottomNavigationView = ((MainActivity) context).findViewById(R.id.bottomNavigationView);
            if (bottomNavigationView == null) {
                Log.e("MyFragment", "BottomNavigationView is null in MainActivity");
            }
        } else {
            throw new RuntimeException("MyFragment must be attached to MainActivity");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
        bottomNavigationView.setVisibility(View.VISIBLE);
        bottomNavigationView = null;
        compositeDisposable.clear();
        if (finishedBooksAdapter != null) {
            finishedBooksAdapter.clearMemory();
        }
    }


}