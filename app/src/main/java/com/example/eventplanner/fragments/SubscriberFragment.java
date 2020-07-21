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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.eventplanner.R;
import com.example.eventplanner.adapters.FriendsAdapter;
import com.example.eventplanner.models.Event;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SubscriberFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SubscriberFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PAGE = "ARG_PAGE";
    private static final String ARG_EVENT = "ARG_EVENT";

    private static final String TAG = "SubscriberFragment";

    private int mPage;
    Event event;
    RecyclerView rvSubscribers;
    FriendsAdapter adapter;
    List<ParseUser> subscribers;


    public SubscriberFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SubscriberFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SubscriberFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        SubscriberFragment fragment = new SubscriberFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getInt(ARG_PAGE);
        }
        Bundle bundle = getArguments();
        if(bundle != null) {
            event =  bundle.getParcelable("event");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subscriber, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvSubscribers = view.findViewById(R.id.rvSubscribers);

        subscribers = new ArrayList<>();
        adapter = new FriendsAdapter(getContext(), subscribers);
        rvSubscribers.setAdapter(adapter);
        rvSubscribers.setLayoutManager(new LinearLayoutManager(getContext()));
        querySubscribers();
    }

    private void querySubscribers() {
        ParseRelation<ParseUser> relation = event.getRelation("subscribers");
        ParseQuery<ParseUser> query = relation.getQuery();
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "done: Failed to query subscribers", e);
                    return;
                }
                subscribers.addAll(objects);
                adapter.notifyDataSetChanged();
            }
        });
    }
}