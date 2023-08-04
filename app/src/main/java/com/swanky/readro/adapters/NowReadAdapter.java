package com.swanky.readro.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;
import com.swanky.readro.R;
import com.swanky.readro.databinding.RecyclerRowNowReadBinding;
import com.swanky.readro.fragments.NowReadFragmentDirections;
import com.swanky.readro.models.roomDbModel.NowRead;
import com.swanky.readro.roomdb.BooksDao;
import com.swanky.readro.roomdb.BooksDatabase;
import java.util.List;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class NowReadAdapter extends RecyclerView.Adapter<NowReadAdapter.ViewHolder> {

    private Context mContext;
    private final List<NowRead> nowReads;
    private CompositeDisposable compositeDisposable;
    private MediaPlayer mediaPlayer;
    private OnItemDeleteListenerNowRead listener;
    private final NavHostFragment navHostFragment;

    public NowReadAdapter(Context mContext, List<NowRead> nowReads, NavHostFragment navHostFragment) {
        this.mContext = mContext;
        this.nowReads = nowReads;
        this.navHostFragment = navHostFragment;
    }

    public void setListener(OnItemDeleteListenerNowRead listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowNowReadBinding binding = RecyclerRowNowReadBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //Convert the byte array to bitmap
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(nowReads.get(position).image, 0, nowReads.get(position).image.length);

        holder.binding.nowReadTitle.setText(nowReads.get(position).title);
        holder.binding.nowReadImage.setImageBitmap(imageBitmap);
        int numberOfPages = nowReads.get(position).numberOfPages;
        int pageCountRead = nowReads.get(position).pageCountRead;
        int calculatedProgress = calculatePercentage(numberOfPages, pageCountRead);


        if (calculatedProgress > 100) {
            calculatedProgress = 100;
        }

        holder.binding.nowReadPageTxt.setText("Completed pages: " + pageCountRead);
        holder.binding.nowReadProgressBar.setProgress(calculatedProgress);
        holder.binding.nowReadProgressTxt.setText("%" + calculatedProgress);

        holder.itemView.setOnClickListener(view -> {
            NavDirections nowReadToReadRoom = NowReadFragmentDirections.actionNowReadFragmentToReadRoomFragment(nowReads.get(position));
            navHostFragment.getNavController().navigate(nowReadToReadRoom);
        });

        holder.itemView.setOnLongClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(mContext, view);
            popupMenu.setGravity(Gravity.CENTER);
            popupMenu.getMenuInflater().inflate(R.menu.now_read_menu, popupMenu.getMenu());
            popupMenu.show();

            holder.binding.containerImage.setBackgroundResource(R.drawable.container_bg_selected);

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_delete_nowRead) {
                    deleteBook(position);
                    return true;
                } else {
                    return false;
                }
            });

            popupMenu.setOnDismissListener(menu -> holder.binding.containerImage.setBackgroundResource(R.drawable.container_bg));

            return true;
        });
    }

    @Override
    public int getItemCount() {
        return nowReads.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerRowNowReadBinding binding;

        public ViewHolder(@NonNull RecyclerRowNowReadBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private int calculatePercentage(int firstNumber, int secondNumber) {
        if (secondNumber == 0) {
            return 0;
        } else {
            double ratio = (double) secondNumber / firstNumber;
            double percentage = ratio * 100;
            return (int) percentage;
        }
    }

    private void deleteBook(int position) {
        BooksDatabase database = BooksDatabase.getInstance(mContext);
        BooksDao dao = database.booksDao();
        compositeDisposable = new CompositeDisposable();

        compositeDisposable.add(dao.deleteNowRead(nowReads.get(position))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());

        //Notify fragment that item has been deleted
        if (listener != null) {
            listener.onItemDelete(nowReads.get(position), position);
        }
        mediaPlayer = MediaPlayer.create(mContext, R.raw.deletesound);
        mediaPlayer.start();
    }


    public interface OnItemDeleteListenerNowRead {
        void onItemDelete(NowRead book, int position);
    }

    public void clearMemory() {
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
        if (mediaPlayer != null) {
            mediaPlayer = null;
        }
        mContext = null;
    }


}
