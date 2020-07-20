package com.example.eventplanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
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

    // for recycler view list of events
    RecyclerView rvEvents;
    protected EventsAdapter adapter;
    protected List<Event> allEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_details);

        friendUser = (ParseUser) Parcels.unwrap(getIntent().getParcelableExtra("user"));
        tvFriendUsername = findViewById(R.id.tvFriendUsername);
        tvFriendInterests = findViewById(R.id.tvFriendInterests);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);

        tvFriendInterests.setText(friendUser.getString("interests"));
        tvFriendUsername.setText(friendUser.getUsername());
        ParseFile image = friendUser.getParseFile("profilePicture");
        if (image != null) {
            Glide.with(this).load(image.getUrl()).into(ivProfilePicture);
        } else {
            // set blank profile picture if friend has no profile pic
            ivProfilePicture.setImageResource(R.drawable.blankpfp);
        }

        rvEvents = findViewById(R.id.rvEvents);
        allEvents = new ArrayList<>();
        adapter = new EventsAdapter(this, allEvents);
        rvEvents.setAdapter(adapter);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        queryFriendSubscribedEvents();
    }

    private void queryFriendSubscribedEvents() {
        /*
        if (miActionProgressItem != null) {
            showProgressBar();
        }
        */

        adapter.clear();
        ParseRelation<Event> relation = friendUser.getRelation("subscriptions");
        ParseQuery<Event> query = relation.getQuery();
        query.include("author");
        query.addDescendingOrder(Event.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {

                /*if (miActionProgressItem != null) {
                    hideProgressBar();
                }
                */

                if (e != null) {
                    Log.e(TAG, "Failed to query subscribed to events");
                    return;
                }
                allEvents.addAll(objects);
                adapter.notifyDataSetChanged();;

            }
        });
    }
}