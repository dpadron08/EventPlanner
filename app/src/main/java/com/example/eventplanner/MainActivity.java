package com.example.eventplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        final Fragment theMap = new MapFragment();

        // add ability to switch between fragments
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment;
                switch (menuItem.getItemId()) {
                    case R.id.action_timeline:
                        //fragment = fragment1;
                        fragment = new TimelineFragment();
                        //Toast.makeText(MainActivity.this, "Timeline!", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.action_friends:
                        //fragment = fragment2;
                        fragment = new FriendsFragment();
                        //Toast.makeText(MainActivity.this, "Friends!", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.action_profile:
                        //fragment = fragment3;
                        fragment = new ProfileFragment();
                        //Toast.makeText(MainActivity.this, "Profile", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.action_map:
                        //Toast.makeText(MainActivity.this, "Map", Toast.LENGTH_SHORT).show();
                    default:
                        //fragment = fragment4;
                        //fragment = new MapFragment();
                        fragment = theMap;
                        break;
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });
        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_timeline);
    }


}