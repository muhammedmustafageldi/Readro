package com.swanky.readro.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.SearchView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.swanky.readro.R;
import com.swanky.readro.activities.AddYourSelfActivity;
import com.swanky.readro.activities.CaptureActivity;
import com.swanky.readro.adapters.SearchBooksAdapter;
import com.swanky.readro.databinding.FragmentSearchBinding;
import com.swanky.readro.models.Book;
import com.swanky.readro.models.ResponseModelOfApi;
import com.swanky.readro.service.GoogleBooksAPI;
import com.swanky.readro.validators.BookValidator;
import java.util.ArrayList;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchFragment extends Fragment {
    private FragmentSearchBinding binding;
    private Retrofit retrofit;
    private CompositeDisposable compositeDisposable;
    private ArrayList<Book> books;
    private ActivityResultLauncher<ScanOptions> scanLauncher;
    private BookValidator validator;
    private CountDownTimer timer;


    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Gson gson = new GsonBuilder().setLenient().create();

        String baseUrl = "https://www.googleapis.com/";
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        compositeDisposable = new CompositeDisposable();
        validator = new BookValidator();

        registerLauncher();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.searchView.clearFocus();
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                binding.searchView.clearFocus();
                binding.infoLayout.setVisibility(View.GONE);
                binding.searchingLayout.setVisibility(View.VISIBLE);
                getDataFromAPI(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        fabShowDetail();

        binding.extendedFab.setOnClickListener(view2 -> startActivity(new Intent(requireContext(), AddYourSelfActivity.class)));

        binding.scannerImage.setOnClickListener(view2 -> scanIsbn());
    }


    private void getDataFromAPI(String query) {
        GoogleBooksAPI googleBooksAPI = retrofit.create(GoogleBooksAPI.class);
        String requestUrl = "books/v1/volumes?q=" + query + getString(R.string.requestUrlLastPart);
        compositeDisposable.add(googleBooksAPI.getData(requestUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse));
    }

    private void handleResponse(ResponseModelOfApi responseModel) {
        binding.notFoundLayout.setVisibility(View.GONE);
        binding.infoLayout.setVisibility(View.GONE);
        binding.searchingLayout.setVisibility(View.GONE);
        fabShowDetail();
        if (responseModel != null) {
            books = validator.validate(responseModel);
            if (books.size() != 0) {
                RecyclerView booksRecycler = binding.resultBooksRecycler;
                booksRecycler.setVisibility(View.VISIBLE);
                booksRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
                booksRecycler.setHasFixedSize(true);
                booksRecycler.setAdapter(new SearchBooksAdapter(requireContext(), books));
                recyclerItemAnimation(booksRecycler);
            } else {
                binding.resultBooksRecycler.setVisibility(View.GONE);
                binding.notFoundLayout.setVisibility(View.VISIBLE);
            }
        } else {
            //Error

        }
    }

    private void fabShowDetail() {
        binding.extendedFab.extend();
        timer = new CountDownTimer(1000, 3000) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                binding.extendedFab.shrink();
            }
        }.start();
    }

    private void recyclerItemAnimation(RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);
        recyclerView.setLayoutAnimation(controller);
        recyclerView.scheduleLayoutAnimation();
    }

    private void scanIsbn() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Kitabın arkasındaki isbn kodunu taratınız.");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureActivity.class);


        scanLauncher.launch(options);
    }

    private void registerLauncher() {
        scanLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null){
                binding.searchView.setQuery(result.getContents(), true);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.searchView.clearFocus();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
        if (timer != null) {
            timer.cancel();
        }
        binding = null;
    }
}