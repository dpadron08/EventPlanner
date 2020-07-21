package com.example.eventplanner.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.eventplanner.R;
import com.example.eventplanner.models.Event;
import com.parse.ParseFile;

import java.util.Date;

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

    private int mPage;


    public SubscriberFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("ABC", "Page: "+mPage);
        return inflater.inflate(R.layout.fragment_subscriber, container, false);
    }


}