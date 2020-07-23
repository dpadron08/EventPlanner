package com.example.eventplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.intuit.fuzzymatcher.component.MatchService;
import com.intuit.fuzzymatcher.domain.Document;
import com.intuit.fuzzymatcher.domain.Element;
import com.intuit.fuzzymatcher.domain.Match;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.intuit.fuzzymatcher.domain.ElementType.ADDRESS;
import static com.intuit.fuzzymatcher.domain.ElementType.NAME;

public class AddFriendActivity extends AppCompatActivity {
    private static final String TAG = "AddFriendActivity";

    TextInputEditText etSearchQuery;
    Button btnSearch;
    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        //getWindow().setExitTransition(new Explode());
        setupWindowAnimations();

        etSearchQuery = findViewById(R.id.etSearchQuery);
        btnSearch = findViewById(R.id.btnSearch);
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
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                /*
                for (ParseUser user : objects) {
                    Log.i(TAG, "Username: " + user.getUsername());
                }
                 */
                List<QueryItem> input = new ArrayList<>();
                input.add(new QueryItem("1", search));
                for (int i = 0; i < objects.size(); i++) {
                    input.add(new QueryItem(Integer.toString(i+2), objects.get(i).getUsername()));
                }

                List<Document> documentList = new ArrayList<>();
                for (QueryItem item : input) {
                    documentList.add(new Document.Builder(item.id)
                    .addElement(new Element.Builder<String>().setValue(item.name).setType(NAME).setThreshold(0).createElement()).createDocument());
                }

                MatchService matchService = new MatchService();
                Map<String, List<Match<Document>>> result = matchService.applyMatchByDocId(documentList);

                result.entrySet().forEach(entry -> {
                    entry.getValue().forEach(match -> {

                        // only get the items that the search matched with
                        if (entry.getKey().equals("1")) {
                            Log.i(TAG, "Key: " +entry.getKey() + " Data: " + match.getData() + " Matched With: " + match.getMatchedWith() + " Score: " + match.getScore().getResult());
                        }

                    });
                });

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
     * The input for the MatchService class
     */
    public class QueryItem {
        String id; // the key
        String name;

        public QueryItem(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}