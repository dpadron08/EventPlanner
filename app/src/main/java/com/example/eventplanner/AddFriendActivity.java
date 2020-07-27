package com.example.eventplanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.job.JobInfo;
import android.os.Bundle;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

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

public class AddFriendActivity extends AppCompatActivity {
    private static final String TAG = "AddFriendActivity";

    // UI elements
    TextInputEditText etSearchQuery;
    Button btnSearch;
    private String query;

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
        btnSearch = findViewById(R.id.btnSearch);
        rvFriends = findViewById(R.id.rvFriends);

        friendMatches = new ArrayList<>();
        adapter = new FriendsAdapter(this, friendMatches);
        rvFriends.setAdapter(adapter);
        rvFriends.setLayoutManager(new LinearLayoutManager(this));

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                query = etSearchQuery.getText().toString();
                search(query);
            }
        });

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
                    triGramAggregate.add(new TriGramList(user.getUsername()));
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
     * queried username. Assumes that the query is at index 0. Uses hash tables to check whether
     * matches exist between tri-gram lists
     * @param triGramAggregate the list of tri-gram lists
     * @param threshold the threshold above which two strings are considered a match
     */
    private void compareAggregateWithHash(ArrayList<TriGramList> triGramAggregate, double threshold) {
        TriGramList queryTriGramList = triGramAggregate.get(0);
        for (int i = 1; i < triGramAggregate.size(); i++) {

            // populate hash with query
            Map<String, Integer> map = new HashMap<>();
            for (int j = 0; j < queryTriGramList.triGramList.size(); j++) {

                if (map.get( queryTriGramList.triGramList.get(j) ) == null) {
                    map.put(queryTriGramList.triGramList.get(j), 1);
                }

            }

            ArrayList<String> currList = triGramAggregate.get(i).triGramList;
            // populate hash with user result
            for (int j = 0; j < currList.size(); j++) {
                if (map.get(currList.get(j)) == null) {
                    map.put(currList.get(j), 1);
                } else {
                    map.put(currList.get(j), 2);
                }
            }

            // determine if match
            double amountMactched = 0;
            Set<Map.Entry<String, Integer>> entries = map.entrySet();
            for (Map.Entry<String, Integer> entry : entries) {
                if (entry.getValue() == 2) {
                    amountMactched += 1;
                }
            }

            double probability = amountMactched /
                    (Math.max(queryTriGramList.triGramList.size(), currList.size()) );

            if (probability >= threshold) {
                triGramAggregate.get(i).didMatch = true;
            }

        }
    }
}