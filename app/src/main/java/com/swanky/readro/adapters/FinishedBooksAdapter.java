package com.swanky.readro.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.swanky.readro.R;
import com.swanky.readro.databinding.ItemBookBinding;
import com.swanky.readro.models.roomDbModel.FinishedBooks;
import com.swanky.readro.roomdb.BooksDao;
import com.swanky.readro.roomdb.BooksDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class FinishedBooksAdapter extends RecyclerView.Adapter<FinishedBooksAdapter.ViewHolder> {

    private Context context;
    private List<FinishedBooks> finishedBooks;
    private CompositeDisposable compositeDisposable;
    private MediaPlayer mediaPlayer;
    private OnItemDeleteListenerFinished listener;


    public FinishedBooksAdapter(Context context, List<FinishedBooks> finishedBooks) {
        this.context = context;
        this.finishedBooks = finishedBooks;
    }

    public void setListener(OnItemDeleteListenerFinished listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setFilteredList(List<FinishedBooks> filteredList) {
        this.finishedBooks = filteredList;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookBinding binding = ItemBookBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //Convert byte array to bitmap
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(finishedBooks.get(position).image, 0, finishedBooks.get(position).image.length);

        holder.binding.itemBookImg.setImageBitmap(imageBitmap);
        holder.binding.itemBookTitle.setText(finishedBooks.get(position).title);
        holder.binding.itemBookAuthor.setText(finishedBooks.get(position).author);
        holder.binding.itemBookPages.setText(finishedBooks.get(position).numberOfPages + " " + context.getString(R.string.pageSt));
        //Convert unix time to date String
        if (finishedBooks.get(position).endDate == 0L) {
            holder.binding.itemBookDate.setText("Unknown");
        } else {
            Date date = new Date(finishedBooks.get(position).endDate * 1000);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String dateString = dateFormat.format(date);
            holder.binding.itemBookDate.setText(dateString);
        }


        //Delete books
        holder.binding.deleteBooksButton.setOnClickListener(view -> {
            // Delete selected book
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogTheme);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.layout_warning_dailog, null);
            builder.setView(dialogView);

            ImageView alertIcon = dialogView.findViewById(R.id.alertIcon);
            Button buttonNegative = dialogView.findViewById(R.id.buttonNegative);
            Button buttonPositive = dialogView.findViewById(R.id.buttonPositive);
            TextView alertTitle = dialogView.findViewById(R.id.alertTitleTxt);
            TextView alertMessage = dialogView.findViewById(R.id.alertMessageTxt);

            alertIcon.setImageResource(R.drawable.ico_delete);
            alertTitle.setText("Delete this Book");
            alertMessage.setText("Are you sure you want to delete this book?");
            buttonNegative.setText("No");
            buttonPositive.setText("Yes");

            final AlertDialog alertDialog = builder.create();

            if (alertDialog.getWindow() != null) {
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            buttonPositive.setOnClickListener(view1 -> {
                BooksDatabase database = Room.databaseBuilder(context.getApplicationContext(), BooksDatabase.class, "Books").build();
                BooksDao dao = database.booksDao();

                compositeDisposable = new CompositeDisposable();
                compositeDisposable.add(dao.deleteFinished(finishedBooks.get(position))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe());

                //Notify fragment that item has been deleted
                if (listener != null) {
                    listener.onItemDelete(finishedBooks.get(position), position);
                }

                mediaPlayer = MediaPlayer.create(context, R.raw.deletesound);
                mediaPlayer.start();
                alertDialog.cancel();
            });

            buttonNegative.setOnClickListener(view1 -> alertDialog.cancel());

            alertDialog.show();
        });

    }

    @Override
    public int getItemCount() {
        return finishedBooks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemBookBinding binding;

        public ViewHolder(@NonNull ItemBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnItemDeleteListenerFinished {
        void onItemDelete(FinishedBooks book, int position);
    }

    public void clearMemory() {
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
        if (mediaPlayer != null) {
            mediaPlayer = null;
        }
        context = null;
    }

}
