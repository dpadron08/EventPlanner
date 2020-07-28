package com.example.eventplanner.models;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.parceler.Parcel;

@Parcel(analyze={Event.class})
@ParseClassName("Comment")
public class Comment extends com.parse.ParseObject {

    /* Keys for getting custom attributes from Parse database*/
    public static final String KEY_TEXT = "text";
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_EVENT_OWNER = "eventOwner";

    // empty constructor for parceler library
    public Comment() {}

    public String getText() {
        return getString(KEY_TEXT);
    }
    public void setText(String text) {
        put(KEY_TEXT, text);
    }

    public ParseUser getAuthor() {
        return getParseUser(KEY_AUTHOR);
    }
    public void setAuthor(ParseUser author) {
        put(KEY_AUTHOR, author);
    }

    public ParseObject getEventOwner() {
        return getParseObject("eventOwner");
    }
    public void setEventOwner(ParseObject event) {
        put(KEY_EVENT_OWNER, event);
    }
}
