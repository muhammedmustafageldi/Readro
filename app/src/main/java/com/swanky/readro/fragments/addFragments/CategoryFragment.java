package com.swanky.readro.fragments.addFragments;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.swanky.readro.R;
import com.swanky.readro.databinding.FragmentCategoryBinding;
import java.util.ArrayList;


public class CategoryFragment extends Fragment {

    private FragmentCategoryBinding binding;
    private CardView selectedCard = null;
    private ArrayList<CardView> cardViews;
    private int selectedCategoryNumber;
   
    private MyCategoryListener listener;


    public CategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cardViews = new ArrayList<>();
        cardViews.add(binding.currentlyCategory);
        cardViews.add(binding.wishCategory);
        cardViews.add(binding.finishedCategory);

        binding.currentlyCategory.setOnClickListener(view1 -> {
            selectedCategoryNumber = 0;
            selectCardView(binding.currentlyCategory);
        });

        binding.wishCategory.setOnClickListener(view1 -> {
            selectedCategoryNumber = 1;
            selectCardView(binding.wishCategory);
        });

        binding.finishedCategory.setOnClickListener(view1 -> {
            selectedCategoryNumber = 2;
            selectCardView(binding.finishedCategory);
        });

    }

    private void selectCardView(CardView cardView) {
        for (CardView c : cardViews) {
            c.clearAnimation();
            c.setCardBackgroundColor(requireContext().getColor(R.color.white));
        }

        cardView.setCardBackgroundColor(requireContext().getColor(R.color.darkGreen));

        //add animation to selected CardView object
        Animator shakeAnimator = AnimatorInflater.loadAnimator(requireContext(), R.animator.cardview_anim);
        shakeAnimator.setTarget(cardView);
        shakeAnimator.start();

        // update the selected CardView object
        selectedCard = cardView;

        listener.categorySelected(selectedCategoryNumber);
    }

    public interface MyCategoryListener {
        void categorySelected(int selectedCategoryNumber);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MyCategoryListener) {
            listener = (MyCategoryListener) context;
        } else {
            throw new RuntimeException(context + " must implement MyFragmentListener");
        }
    }


}