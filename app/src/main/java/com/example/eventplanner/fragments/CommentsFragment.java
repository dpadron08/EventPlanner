package com.example.eventplanner.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.eventplanner.R;
import com.example.eventplanner.adapters.CommentsAdapter;
import com.example.eventplanner.adapters.FriendsAdapter;
import com.example.eventplanner.models.Comment;
import com.example.eventplanner.models.Event;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CommentsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CommentsFragment extends Fragment implements EditCommentDialogFragment.EditNameDialogListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_EVENT = "ARG_EVENT";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final String TAG = "CommentsFragment";
    RecyclerView rvComments;
    CommentsAdapter adapter;
    List<Comment> comments;
    Event event;
    FloatingActionButton btnFloatingAddComment;
    ConstraintLayout constraintLayout;

    public CommentsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment CommentsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CommentsFragment newInstance(String param1) {
        //CommentsFragment fragment = new CommentsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        CommentsFragment fragment = new CommentsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
        Bundle bundle = getArguments();
        if (bundle != null) {
            event = bundle.getParcelable("event");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_comments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        rvComments = view.findViewById(R.id.rvComments);
        btnFloatingAddComment = view.findViewById(R.id.btnFloatingAddComment);
        constraintLayout = view.findViewById(R.id.constraintLayout);
        comments = new ArrayList<>();
        adapter = new CommentsAdapter(getContext(), comments);
        rvComments.setAdapter(adapter);
        rvComments.setLayoutManager(new LinearLayoutManager(getContext()));
        queryComments();

        btnFloatingAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditDialog();
            }
        });
        
    }

    private void queryComments() {
        adapter.clear();
        ParseQuery<Comment> query = ParseQuery.getQuery(Comment.class);
        query.include(Comment.KEY_EVENT_OWNER);
        query.include(Comment.KEY_AUTHOR);
        query.whereEqualTo(Comment.KEY_EVENT_OWNER, event);
        query.addDescendingOrder(Comment.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Comment>() {
            @Override
            public void done(List<Comment> objects, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting comments", e);
                    return;
                }
                comments.addAll(objects);
                adapter.notifyDataSetChanged();

            }
        });
    }

    // Call this method to launch the edit dialog
    private void showEditDialog() {
        FragmentManager fm = getFragmentManager();
        EditCommentDialogFragment editNameDialogFragment = EditCommentDialogFragment.newInstance("Some Title", "Some arg");
        // SETS the target fragment for use later when sending results
        editNameDialogFragment.setTargetFragment(CommentsFragment.this, 300);
        editNameDialogFragment.show(fm, "fragment_edit_comment_dialog");
    }

    @Override
    public void onFinishEditDialog(String inputText) {
        //Toast.makeText(getContext(), "Hi, " + inputText, Toast.LENGTH_SHORT).show();
        if (inputText.isEmpty()) {
            Snackbar.make(constraintLayout, "Comment can't be empty", Snackbar.LENGTH_SHORT);
            return;
        }
        addComment(inputText);
    }

    private void addComment(String commentText) {
        Comment comment = new Comment();
        comment.setAuthor(ParseUser.getCurrentUser());
        comment.setEventOwner(event);
        comment.setText(commentText);

        comment.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.i(TAG, "done: Error saving comment");
                    return;
                }
                queryComments();

            }
        });

    }
}