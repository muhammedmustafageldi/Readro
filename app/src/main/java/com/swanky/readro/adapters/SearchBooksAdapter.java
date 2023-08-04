package com.swanky.readro.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import com.swanky.readro.R;
import com.swanky.readro.activities.AddBookActivity;
import com.swanky.readro.databinding.RecyclerRowSearchBinding;
import com.swanky.readro.models.Book;
import java.util.ArrayList;

public class SearchBooksAdapter extends RecyclerView.Adapter<SearchBooksAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Book> books;

    public SearchBooksAdapter(Context context, ArrayList<Book> books) {
        this.context = context;
        this.books = books;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowSearchBinding binding = RecyclerRowSearchBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = books.get(position);
        holder.binding.searchRowTitle.setText(book.getTitle());
        holder.binding.searchRowAuthor.setText(book.getAuthor());
        holder.binding.searchRowLanguage.setText(context.getString(R.string.languageString)+book.getLanguage().toUpperCase());
        holder.binding.searchRowPageCount.setText(context.getString(R.string.pageCountString)+book.getPageCount());
        holder.binding.searchRowPublishDate.setText(context.getString(R.string.publishedDate)+book.getPublishedDate());

        Picasso.get().load(book.getImageLink())
                .resize(100,160)
                .placeholder(R.drawable.loading)
                .into(holder.binding.searchRowImage);

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, AddBookActivity.class);
            intent.putExtra("Book", books.get(position));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private RecyclerRowSearchBinding binding;

        public ViewHolder(RecyclerRowSearchBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
