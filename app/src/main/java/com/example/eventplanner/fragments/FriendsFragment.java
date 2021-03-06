package com.example.eventplanner.fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.transition.Explode;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.eventplanner.AddFriendActivity;
import com.example.eventplanner.R;
import com.example.eventplanner.adapters.FriendsAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendsFragment extends Fragment {

    private static final String TAG = "FriendsFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    RecyclerView rvFriends;
    List<ParseUser> allFriends;
    FriendsAdapter adapter;
    Button btnAddFriend;
    FloatingActionButton btnFloatingAdd;

    // for the progress loading action item
    MenuItem miActionProgressItem;

    TextView tvNoFriendsFound;
    ImageView ivNoFriendsFound;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FriendsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendsFragment newInstance(String param1, String param2) {
        FriendsFragment fragment = new FriendsFragment();
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
        setupWindowAnimations();
    }

    private void setupWindowAnimations() {
        Slide slide = new Slide();
        slide.setDuration(700);
        getActivity().getWindow().setExitTransition(slide);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    // triggered soon after onCreateView
    // any view setup should occur here. View lookups, attaching view listeners
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnAddFriend = view.findViewById(R.id.btnAddFriend);
        btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goAddFriendActivity();
            }
        });
        btnFloatingAdd = view.findViewById(R.id.btnFloatingAdd);
        btnFloatingAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goAddFriendActivity();
            }
        });
        tvNoFriendsFound = view.findViewById(R.id.tvNoFriendsFound);
        ivNoFriendsFound = view.findViewById(R.id.ivNoFriendsFound);


        rvFriends = view.findViewById(R.id.rvFriends);
        allFriends = new ArrayList<>();
        adapter = new FriendsAdapter(getContext(), allFriends);
        // set the adapter on the recycler view
        rvFriends.setAdapter(adapter);
        rvFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        queryFriends();
    }

    private void goAddFriendActivity() {

        Intent intent = new Intent(getContext(), AddFriendActivity.class);
        // will need to convert to startActivityForResult when friend adding ability implemented
        //startActivity(intent); // no transitions

        // enter and exit explode transitions
        //getActivity().getWindow().setEnterTransition(new Explode());
        //getActivity().getWindow().setExitTransition(new Explode());


        startActivity(intent,
                ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());

    }

    // get friends from database
    private void queryFriends() {
        if (miActionProgressItem != null) {
            showProgressBar();
        }
        adapter.clear();
        // specify what type of data we want to query
        ParseRelation<ParseUser> relation = ParseUser.getCurrentUser().getRelation("friends");
        ParseQuery<ParseUser> query = relation.getQuery();
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Failed to query friends list");
                }
                allFriends.clear();
                allFriends.addAll(objects);
                adapter.notifyDataSetChanged();

                if (miActionProgressItem != null) {
                    hideProgressBar();
                }
                for (ParseUser a : allFriends) {
                    Log.i("FriendsFragment", "Friend :  " + a.getUsername());
                }
                toggleNoFriendsMessageVisibility();

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
        // refresh friends list after onViewCreated called once
        if (adapter!= null && allFriends != null) {
            queryFriends();
        }
    }

    private void toggleNoFriendsMessageVisibility() {
        if (adapter != null && allFriends != null) {
            if (adapter.getItemCount() == 0) {
                tvNoFriendsFound.setVisibility(View.VISIBLE);
                ivNoFriendsFound.setVisibility(View.VISIBLE);
            } else {
                tvNoFriendsFound.setVisibility(View.GONE);
                ivNoFriendsFound.setVisibility(View.GONE);
            }
        }
    }

    // for inflating our custom action bar with menu items
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

        // return to finish
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