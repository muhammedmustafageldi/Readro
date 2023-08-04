package com.swanky.readro.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import com.airbnb.lottie.LottieAnimationView;
import com.swanky.readro.R;
import com.swanky.readro.databinding.RecyclerRowRequestedBinding;
import com.swanky.readro.models.roomDbModel.NowRead;
import com.swanky.readro.models.roomDbModel.RequestedBooks;
import com.swanky.readro.roomdb.BooksDao;
import com.swanky.readro.roomdb.BooksDatabase;
import java.util.List;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RequestedBooksAdapter extends RecyclerView.Adapter<RequestedBooksAdapter.ViewHolder> {

    private Context context;
    private final List<RequestedBooks> requestedBooks;
    private CompositeDisposable compositeDisposable;
    private MediaPlayer mediaPlayer;
    private OnItemDeleteListenerRequested listener;


    public RequestedBooksAdapter(Context context, List<RequestedBooks> requestedBooks) {
        this.context = context;
        this.requestedBooks = requestedBooks;
    }

    public void setListener(OnItemDeleteListenerRequested listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowRequestedBinding binding = RecyclerRowRequestedBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //Convert the byte array to bitmap
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(requestedBooks.get(position).image, 0, requestedBooks.get(position).image.length);

        holder.binding.bookImageRequested.setImageBitmap(imageBitmap);
        holder.binding.bookTitleRequested.setText(requestedBooks.get(position).title);
        holder.binding.authorTxtRequested.setText(requestedBooks.get(position).author);

        TextView aboutTxt = holder.binding.aboutOfBookTxt;
        if (requestedBooks.get(position).aboutTheBook.isEmpty()) {
            aboutTxt.setGravity(Gravity.CENTER);
            holder.binding.aboutOfBookTxt.setText(R.string.no_summary_txt);
        } else {
            holder.binding.aboutOfBookTxt.setText(requestedBooks.get(position).aboutTheBook);
        }

        holder.binding.bookOptionsButton.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(context, view);
            popupMenu.setGravity(Gravity.END);
            popupMenu.getMenuInflater().inflate(R.menu.my_requested_options_menu, popupMenu.getMenu());
            popupMenu.show();

            popupMenu.setOnMenuItemClickListener(item -> {
                //Init objects of transaction
                BooksDatabase database = BooksDatabase.getInstance(context);
                BooksDao dao = database.booksDao();
                compositeDisposable = new CompositeDisposable();

                int itemId = item.getItemId();
                if (itemId == R.id.action_add_nowRead) {
                    // Add this book to currently reading
                    transferToNowRead(position, dao);
                    deleteBook(position, dao);
                    return true;
                } else if (itemId == R.id.action_delete_requested) {
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

                    buttonPositive.setOnClickListener(view1 ->{
                        deleteBook(position, dao);
                        alertDialog.cancel();
                    });

                    buttonNegative.setOnClickListener(view1 -> alertDialog.cancel());

                    alertDialog.show();
                    return true;
                } else {
                    return false;
                }
            });
        });

    }

    @Override
    public int getItemCount() {
        return requestedBooks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerRowRequestedBinding binding;

        public ViewHolder(@NonNull RecyclerRowRequestedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private void deleteBook(int position, BooksDao dao) {
        compositeDisposable.add(dao.deleteRequested(requestedBooks.get(position))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());

        //Notify fragment that item has been deleted
        if (listener != null) {
            listener.onItemDelete(requestedBooks.get(position), position);
        }

        mediaPlayer = MediaPlayer.create(context, R.raw.deletesound);
        mediaPlayer.start();
    }

    private void transferToNowRead(int position, BooksDao dao) {
        String title = requestedBooks.get(position).title;
        String author = requestedBooks.get(position).author;
        int numberOfPages = requestedBooks.get(position).numberOfPages;
        byte[] imageData = requestedBooks.get(position).image;

        NowRead transferredBook = new NowRead(title, author, numberOfPages, 0, imageData);
        compositeDisposable.add(dao.insertNowRead(transferredBook)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());

        Dialog dialog = new Dialog(context, R.style.DialogTheme);
        @SuppressLint("InflateParams") View dialogView = LayoutInflater.from(context).inflate(R.layout.transfer_book_dialog, null);
        Button okButton = dialogView.findViewById(R.id.okButton);
        LottieAnimationView transferDialogAnim = dialogView.findViewById(R.id.transferDialogAnim);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(dialogView);
        dialog.show();

        okButton.setOnClickListener(view -> dialog.cancel());

        dialog.setOnCancelListener(dialogInterface -> transferDialogAnim.cancelAnimation());
    }

    public interface OnItemDeleteListenerRequested {
        void onItemDelete(RequestedBooks book, int position);
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
