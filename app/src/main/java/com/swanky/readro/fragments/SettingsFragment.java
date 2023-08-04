package com.swanky.readro.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import com.swanky.readro.R;
import com.swanky.readro.databinding.FragmentSettingsBinding;
import java.util.ArrayList;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SharedPreferences sharedPreferences;
    private boolean nightMode;
    private boolean silentMode;
    private ArrayList<CardView> settingsCards;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = requireContext().getSharedPreferences("MODE", Context.MODE_PRIVATE);
        nightMode = sharedPreferences.getBoolean("nightMode", false);
        silentMode = sharedPreferences.getBoolean("silentMode", false);
        binding.switchDarkMode.setChecked(nightMode);

        setDarkMode();
        cardsAnimation();
        viewInit();
        setSilentMode();

        binding.setBackgroundCard.setOnClickListener(view2 -> {
            NavHostFragment navHostFragment = (NavHostFragment) requireActivity().getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
            NavDirections settingsToSetBackground = SettingsFragmentDirections.actionSettingsFragmentToSetBackgroundFragment();
            assert navHostFragment != null;
            navHostFragment.getNavController().navigate(settingsToSetBackground);
        });

    }

    private void viewInit() {
        if (silentMode) {
            binding.silentModeIcon.setImageResource(R.drawable.ico_mute);
        } else {
            binding.silentModeIcon.setImageResource(R.drawable.ico_audio);
        }
    }

    private void setSilentMode() {
        binding.silentModeIcon.setOnClickListener(View -> {
            if (silentMode) {
                sharedPreferences.edit().putBoolean("silentMode", false).apply();
                binding.silentModeIcon.setImageResource(R.drawable.ico_audio);
                silentMode = false;
                Toast.makeText(requireContext(), R.string.voiceModeString, Toast.LENGTH_SHORT).show();
            } else {
                sharedPreferences.edit().putBoolean("silentMode", true).apply();
                binding.silentModeIcon.setImageResource(R.drawable.ico_mute);
                silentMode = true;
                Toast.makeText(requireContext(), R.string.silentModeString, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cardsAnimation() {
        settingsCards = new ArrayList<>();
        settingsCards.add(binding.darkModeCard);
        settingsCards.add(binding.setBackgroundCard);
        settingsCards.add(binding.notificationSoundCard);
        settingsCards.add(binding.languageCard);
        settingsCards.add(binding.importOrExportCard);
        settingsCards.add(binding.rateUsCard);

        for (CardView c : settingsCards) {
            c.setVisibility(View.GONE);
        }

        Animation animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in);

        new CountDownTimer(settingsCards.size() * 300L, 300) {
            int position = 0;

            public void onTick(long millisUntilFinished) {
                settingsCards.get(position).setVisibility(View.VISIBLE);
                settingsCards.get(position).startAnimation(animation);
                position++;
            }

            public void onFinish() {
                // Animation is complete.
                for (CardView c : settingsCards) {
                    c.clearAnimation();
                }
            }
        }.start();
    }

    private void setDarkMode() {
        binding.switchDarkMode.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                sharedPreferences.edit().putBoolean("nightMode", true).apply();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                sharedPreferences.edit().putBoolean("nightMode", false).apply();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (CardView c : settingsCards) {
            c.clearAnimation();
        }
        binding = null;
    }


}