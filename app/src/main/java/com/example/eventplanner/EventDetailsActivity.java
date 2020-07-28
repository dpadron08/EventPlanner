package com.example.eventplanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.eventplanner.adapters.FriendsAdapter;
import com.example.eventplanner.adapters.SubscriberFragmentPagerAdapter;
import com.example.eventplanner.fragments.EventDetailsFragment;
import com.example.eventplanner.fragments.SubscriberFragment;
import com.example.eventplanner.models.Event;
import com.google.android.material.tabs.TabLayout;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.Date;
import java.util.List;

public class EventDetailsActivity extends AppCompatActivity {

    private static final String TAG = "EventDetailsActivity";

    Event event;


    ConstraintLayout container;
    FragmentPagerAdapter adapterViewPager;
    ImageView ivImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        ivImage = findViewById(R.id.ivImage);
        container = findViewById(R.id.container);
        setTitle("Details");

        event = (Event) Parcels.unwrap(getIntent().getParcelableExtra("event"));
        ParseFile image = event.getImage();
        if (image != null) {
            Glide.with(this).load(image.getUrl()).into(ivImage);
        } else {
            ivImage.setImageResource(R.drawable.blankpfp);
        }

        ViewPager vpPager = (ViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new SubscriberFragmentPagerAdapter(getSupportFragmentManager(), event);
        vpPager.setAdapter(adapterViewPager);

        // Attach the page change listener inside the activity
        vpPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            // This method will be invoked when a new page becomes selected.
            @Override
            public void onPageSelected(int position) {
                adapterViewPager.getItem(position);
            }

            // This method will be invoked when the current page is scrolled
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Code goes here
            }

            // Called when the scroll state changes:
            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
            @Override
            public void onPageScrollStateChanged(int state) {
                // Code goes here
            }
        });


    }


}