package com.example.eventplanner.adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.eventplanner.fragments.EventDetailsFragment;
import com.example.eventplanner.fragments.SubscriberFragment;
import com.example.eventplanner.models.Event;

public class SubscriberFragmentPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[] { "Details", "Who's going" };
    private Event event;

    public SubscriberFragmentPagerAdapter(FragmentManager fm, Event event) {
        super(fm);
        this.event = event;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new EventDetailsFragment();
                bundle.putParcelable("event", event);
                fragment.setArguments(bundle);
                return fragment;
            case 1:
                fragment = new SubscriberFragment();
                bundle.putParcelable("event", event);
                fragment.setArguments(bundle);
                return fragment;
            default:
                return null;
        }

    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
