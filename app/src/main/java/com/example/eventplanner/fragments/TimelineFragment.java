package com.example.eventplanner.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.eventplanner.ComposeEventActivity;
import com.example.eventplanner.LoginActivity;
import com.example.eventplanner.MainActivity;
import com.example.eventplanner.R;
import com.example.eventplanner.adapters.EventsAdapter;
import com.example.eventplanner.models.Event;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import listeners.EndlessRecyclerViewScrollListener;

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
    FloatingActionButton btnFloatingAdd;
    ConstraintLayout constraintLayout;
    SwipeRefreshLayout swipeContainer;
    int totalQueried = 0;
    private EndlessRecyclerViewScrollListener scrollListener;

    // for recycler view list of events
    RecyclerView rvEvents;
    protected EventsAdapter adapter;
    protected List<Event> allEvents;

    // for the progress loading action item
    MenuItem miActionProgressItem;

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
        setHasOptionsMenu(true);
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
        btnFloatingAdd = view.findViewById(R.id.btnFloatingAdd);
        constraintLayout = view.findViewById(R.id.constraintLayout);
        swipeContainer = view.findViewById(R.id.swipeContainer);

        rvEvents = view.findViewById(R.id.rvEvents);
        allEvents = new ArrayList<>();
        adapter = new EventsAdapter(getContext(), allEvents);
        rvEvents.setAdapter(adapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvEvents.setLayoutManager(linearLayoutManager);
        totalQueried = 0;

        btnAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goComposeEventActivity();
            }
        });
        btnFloatingAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goComposeEventActivity();
            }
        });

        rvEvents.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && btnFloatingAdd.getVisibility() == View.VISIBLE) {
                    btnFloatingAdd.hide();
                } else if (dy < 0 && btnFloatingAdd.getVisibility() != View.INVISIBLE) {
                    btnFloatingAdd.show();
                }
                if (dy > 0 && MainActivity.bottomNavigationView.getVisibility() == View.VISIBLE) {
                    MainActivity.bottomNavigationView.setVisibility(View.GONE);
                } else if (dy < 0 && MainActivity.bottomNavigationView.getVisibility() != View.INVISIBLE) {
                    MainActivity.bottomNavigationView.setVisibility(View.VISIBLE);
                }
            }
        });

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                totalQueried = 0;
                queryEvents();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.i(TAG, "onLoadMore: loading more");
                addMoreEvents();
            }
        };

        rvEvents.addOnScrollListener(scrollListener);

        queryEvents();

    }


    // get all most recent 20 events and put on timeline
    private void queryEvents() {
        if (miActionProgressItem != null) {
            showProgressBar();
        }
        adapter.clear();
        ParseQuery<Event> query = ParseQuery.getQuery(Event.class);
        query.setLimit(10);
        //query.setSkip(totalQueried);
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
                totalQueried += objects.size();
                allEvents.addAll(objects);
                removeEventsAtCapacity();
                //adapter.notifyDataSetChanged();  // uncomment when u dont want to filter
                if (miActionProgressItem != null) {
                    hideProgressBar();
                }
                swipeContainer.setRefreshing(false);

            }
        });
    }

    private void addMoreEvents() {
        if (miActionProgressItem != null) {
            showProgressBar();
        }
        ParseQuery<Event> query = ParseQuery.getQuery(Event.class);
        query.setLimit(10);
        query.setSkip(totalQueried);
        query.include(Event.KEY_AUTHOR);
        // order posts by creation date (newest first)
        query.addDescendingOrder(Event.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {
                if (miActionProgressItem != null) {
                    hideProgressBar();
                }
                if (e != null) {
                    Log.e(TAG, "Failed to query events");
                    return;
                }

                totalQueried += objects.size();
                allEvents.addAll(objects);
                removeEventsAtCapacity();
                adapter.notifyDataSetChanged();
            }
        });

    }

    private void removeEventsAtCapacity() {
        for (Event event : allEvents) {
            final Event currentEvent = event;
            currentEvent.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Error trying to remove events filled at capacity. Stopping now: ", e);
                        Snackbar.make(constraintLayout, "Error trying to remove events filled " +
                                "at capacity. Stopping now", Snackbar.LENGTH_SHORT)
                                .show();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    // get population count
                    final int population = currentEvent.getSubscriberCount();

                    // find out if current user is a subscriber
                    ParseRelation<ParseUser> relation = currentEvent.getRelation("subscribers");
                    ParseQuery<ParseUser> query = relation.getQuery();
                    query.findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> subscribers, ParseException e2) {
                            if (e2 != null) {
                                Log.e(TAG, "done: Error determining if user is a subscriber, for filtering. Stopping now", e2);
                                Snackbar.make(constraintLayout, "Error determining if user is " +
                                        "a subscriber, for filtering. Stopping now", Snackbar.LENGTH_SHORT)
                                        .show();
                                adapter.notifyDataSetChanged();
                                return;
                            }
                            boolean isSubscriber = false;
                            for (ParseUser subscriber : subscribers) {
                                if (subscriber.getObjectId().equals(ParseUser.getCurrentUser().getObjectId()) ) {
                                    isSubscriber = true;
                                    break;
                                }
                            }

                            // if user is not currently a subscriber and event is over or at capacity,
                            // remove this event
                            if (!isSubscriber && population >= currentEvent.getCapacity()) {
                                allEvents.remove(currentEvent);
                                Log.i(TAG, "done: Event deleted");
                            }
                            adapter.notifyDataSetChanged();

                        }
                    });
                }
            });
        }
    }

    // go to create a new event
    private void goComposeEventActivity() {
        Intent intent = new Intent(getContext(), ComposeEventActivity.class);

        startActivityForResult(intent, 20);
        getActivity().overridePendingTransition(R.anim.right_bottom_up, R.anim.slide_down_back);
    }

    // an event was created
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 20  && resultCode == Activity.RESULT_OK) {
            final Event event = Parcels.unwrap(data.getParcelableExtra("event"));
            // add the new event at the top of the timeline
            allEvents.add(0, event);
            adapter.notifyItemInserted(0);
            rvEvents.smoothScrollToPosition(0);

            Snackbar.make(constraintLayout, "Event saved successfully", Snackbar.LENGTH_LONG)
                    .show();

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

    // for toolbar
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu, this adds items to the action bar if it is present
        inflater.inflate(R.menu.menu_main, menu);
    }


    // for progress bar
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // Store instance of the menu item containing progress
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        Log.i(TAG, "Get here?");

        super.onPrepareOptionsMenu(menu);
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