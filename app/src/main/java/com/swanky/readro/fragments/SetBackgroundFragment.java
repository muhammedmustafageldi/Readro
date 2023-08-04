package com.swanky.readro.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.swanky.readro.R;
import com.swanky.readro.activities.MainActivity;
import com.swanky.readro.adapters.BackgroundItemsAdapter;
import com.swanky.readro.databinding.FragmentSetBackgroundBinding;
import com.swanky.readro.models.Book;
import com.swanky.readro.models.roomDbModel.BackgroundItem;
import com.swanky.readro.roomdb.BooksDao;
import com.swanky.readro.roomdb.BooksDatabase;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SetBackgroundFragment extends Fragment {

    private FragmentSetBackgroundBinding binding;
    private CompositeDisposable compositeDisposable;
    private BooksDao dao;
    private BottomNavigationView bottomNavigationView;

    public SetBackgroundFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSetBackgroundBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomNavigationView.setVisibility(View.GONE);

        BooksDatabase database = BooksDatabase.getInstance(requireContext());
        dao = database.booksDao();
        compositeDisposable = new CompositeDisposable();
        getData();

    }

    private void getData() {
        compositeDisposable.add(dao.getBackgroundItems()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(SetBackgroundFragment.this :: handleResponse));
    }

    private void handleResponse(List<BackgroundItem> backgroundItems) {
        BackgroundItemsAdapter adapter = new BackgroundItemsAdapter(backgroundItems, requireContext());
        ViewPager2 viewPager2 = binding.viewPagerOfBackgrounds;

        viewPager2.setAdapter(adapter);
        viewPager2.setOffscreenPageLimit(2);
        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.85f + r * 0.15f);
        });

        viewPager2.setPageTransformer(compositePageTransformer);
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
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
        bottomNavigationView.setVisibility(View.VISIBLE);
        bottomNavigationView = null;
    }
}