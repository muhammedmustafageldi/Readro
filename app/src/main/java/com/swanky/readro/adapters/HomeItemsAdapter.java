package com.swanky.readro.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;
import com.swanky.readro.activities.ReminderActivity;
import com.swanky.readro.databinding.RecyclerRowHomeItemsBinding;
import com.swanky.readro.fragments.HomeFragmentDirections;
import com.swanky.readro.models.HomePageItem;
import java.util.ArrayList;

public class HomeItemsAdapter extends RecyclerView.Adapter<HomeItemsAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<HomePageItem> homePageItems;
    private final NavHostFragment navHostFragment;

    public HomeItemsAdapter(Context context, ArrayList<HomePageItem> homePageItems, NavHostFragment navHostFragment) {
        this.context = context;
        this.homePageItems = homePageItems;
        this.navHostFragment = navHostFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowHomeItemsBinding binding = RecyclerRowHomeItemsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.binding.homeItemsNameTxt.setText(homePageItems.get(position).getItemName());
        holder.binding.homeItemsImageView.setImageResource(homePageItems.get(position).getItemIcon());


        switch (position) {
            case 0:
                holder.itemView.setOnClickListener(view -> {
                    NavDirections homeToNowRead = HomeFragmentDirections.actionHomeFragmentToNowReadFragment();
                    navHostFragment.getNavController().navigate(homeToNowRead);
                });
                break;
            case 1:
                holder.itemView.setOnClickListener(view -> {
                    NavDirections homeToRequested = HomeFragmentDirections.actionHomeFragmentToRequestedFragment();
                    navHostFragment.getNavController().navigate(homeToRequested);
                });
                break;
            case 2:
                //Go to finished books
                holder.itemView.setOnClickListener(view -> {
                    NavDirections homeToFinished = HomeFragmentDirections.actionHomeFragmentToFinishedBooksFragment();
                    navHostFragment.getNavController().navigate(homeToFinished);
                });
                break;
            case 3:
                holder.itemView.setOnClickListener(view -> context.startActivity(new Intent(context, ReminderActivity.class)));
                break;
        }

    }

    @Override
    public int getItemCount() {
        return homePageItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerRowHomeItemsBinding binding;

        public ViewHolder(RecyclerRowHomeItemsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
