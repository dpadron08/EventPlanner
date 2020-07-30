package com.example.eventplanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.job.JobInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.eventplanner.adapters.FriendsAdapter;
import com.google.android.material.textfield.TextInputEditText;
import com.intuit.fuzzymatcher.component.MatchService;
import com.intuit.fuzzymatcher.domain.Document;
import com.intuit.fuzzymatcher.domain.Element;
import com.intuit.fuzzymatcher.domain.Match;
import com.intuit.fuzzymatcher.domain.MatchType;
import com.intuit.fuzzymatcher.function.TokenizerFunction;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An enum that describes where each tri-gram in the hash table comes from. If a tri-gram is found
 * in both the query from the search box and from user data, the tri-gram is labeled as a match for
 * the purposes of displaying search results.
 */
enum MatchLevel {
    FROM_QUERY, // this tri-gram came from the user query
    FROM_USER, // this tri-gram came from user data (username + interests)
    MATCH // this tri-gram found in both query and user data. We have a match
}

public class AddFriendActivity extends AppCompatActivity {
    private static final String TAG = "AddFriendActivity";

    // UI elements
    TextInputEditText etSearchQuery;
    private String query;
    ImageView ivNoUserFound;
    TextView tvNoUserfound;

    RecyclerView rvFriends;
    FriendsAdapter adapter;

    // users we found by searching
    List<ParseUser> friendMatches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        //getWindow().setExitTransition(new Explode());
        setupWindowAnimations();

        // setup UI
        etSearchQuery = findViewById(R.id.etSearchQuery);
        rvFriends = findViewById(R.id.rvFriends);
        ivNoUserFound = findViewById(R.id.ivNoUserFound);
        tvNoUserfound = findViewById(R.id.tvNoUserFound);
        setTitle("Add friend");

        friendMatches = new ArrayList<>();
        adapter = new FriendsAdapter(this, friendMatches);
        rvFriends.setAdapter(adapter);
        rvFriends.setLayoutManager(new LinearLayoutManager(this));

        etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                query = editable.toString();
                search(query);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (friendMatches == null) {
            return;
        }
        if (friendMatches.isEmpty()) {
            ivNoUserFound.setVisibility(View.VISIBLE);
            tvNoUserfound.setVisibility(View.VISIBLE);
        } else {
            ivNoUserFound.setVisibility(View.GONE);
            tvNoUserfound.setVisibility(View.GONE);
        }
    }

    private void search(String search) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();

        // query all users
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {

                // This is the list of all the triGramLists. A triGram list is a list of all the tri
                // grams for a string
                ArrayList<TriGramList> triGramAggregate = new ArrayList<>();

                //add the search query to the list of triGramLists
                triGramAggregate.add(new TriGramList(search));
                // add each users' username into the triGramListAggregate
                for (ParseUser user : objects) {
                    //triGramAggregate.add(new TriGramList(user.getUsername()));
                    triGramAggregate.add(new TriGramList(user.getUsername() + " " + user.getString("interests") ));
                }

                // determine whether or not each username in the database matches with the query
                // using our matching algorithm
                compareAggregateWithHash(triGramAggregate, 0.01);

                List<ParseUser> usersThatMatched = new ArrayList<>();

                // find all the users that matched
                adapter.clear();
                for (int i = 1; i < triGramAggregate.size(); i++) {
                    if (triGramAggregate.get(i).didMatch) {
                        usersThatMatched.add(objects.get(i-1));
                    }
                }
                friendMatches.addAll(usersThatMatched);
                adapter.notifyDataSetChanged();

                if (usersThatMatched.size() == 0) {
                    tvNoUserfound.setVisibility(View.VISIBLE);
                    ivNoUserFound.setVisibility(View.VISIBLE);
                } else {
                    tvNoUserfound.setVisibility(View.GONE);
                    ivNoUserFound.setVisibility(View.GONE);
                }

                Log.i(TAG, "done: Done");

            }
        });



    }

    private void setupWindowAnimations() {
        Fade fade = new Fade();
        fade.setDuration(1000);
        getWindow().setEnterTransition(fade);
    }

    /**
     * Converts a given string into an array list of strings representing the list of tri-grams for
     * that string
     */
    public class TriGramList {
        // the list of tri grams
        ArrayList<String> triGramList;

        // whether or not the string matched with the query
        boolean didMatch;

        // the string used to form the tri-gram list
        String originalString;

        public TriGramList(String originalString) {
            triGramList = new ArrayList<>();
            didMatch = false;
            // pre-format the given string. remove white space. Convert to all lowercase
            this.originalString = (originalString.replaceAll(" ", ""))
                    .toLowerCase();
            findTriGrams();
        }

        /**
         * Convert the given string to an array list of tri grams
         */
        private void findTriGrams() {
            if (originalString.length() < 3) {
                triGramList.add(originalString);
                return;
            }
            for (int i = 0; i < originalString.length(); i++) {
                if (originalString.length() - i <= 3) {
                    triGramList.add(originalString.substring(i, originalString.length()));
                    return;
                }
                triGramList.add(originalString.substring(i, i+3));
            }
        }
    }

    /**
     * Compares the tri gram list of the query with the tri grams of the queried total list of users.
     * In other words, finds the percentage tri-grams that match between two lists of tri-grams: that of query and each
     * queried user data. Assumes that the query is at index 0. Uses hash tables to check whether
     * matches exist between tri-gram lists
     * @param triGramAggregate the list of tri-gram lists
     * @param threshold the threshold above which two strings are considered a match
     */
    private void compareAggregateWithHash(ArrayList<TriGramList> triGramAggregate, double threshold) {
        TriGramList queryTriGramList = triGramAggregate.get(0);
        for (int i = 1; i < triGramAggregate.size(); i++) {

            // populate hash with query
            Map<String, MatchLevel> map = new HashMap<>();
            for (int j = 0; j < queryTriGramList.triGramList.size(); j++) {
                if (map.get( queryTriGramList.triGramList.get(j) ) == null) {
                    map.put(queryTriGramList.triGramList.get(j), MatchLevel.FROM_QUERY);
                }
            }

            ArrayList<String> currList = triGramAggregate.get(i).triGramList;
            // populate hash with user result
            for (int j = 0; j < currList.size(); j++) {

                if (map.get(currList.get(j)) == null) {
                    map.put(currList.get(j), MatchLevel.FROM_USER);
                } else if (map.get(currList.get(j)) == MatchLevel.FROM_QUERY) {
                    // we have match between query and user string
                    map.put(currList.get(j), MatchLevel.MATCH);
                } else if (map.get(currList.get(j)) == MatchLevel.FROM_USER) {
                    // nothing, a match between user string tri-grams
                }
            }

            // determine if match
            double amountMatched = 0;
            Set<Map.Entry<String, MatchLevel>> entries = map.entrySet();
            for (Map.Entry<String, MatchLevel> entry : entries) {
                if (entry.getValue() == MatchLevel.MATCH) {
                    amountMatched += 1;
                }
            }

            double probability = amountMatched /
                    (Math.max(queryTriGramList.triGramList.size(), currList.size()) );

            if (probability >= threshold) {
                triGramAggregate.get(i).didMatch = true;
            }

        }
    }
}