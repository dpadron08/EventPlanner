package com.example.eventplanner;

import android.app.Application;

import com.example.eventplanner.models.Comment;
import com.example.eventplanner.models.Event;
import com.parse.Parse;

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // set applicationId, and server server based on the values in the Heroku settings.
        // clientKey is not needed unless explicitly configured
        // any network interceptors must be added with the Configuration Builder given this syntax
        Event.registerSubclass(Event.class);
        Comment.registerSubclass(Comment.class);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("david-eventplanner") // should correspond to APP_ID env variable
                .clientKey(null)  // set explicitly unless clientKey is explicitly configured on Parse server
                .server("https://david-eventplanner.herokuapp.com/parse/").build());
    }
}
