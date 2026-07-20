package com.stella.ieltsreader;

public final class Article {
    public final String id;
    public final String category;
    public final String title;
    public final String body;
    public final int minutes;

    public Article(String id, String category, String title, String body, int minutes) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.body = body;
        this.minutes = minutes;
    }
}
