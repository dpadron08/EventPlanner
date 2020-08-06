package com.example.eventplanner.fragments;

import android.app.Activity;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventplanner.EditEventActivity;
import com.example.eventplanner.R;
import com.example.eventplanner.models.Event;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventDetailsFragment extends Fragment {
    // Store instance variables
    private String title;
    private int page;

    private static final String TAG = "EventDetailsFragment";
    public static final int EDIT_EVENT_REQUEST_CODE = 45;

    TextView tvTitle;
    TextView tvDescription;
    TextView tvAuthor;
    TextView tvLocation;
    TextView tvDate;
    TextView tvRestrictions;
    TextView tvCapacity;
    Button btnCalendar;
    Button btnEdit;
    Button btnSubscription;

    private Event event;

    // newInstance constructor for creating fragment with arguments
    public static EventDetailsFragment newInstance(int page, String title) {
        EventDetailsFragment fragmentFirst = new EventDetailsFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");

        Bundle bundle = getArguments();
        if(bundle != null) {
            event =  bundle.getParcelable("event");
        }
        // allow fragment to make changes to actionbar
        setHasOptionsMenu(true);
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_details, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTitle = view.findViewById(R.id.tvTitle);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvAuthor = view.findViewById(R.id.tvAuthor);
        tvLocation = view.findViewById(R.id.tvLocation);
        tvDate = view.findViewById(R.id.tvDate);
        tvRestrictions = view.findViewById(R.id.tvRestrictions);
        tvCapacity = view.findViewById(R.id.tvCapacity);
        btnCalendar = view.findViewById(R.id.btnCalendar);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnSubscription = view.findViewById(R.id.btnSubscription);

        tvTitle.setText(event.getTitle());
        tvDescription.setText(event.getDescription());
        tvAuthor.setText(event.getAuthor().getUsername());

        // change the subscribe button depending on if user is subscribed
        styleSubscribeButton();

        // display Location if it exists
        if (event.getLocation() != null) {
            String locationStr = "" + getAddress(event.getLocation());
            tvLocation.setText(locationStr);
        }


        if (event.getDate() != null) {
            SimpleDateFormat targetFormat = new SimpleDateFormat("MM/dd/yyyy h:mm a", Locale.ENGLISH);
            Date date = event.getDate();
            String formattedDate = targetFormat.format(date);
            tvDate.setText(formattedDate);
        }
        tvRestrictions.setText(event.getRestrictions());

        ParseRelation<ParseUser> relation = event.getRelation("subscribers");
        ParseQuery<ParseUser> query = relation.getQuery();
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "done: Unable to fetch subscriber count", e);
                    return;
                }
                String capacity;
                if (event.getCapacity() > 0) {
                    capacity = objects.size() + "/" + event.getCapacity();
                } else {
                    capacity = "No capacity";
                }
                tvCapacity.setText(capacity);
            }
        });

        btnCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // launch calendar
                launchCalendar();
            }
        });
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchEditEventActivity();
            }
        });

        btnSubscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleSubscription();
            }
        });

        enforceOnlyAuthorCanEdit();
    }



    private void styleSubscribeButton() {
        ParseRelation<ParseUser> relation = event.getRelation("subscribers");
        ParseQuery<ParseUser> query = relation.getQuery();

        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                // once subscribers for event are found,

                    /*
                    for (ParseUser u : objects) {
                        Log.i(TAG, "Username subscribed: "+ u.getUsername());
                    }
                     */
                boolean currentUserIsSubscribed = false;
                for (ParseUser subscriber : objects) {
                    if (subscriber.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                        currentUserIsSubscribed = true;
                        break;
                    }
                }

                if (currentUserIsSubscribed) {
                    btnSubscription.setText("Subscribed");
                    btnSubscription.setCompoundDrawablesRelativeWithIntrinsicBounds(getActivity()
                            .getDrawable(R.drawable.ic_star_filled_24),null, null, null);
                } else {
                    btnSubscription.setText("Not subscribed");
                    btnSubscription.setCompoundDrawablesRelativeWithIntrinsicBounds(getActivity()
                            .getDrawable(R.drawable.ic_star_unfilled_24),null, null, null);
                }

            }
        });
    }


    private void enforceOnlyAuthorCanEdit() {
        if (!event.getAuthor().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
            btnEdit.setVisibility(View.GONE);
        } else {
            btnEdit.setVisibility(View.VISIBLE);
        }
    }

    private void launchEditEventActivity() {
        Intent intent = new Intent(getContext(), EditEventActivity.class);
        intent.putExtra("event", Parcels.wrap(event));
        startActivityForResult(intent, EDIT_EVENT_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == EDIT_EVENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            reQueryEvent();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void reQueryEvent() {

        event.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                event = (Event) object;
                tvTitle.setText(event.getTitle());
                tvDescription.setText(event.getDescription());
                tvAuthor.setText(event.getAuthor().getUsername());
                // display Location if it exists
                if (event.getLocation() != null) {
                    String locationStr = "" + getAddress(event.getLocation());
                    tvLocation.setText(locationStr);
                }
                if (event.getDate() != null) {
                    SimpleDateFormat targetFormat = new SimpleDateFormat("MM/dd/yyyy h:mm a", Locale.ENGLISH);
                    Date date = event.getDate();
                    String formattedDate = targetFormat.format(date);
                    tvDate.setText(formattedDate);
                }
                tvRestrictions.setText(event.getRestrictions());
            }
        });
    }

    private void launchCalendar() {
        Intent calIntent = new Intent(Intent.ACTION_INSERT);
        calIntent.setType("vnd.android.cursor.item/event");
        //calIntent.setData(CalendarContract.Events.CONTENT_URI);
        calIntent.putExtra(CalendarContract.Events.TITLE, event.getTitle());
        calIntent.putExtra(CalendarContract.Events.DESCRIPTION, event.getDescription());
        // setting time of the event by passing in milliseconds since 1970
        calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.getDate().getTime());

        String locationStr = getAddress(event.getLocation());
        calIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, locationStr);
        startActivity(calIntent);
    }

    private String getAddress(ParseGeoPoint location)  {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
            return "No address found";
        }
        return addresses.get(0).getAddressLine(0);
    }

    private void toggleSubscription() {
        // change the subscription status of this event for the user
        ParseRelation<ParseUser> relation = event.getRelation("subscribers");
        ParseQuery<ParseUser> query = relation.getQuery();
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(final List<ParseUser> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Unable to retrieve subscribers for event when toggling subscription", e);
                    return;
                }
                int population = objects.size();
                boolean isUserSubscribed = false;
                for (ParseUser subscriber : objects) {
                    if (subscriber.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                        isUserSubscribed = true;
                        break;
                    }
                }
                if (isUserSubscribed) {
                    // unsubscribe and change border color
                    event.getRelation("subscribers").remove(ParseUser.getCurrentUser());
                    ParseUser.getCurrentUser().getRelation("subscriptions").remove(event);
                    btnSubscription.setText("Not subscribed");
                    btnSubscription.setCompoundDrawablesRelativeWithIntrinsicBounds(getActivity()
                            .getDrawable(R.drawable.ic_star_unfilled_24),null, null, null);
                    population--;

                } else {
                    // subscribe and change border color
                    event.getRelation("subscribers").add(ParseUser.getCurrentUser());
                    ParseUser.getCurrentUser().getRelation("subscriptions").add(event);
                    btnSubscription.setText("Subscribed");
                    btnSubscription.setCompoundDrawablesRelativeWithIntrinsicBounds(getActivity()
                            .getDrawable(R.drawable.ic_star_filled_24),null, null, null);
                    population++;
                }
                final int finalPopulation = population;
                //possible problem with race conditions
                event.setSubscriberCount(finalPopulation);
                event.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "done: Unable to add to event's subsscribers", e);
                            return;
                        }
                        if (event.getCapacity() > 0) {
                            String capacity = finalPopulation +"/" +event.getCapacity();
                            tvCapacity.setText(capacity);
                        }
                        ParseUser.getCurrentUser().saveInBackground();
                    }
                });


            }
        });
    }
}