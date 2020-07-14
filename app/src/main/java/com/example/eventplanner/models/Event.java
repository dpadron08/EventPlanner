package com.example.eventplanner.models;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.parceler.Parcel;
import org.xml.sax.DTDHandler;

import java.util.Date;

@Parcel(analyze={Event.class})
@ParseClassName("Event")
public class Event extends com.parse.ParseObject {

    public static final String KEY_TITLE = "title";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_DATE = "date";
    //public static final String KEY_CREATED_AT = "createdAt"; // alrdy defined in superclass
    public static final String KEY_RESTRICTIONS = "restrictions";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_IMAGE = "image";


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

    public Date getDate() {
        Object object = get("date");
        return (Date) object;
    }
    // TODO set date

    public String getRestrictions() {
        return getString(KEY_RESTRICTIONS);
    }
    public void setRestrictions(String restrictions) {
        put(KEY_RESTRICTIONS, restrictions);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint(KEY_LOCATION);
    }
    // TODO set location

    public ParseFile getImage() {
        return getParseFile(KEY_IMAGE);
    }

    public void setImage(ParseFile parseFile) {
        put(KEY_IMAGE, parseFile);
    }
}
