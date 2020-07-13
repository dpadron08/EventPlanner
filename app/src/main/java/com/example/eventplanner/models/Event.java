package com.example.eventplanner.models;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseUser;

@ParseClassName("Event")
public class Event extends com.parse.ParseObject {

    public static final String KEY_TITLE = "title";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_AUTHOR = "author";
    //public static final String KEY_CREATED_AT = "createdAt"; // alrdy defined in superclass

    public Event() {}

    public String getTitle() {
        return getString(KEY_TITLE);
    }
    public void setTitle(String title) {
        put(KEY_TITLE, title);
    }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }
    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public ParseUser getAuthor() {
        return getParseUser(KEY_AUTHOR);
    }
    public void setAuthor(ParseUser author) {
        put(KEY_AUTHOR, author);
    }
}
