package com.example.eventplanner.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.eventplanner.ComposeEventActivity;
import com.example.eventplanner.R;
import com.example.eventplanner.adapters.EventsAdapter;
import com.example.eventplanner.models.Event;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TimelineFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TimelineFragment extends Fragment {

    private static final String TAG = "TimelineFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    Button btnAddEvent;
    
    // for recycler view list of events
    RecyclerView rvEvents;
    protected EventsAdapter adapter;
    protected List<Event> allEvents;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TimelineFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TimelineFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TimelineFragment newInstance(String param1, String param2) {
        TimelineFragment fragment = new TimelineFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_timeline, container, false);
    }

    // triggered soon after onCreateView
    // any view setup should occur here. View lookups, attaching view listeners
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnAddEvent = view.findViewById(R.id.btnAddEvent);
        
        rvEvents = view.findViewById(R.id.rvEvents);
        allEvents = new ArrayList<>();
        adapter = new EventsAdapter(getContext(), allEvents);
        rvEvents.setAdapter(adapter);
        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));

        btnAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goComposeEventActivity();
            }
        });
        
        queryEvents();
    }

    private void queryEvents() {
        adapter.clear();
        ParseQuery<Event> query = ParseQuery.getQuery(Event.class);
        query.setLimit(20);
        query.include(Event.KEY_AUTHOR);
        // order posts by creation date (newest first)
        query.addDescendingOrder(Event.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Failed to query events");
                    return;
                }

                // for debugging purposes let's print every event description to logcat
                /*
                for (Event event : objects) {
                    Log.i(TAG, "Event title: " + event.getTitle() + "Description: " + event.getDescription()
                    + "image url: " + event.getImage().getUrl());
                }

                 */

                allEvents.addAll(objects);
                adapter.notifyDataSetChanged();

            }
        });
    }

    private void goComposeEventActivity() {
        Intent intent = new Intent(getContext(), ComposeEventActivity.class);
        // will need to convert to startActivityforResult
        startActivityForResult(intent, 20);
    }

    // an event was created
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 20  && resultCode == Activity.RESULT_OK) {
            final Event event = Parcels.unwrap(data.getParcelableExtra("event"));
            allEvents.add(0, event);
            adapter.notifyItemInserted(0);
            rvEvents.smoothScrollToPosition(0);

            // add newly created event to list of events the user is subscribed to
            final ParseUser user = ParseUser.getCurrentUser();
            user.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (e != null) {
                        Log.i(TAG, "Failed to add event to subscriptions list");
                        return;
                    }
                    user.getRelation("subscriptions").add(event);
                    user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            Log.i(TAG, "Saved created event to subscriptions list");
                        }
                    });
                }
            });

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}