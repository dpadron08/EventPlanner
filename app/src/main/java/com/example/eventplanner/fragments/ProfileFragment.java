package com.example.eventplanner.fragments;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.eventplanner.R;
import com.example.eventplanner.adapters.EventsAdapter;
import com.example.eventplanner.models.Event;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final String TAG = "ProfileFragment";

    // for recycler view list of events
    RecyclerView rvEvents;
    protected EventsAdapter adapter;
    protected List<Event> allEvents;

    // other profile elements
    ImageView ivProfilePic;
    TextView tvUsername;
    TextView tvInterests;




    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    // triggered soon after onCreateView
    // any view setup should occur here. View lookups, attaching view listeners
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivProfilePic = view.findViewById(R.id.ivProfilePicture);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvInterests = view.findViewById(R.id.tvInterests);
        queryUserProfile(); // get user attributes and populate the views with data


        rvEvents = view.findViewById(R.id.rvEvents);
        allEvents = new ArrayList<>();
        adapter = new EventsAdapter(getContext(), allEvents);
        rvEvents.setAdapter(adapter);
        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        querySubscribedEvents();
    }

    private void queryUserProfile() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        currentUser.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e != null) {
                    Log.i(TAG, "Failed to query current user");
                    return;
                }
                tvUsername.setText(((ParseUser)object).getUsername());
                tvInterests.setText(((ParseUser)object).getString("interests"));
                ParseFile image = ((ParseUser)object).getParseFile("profilePicture");
                if (image != null) {
                    Glide.with(getContext()).load(image.getUrl()).into(ivProfilePic);
                } else {
                    ivProfilePic.setImageResource(R.drawable.blankpfp);
                }
            }
        });


    }

    private void querySubscribedEvents() {
        adapter.clear();
        ParseRelation<Event> relation = ParseUser.getCurrentUser().getRelation("subscriptions");
        ParseQuery<Event> query = relation.getQuery();
        query.include("author");
        query.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {
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