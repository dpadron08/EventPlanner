package com.example.eventplanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.eventplanner.adapters.EventsAdapter;
import com.example.eventplanner.models.Event;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class FriendDetailsActivity extends AppCompatActivity {
    private static final String TAG = "FriendDetailsActivity";

    ParseUser friendUser;

    TextView tvFriendUsername;
    TextView tvFriendInterests;
    ImageView ivProfilePicture;
    Button btnToggleFriend;

    // for recycler view list of events
    RecyclerView rvEvents;
    protected EventsAdapter adapter;
    protected List<Event> allEvents;

    // for the progress loading action item
    MenuItem miActionProgressItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_details);

        friendUser = (ParseUser) Parcels.unwrap(getIntent().getParcelableExtra("user"));
        tvFriendUsername = findViewById(R.id.tvFriendUsername);
        tvFriendInterests = findViewById(R.id.tvFriendInterests);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        btnToggleFriend = findViewById(R.id.btnToggleFriend);
        setTitle("Profile");

        tvFriendInterests.setText(friendUser.getString("interests"));
        tvFriendUsername.setText(friendUser.getUsername());
        ParseFile image = friendUser.getParseFile("profilePicture");
        if (image != null) {
            Glide.with(this).load(image.getUrl()).into(ivProfilePicture);
        } else {
            // set blank profile picture if friend has no profile pic
            ivProfilePicture.setImageResource(R.drawable.blankpfp);
        }

        queryFriendship();

        rvEvents = findViewById(R.id.rvEvents);
        allEvents = new ArrayList<>();
        adapter = new EventsAdapter(this, allEvents);
        rvEvents.setAdapter(adapter);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        queryFriendSubscribedEvents();

        btnToggleFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFriendship();
            }
        });

        preventFriendingYourself();
    }

    private void preventFriendingYourself() {
        if (friendUser.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
            btnToggleFriend.setVisibility(View.GONE);
        }
    }

    private void toggleFriendship() {
        ParseRelation<ParseUser> relation = ParseUser.getCurrentUser().getRelation("friends");
        ParseQuery<ParseUser> query = relation.getQuery();
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "done: Unable to query friendship status", e);
                    return;
                }
                boolean userIsFriend = false;
                for (ParseUser friend : objects) {
                    if (friend.getObjectId().equals(friendUser.getObjectId())) {
                        userIsFriend = true;
                        break;
                    }
                }
                if (userIsFriend) {
                    // unfollow this friend
                    btnToggleFriend.setText("Follow");
                    btnToggleFriend.setCompoundDrawablesRelativeWithIntrinsicBounds(getDrawable(R.drawable.ic_add_friend_24),
                            null, null, null);
                    ParseUser.getCurrentUser().getRelation("friends").remove(friendUser);
                } else {
                    // follow this friend
                    btnToggleFriend.setText("Unfollow");
                    btnToggleFriend.setCompoundDrawablesRelativeWithIntrinsicBounds(getDrawable(R.drawable.ic_unfollow_friend_24),
                            null, null, null);
                    ParseUser.getCurrentUser().getRelation("friends").add(friendUser);
                }
                ParseUser.getCurrentUser().saveInBackground();
            }
        });
    }

    private void queryFriendship() {
        ParseRelation<ParseUser> relation = ParseUser.getCurrentUser().getRelation("friends");
        ParseQuery<ParseUser> query = relation.getQuery();
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "done: Unable to query friendship status", e);
                    return;
                }
                // check if this user is a friend in order to style button
                boolean userIsFriend = false;
                for (ParseUser friend : objects) {
                    if (friend.getObjectId().equals(friendUser.getObjectId())) {
                        userIsFriend = true;
                        break;
                    }
                }
                if (userIsFriend) {
                    btnToggleFriend.setText("Unfollow");
                    btnToggleFriend.setCompoundDrawablesRelativeWithIntrinsicBounds(getDrawable(R.drawable.ic_unfollow_friend_24),
                            null, null, null);
                } else {
                    btnToggleFriend.setText("Follow");
                    btnToggleFriend.setCompoundDrawablesRelativeWithIntrinsicBounds(getDrawable(R.drawable.ic_add_friend_24),
                            null, null, null);
                }

            }
        });
    }

    private void queryFriendSubscribedEvents() {
        if (miActionProgressItem != null) {
            showProgressBar();
        }

        adapter.clear();
        ParseRelation<Event> relation = friendUser.getRelation("subscriptions");
        ParseQuery<Event> query = relation.getQuery();
        query.include("author");
        query.addDescendingOrder(Event.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {
                if (miActionProgressItem != null) {
                    hideProgressBar();
                }

                if (e != null) {
                    Log.e(TAG, "Failed to query subscribed to events");
                    return;
                }
                allEvents.addAll(objects);
                adapter.notifyDataSetChanged();;

            }
        });
    }

    // for toolbar

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu, this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (miActionProgressItem != null) {
            hideProgressBar();
        }
        return super.onCreateOptionsMenu(menu);
    }

    // for progress bar
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Store instance of the menu item containing progress
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        if (miActionProgressItem != null) {
            hideProgressBar();
        }
        Log.i(TAG, "Get here?");

        // return to finish
        return super.onPrepareOptionsMenu(menu);
    }

    // making the progress bar visible and invisible
    public void showProgressBar() {
        // Show progress item
        miActionProgressItem.setVisible(true);
    }

    public void hideProgressBar() {
        // Hide progress item
        miActionProgressItem.setVisible(false);
    }
}