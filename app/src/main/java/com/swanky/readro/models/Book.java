package com.swanky.readro.models;

import java.io.Serializable;

public class Book implements Serializable {

    private String id;
    private String title;
    private String author;
    private int pageCount;
    private String imageLink;
    private String language;
    private String publishedDate;

    public Book(String id, String title, String author, int pageCount, String imageLink, String language, String publishedDate) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.pageCount = pageCount;
        this.imageLink = imageLink;
        this.language = language;
        this.publishedDate = publishedDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }
}
