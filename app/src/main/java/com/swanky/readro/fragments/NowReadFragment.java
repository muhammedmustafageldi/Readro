package com.swanky.readro.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.swanky.readro.R;
import com.swanky.readro.activities.MainActivity;
import com.swanky.readro.adapters.NowReadAdapter;
import com.swanky.readro.databinding.FragmentNowReadBinding;
import com.swanky.readro.models.roomDbModel.NowRead;
import com.swanky.readro.roomdb.BooksDao;
import com.swanky.readro.roomdb.BooksDatabase;
import com.swanky.readro.service.CustomItemAnimator;
import java.util.List;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class NowReadFragment extends Fragment {

    private FragmentNowReadBinding binding;
    private CompositeDisposable compositeDisposable;
    private BottomNavigationView bottomNavigationView;
    private NowReadAdapter adapter;

    public NowReadFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentNowReadBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomNavigationView.setVisibility(View.GONE);

        getData();
    }

    private void getData() {
        BooksDatabase database = BooksDatabase.getInstance(requireContext());
        BooksDao dao = database.booksDao();

        compositeDisposable = new CompositeDisposable();

        compositeDisposable.add(dao.getAllNowRead()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        nowReads -> {
                            if (nowReads.size() == 0) {
                                whenNoData();
                            } else {
                                setRecyclerView(nowReads);
                            }
                        }));
    }

    private void whenNoData() {
        binding.nowReadRecycler.setVisibility(View.GONE);
        binding.notFoundNowRead.setVisibility(View.VISIBLE);
    }

    private void setRecyclerView(List<NowRead> nowReads) {
        RecyclerView recyclerView = binding.nowReadRecycler;
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        NavHostFragment navHostFragment = (NavHostFragment) requireActivity().getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
        adapter = new NowReadAdapter(requireContext(), nowReads, navHostFragment);
        recyclerView.setAdapter(adapter);
        //Set animation of recyclerview
        recyclerItemAnimation(recyclerView);
        recyclerView.setItemAnimator(new CustomItemAnimator());

        adapter.setListener((book, position) -> {
            nowReads.remove(book);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, nowReads.size());
        });
    }

    private void recyclerItemAnimation(RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);
        recyclerView.setLayoutAnimation(controller);
        recyclerView.scheduleLayoutAnimation();
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
        bottomNavigationView.setVisibility(View.VISIBLE);
        bottomNavigationView = null;
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
        if (adapter != null) {
            adapter.clearMemory();
        }
    }
}