package com.example.eventplanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.eventplanner.fragments.OrderDialogFragment;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.apache.commons.collections4.Get;

public class SettingsActivity extends AppCompatActivity {

    ParseUser currentUser;

    ImageView ivProfilePicture;
    TextView tvUsername;
    Switch swOldEventVisibility;
    Button btnOrder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        tvUsername = findViewById(R.id.tvUsername);
        swOldEventVisibility = findViewById(R.id.swOldEventVisibility);
        btnOrder = findViewById(R.id.btnOrder);
        currentUser = ParseUser.getCurrentUser();
        populateUserFields();
        setTitle("Settings");


        btnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOrderDialog();
            }
        });
    }

    private void showOrderDialog() {
        FragmentManager fm = getSupportFragmentManager();
        OrderDialogFragment editNameDialogFragment = OrderDialogFragment.newInstance("Some Title", "Hi");
        editNameDialogFragment.show(fm, "fragment_order_dialog");
    }

    private void populateUserFields() {
        currentUser.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e != null) {
                    return;
                }
                ParseUser fetchedUser = (ParseUser) object;
                tvUsername.setText(fetchedUser.getUsername());
                ParseFile image = fetchedUser.getParseFile("profilePicture");
                if (image != null) {
                    Glide.with(SettingsActivity.this).load(image.getUrl())
                            .transform(new CircleCrop())
                            .into(ivProfilePicture);
                } else {
                    // set blank profile picture if friend has no profile pic
                    ivProfilePicture.setImageResource(R.drawable.blankpfp);
                }
            }
        });
    }


}