package com.swanky.readro.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.swanky.readro.R;
import com.swanky.readro.adapters.HomeItemsAdapter;
import com.swanky.readro.databinding.FragmentHomeBinding;
import com.swanky.readro.models.HomePageItem;

import java.util.ArrayList;
import java.util.Calendar;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

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


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }


}