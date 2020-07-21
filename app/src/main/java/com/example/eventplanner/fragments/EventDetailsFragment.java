package com.example.eventplanner.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.eventplanner.R;
import com.example.eventplanner.models.Event;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.util.Date;

public class EventDetailsFragment extends Fragment {
    // Store instance variables
    private String title;
    private int page;

    TextView tvTitle;
    TextView tvDescription;
    TextView tvAuthor;
    TextView tvLocation;
    TextView tvDate;
    TextView tvRestrictions;

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

        tvTitle.setText(event.getTitle());
        tvDescription.setText(event.getDescription());
        tvAuthor.setText(event.getAuthor().getUsername());

        if (event.getLocation() != null) {
            tvLocation.setText(event.getLocation().toString()); // getting geo point
        }

        if (event.getDate() != null) {
            String date = ((Date) event.getDate()).toString();
            tvDate.setText(date); // get Datetime
        }
        tvRestrictions.setText(event.getRestrictions());


    }
}