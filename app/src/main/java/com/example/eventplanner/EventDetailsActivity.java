package com.example.eventplanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.android.material.transition.platform.MaterialContainerTransform;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
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
        View finalContainer = findViewById(android.R.id.content);
        finalContainer.setTransitionName("shared_item_event");
        setEnterSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
        MaterialContainerTransform materialContainerTransform = new MaterialContainerTransform();
        materialContainerTransform.addTarget(finalContainer);
        //materialContainerTransform.setStartContainerColor(Color.WHITE);
        materialContainerTransform.setFadeMode(MaterialContainerTransform.FADE_MODE_OUT);
        materialContainerTransform.setDuration(500L);
        getWindow().setSharedElementEnterTransition(materialContainerTransform);

        MaterialContainerTransform materialContainerTransformReverse = new MaterialContainerTransform();
        materialContainerTransformReverse.addTarget(finalContainer);
        materialContainerTransformReverse.setFadeMode(MaterialContainerTransform.FADE_MODE_OUT);
        materialContainerTransformReverse.setDuration(500L);
        //materialContainerTransform.setAllContainerColors(Color.WHITE);
        getWindow().setSharedElementReturnTransition(materialContainerTransformReverse);

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

    /**
     * For reloading the image for the event in case it was updated when the user edited it
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: reloading image");
        event.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                ParseFile image = ((Event) object).getImage();
                if (image != null) {
                    Glide.with(EventDetailsActivity.this).load(image.getUrl()).into(ivImage);
                } else {
                    ivImage.setImageResource(R.drawable.blankpfp);
                }
            }
        });

    }


}