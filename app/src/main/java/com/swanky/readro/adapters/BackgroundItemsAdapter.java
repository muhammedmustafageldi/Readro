package com.swanky.readro.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.swanky.readro.R;
import com.swanky.readro.databinding.BackgroundItemContainerBinding;
import com.swanky.readro.models.roomDbModel.BackgroundItem;
import java.util.List;

public class BackgroundItemsAdapter extends RecyclerView.Adapter<BackgroundItemsAdapter.ViewHolder> {

    private final List<BackgroundItem> backgroundItems;
    private final Context context;

    public BackgroundItemsAdapter(List<BackgroundItem> backgroundItems, Context context) {
        this.backgroundItems = backgroundItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BackgroundItemContainerBinding binding = BackgroundItemContainerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BackgroundItem backgroundItem = backgroundItems.get(position);

        holder.binding.imageBackgroundSlide.setImageResource(backgroundItem.getImageResource());
        holder.binding.priceTxt.setText(String.valueOf(backgroundItem.getImagePrice()));
        boolean available = backgroundItem.isAvailable();

        if (available){
            holder.binding.backgroundItemButton.setText("Arka planı ayarla");
            holder.binding.priceTxt.setText("Mevcut");
            holder.binding.priceIcon.setVisibility(View.GONE);
            holder.binding.availableIcon.setImageResource(R.drawable.ico_complete);
        }else{
            holder.binding.backgroundItemButton.setText("Arka planı aç" );
        }


    }

    @Override
    public int getItemCount() {
        return backgroundItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private BackgroundItemContainerBinding binding;

        public ViewHolder(@NonNull BackgroundItemContainerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }


}
