package com.swanky.readro.fragments.addFragments;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.swanky.readro.R;
import com.swanky.readro.activities.AddYourSelfActivity;
import com.swanky.readro.activities.MainActivity;
import com.swanky.readro.databinding.FragmentCompleteSaveBinding;


public class CompleteSaveFragment extends Fragment {

    private FragmentCompleteSaveBinding binding;
    private MediaPlayer player;

    public CompleteSaveFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCompleteSaveBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        player = MediaPlayer.create(requireContext(), R.raw.successful);
        player.start();

        binding.backToMainButton.setOnClickListener(view1 -> {
            player.stop();
            Intent intent = new Intent(requireContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.lottieCompleteAnim.cancelAnimation();
        binding = null;
        if (player != null){
            player.stop();
            player = null;
        }
    }
}