package com.swanky.readro.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.swanky.readro.R;
import com.swanky.readro.activities.MainActivity;
import com.swanky.readro.adapters.RequestedBooksAdapter;
import com.swanky.readro.databinding.FragmentRequestedBinding;
import com.swanky.readro.models.roomDbModel.RequestedBooks;
import com.swanky.readro.roomdb.BooksDao;
import com.swanky.readro.roomdb.BooksDatabase;
import com.swanky.readro.service.CustomItemAnimator;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class RequestedFragment extends Fragment {

    private FragmentRequestedBinding binding;
    private BottomNavigationView bottomNavigationView;
    private BooksDao dao;
    private CompositeDisposable compositeDisposable;
    private RequestedBooksAdapter requestedBooksAdapter;


    public RequestedFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRequestedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomNavigationView.setVisibility(View.GONE);

        BooksDatabase database = Room.databaseBuilder(requireContext().getApplicationContext(), BooksDatabase.class, "Books").build();
        dao = database.booksDao();

        getData();
    }

    private void getData() {
        compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(dao.getAllRequested()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        requestedBooks -> {
                            if (requestedBooks.size() == 0) {
                                whenNoData();
                            } else {
                                initRecycler(requestedBooks);
                            }
                        }));
    }

    private void whenNoData() {
        binding.requestedRecycler.setVisibility(View.GONE);
        binding.notFoundRequested.setVisibility(View.VISIBLE);
    }

    private void initRecycler(List<RequestedBooks> requestedBooks) {
        RecyclerView recyclerView = binding.requestedRecycler;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setHasFixedSize(true);
        requestedBooksAdapter = new RequestedBooksAdapter(requireContext(), requestedBooks);
        recyclerView.setAdapter(requestedBooksAdapter);
        recyclerView.setItemAnimator(new CustomItemAnimator());

        requestedBooksAdapter.setListener((book, position) -> {
            requestedBooks.remove(book);
            requestedBooksAdapter.notifyItemRemoved(position);
            requestedBooksAdapter.notifyItemRangeChanged(position, requestedBooks.size());
        });
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
        if (requestedBooksAdapter != null){
            requestedBooksAdapter.clearMemory();
        }
    }
}