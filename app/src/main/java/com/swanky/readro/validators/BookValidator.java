package com.swanky.readro.validators;

import com.swanky.readro.models.Book;
import com.swanky.readro.models.Item;
import com.swanky.readro.models.ResponseModelOfApi;
import com.swanky.readro.models.VolumeInfo;

import java.util.ArrayList;
import java.util.List;

public class BookValidator {

    private List<Item> items;
    private ArrayList<Book> validBooks;

    public BookValidator() {
        validBooks = new ArrayList<>();
    }

    public ArrayList<Book> validate(ResponseModelOfApi responseModelOfApi) {
        validBooks.clear();
        items = responseModelOfApi.getItems();
        if (items != null){
            for (Item item : items) {
                String id = item.getId();
                VolumeInfo volumeInfo = item.getVolumeInfo();

                if (id != null && volumeInfo != null) {
                    if (volumeInfo.getTitle() != null && volumeInfo.getAuthors() != null && volumeInfo.getPageCount() != null && volumeInfo.getLanguage() != null && volumeInfo.getImageLinks() != null && volumeInfo.getPublishedDate() != null) {
                        String title = volumeInfo.getTitle();
                        String author = volumeInfo.getAuthors().get(0);
                        int pageCount = volumeInfo.getPageCount();
                        String language = volumeInfo.getLanguage();
                        String publishedDate = volumeInfo.getPublishedDate();
                        String imageLink = volumeInfo.getImageLinks().getThumbnail();
                        validBooks.add(new Book(id, title, author, pageCount, imageLink, language, publishedDate));
                    }
                }
            }
        }
        return validBooks;
    }

}
