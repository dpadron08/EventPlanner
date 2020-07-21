package com.example.eventplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;

import com.example.eventplanner.fragments.FriendsFragment;
import com.example.eventplanner.fragments.MapFragment;
import com.example.eventplanner.fragments.ProfileFragment;
import com.example.eventplanner.fragments.TimelineFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    final FragmentManager fragmentManager = getSupportFragmentManager();
    private BottomNavigationView bottomNavigationView;
    int startingPosition = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        final Fragment theMap = new MapFragment();

        //load the first fragment
        Fragment firstFragment = new TimelineFragment();
        fragmentManager.beginTransaction().replace(R.id.flContainer, firstFragment).commit();

        // add ability to switch between fragments
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment;
                int newPosition = 1;
                switch (menuItem.getItemId()) {
                    case R.id.action_timeline:
                        //fragment = fragment1;
                        fragment = new TimelineFragment();
                        setTitle("My Timeline");
                        newPosition = 1;
                        break;
                    case R.id.action_friends:
                        //fragment = fragment2;
                        fragment = new FriendsFragment();
                        setTitle("Friends");
                        newPosition = 2;
                        break;
                    case R.id.action_profile:
                        //fragment = fragment3;
                        fragment = new ProfileFragment();
                        setTitle("Profile");
                        newPosition = 3;
                        break;
                    case R.id.action_map:
                    default:
                        //fragment = fragment4;
                        //fragment = new MapFragment();
                        fragment = theMap;
                        setTitle("Map");
                        newPosition = 4;
                        break;
                }
                // old way to begin transaction
                //fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();

                // custom animation with only one left to right animation
                //fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left).replace(R.id.flContainer, fragment).commit();

                // animations using the correct swiping to/from left/right
                doAnimation(fragmentManager, fragment, newPosition);
                return true;
            }
        });
        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_timeline);
    }

    private void doAnimation(FragmentManager fragmentManager, Fragment fragment, int newPosition) {
        if(fragment != null) {
            if(startingPosition > newPosition) {
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right )
                        .replace(R.id.flContainer, fragment)
                        .commit();
            }
            if(startingPosition < newPosition) {
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                        .replace(R.id.flContainer, fragment)
                        .commit();
            }
            startingPosition = newPosition;
        }
    }
}