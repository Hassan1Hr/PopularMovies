package com.example.hassan.popularmovies.model;

public class Review {

    private String id;
    private String author;
    private String content;

    public Review(String author,String content) {
        this.author = author;
        this.content = content;

    }



    public String getId() { return id; }

    public String getAuthor() { return author; }

    public String getContent() { return content; }
}
