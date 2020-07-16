package com.example.eventplanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.eventplanner.adapters.FriendsAdapter;
import com.example.eventplanner.models.Event;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.Date;
import java.util.List;

public class EventDetailsActivity extends AppCompatActivity {

    private static final String TAG = "EventDetailsActivity";

    Event event;

    TextView tvTitle;
    TextView tvDescription;
    TextView tvAuthor;
    TextView tvLocation;
    TextView tvDate;
    TextView tvRestrictions;
    ImageView ivImage;
    RelativeLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        tvAuthor = findViewById(R.id.tvAuthor);
        tvLocation = findViewById(R.id.tvLocation);
        tvDate = findViewById(R.id.tvDate);
        tvRestrictions = findViewById(R.id.tvRestrictions);
        ivImage = findViewById(R.id.ivImage);
        container = findViewById(R.id.container);

        event = (Event) Parcels.unwrap(getIntent().getParcelableExtra("event"));

        tvTitle.setText(event.getTitle());
        tvDescription.setText(event.getDescription());
        tvAuthor.setText(event.getAuthor().getUsername());

        if (event.getLocation() != null) {
            tvLocation.setText(event.getLocation().toString()); // getting geo point
        }

        if (event.getDate() != null) {
            String date = ((Date) event.getDate()).toString();
            tvDate.setText(date); // get Datetime
        }
        tvRestrictions.setText(event.getRestrictions());

        ParseFile image = event.getImage();
        if (image != null) {
            Glide.with(this).load(image.getUrl()).into(ivImage);
        } else {
            ivImage.setImageResource(R.drawable.blankpfp);
        }
    }
}