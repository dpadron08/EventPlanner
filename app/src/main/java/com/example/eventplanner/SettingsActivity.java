package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.eventplanner.fragments.OrderDialogFragment;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class SettingsActivity extends AppCompatActivity implements OrderDialogFragment.OnDonePressedListener {

    private static final String TAG = "SettingsActivity";
    ParseUser currentUser;

    ImageView ivProfilePicture;
    TextView tvUsername;
    Switch swOldEventVisibility;
    Button btnOrder;
    Button btnLogout;
    OrderDialogFragment.Order order = OrderDialogFragment.Order.BY_CREATION_DATE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        tvUsername = findViewById(R.id.tvUsername);
        swOldEventVisibility = findViewById(R.id.swOldEventVisibility);
        btnOrder = findViewById(R.id.btnOrder);
        btnLogout = findViewById(R.id.btnLogout);
        currentUser = ParseUser.getCurrentUser();
        populateUserFieldsAndSettings();
        setTitle("Settings");

        btnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOrderDialog();
            }
        });

        swOldEventVisibility.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                saveHidePastEventsSetting(isChecked);
            }
        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userLogout();
            }
        });
    }

    private void userLogout() {
        ParseUser.logOut();
        if (ParseUser.getCurrentUser() == null) {
            Log.i(TAG, "Successfully logged out");
        }
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveHidePastEventsSetting(boolean isChecked) {
        ParseUser user = ParseUser.getCurrentUser();
        user.put("hidePastEvents", isChecked);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(SettingsActivity.this, "Unable to save setting", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void showOrderDialog() {
        FragmentManager fm = getSupportFragmentManager();
        int orderInt = 0;
        if (order == OrderDialogFragment.Order.BY_SCHEDULED_DATE) {
            orderInt = 1;
        }
        OrderDialogFragment orderDialogFragment = OrderDialogFragment.newInstance(orderInt);
        orderDialogFragment.show(fm, "fragment_order_dialog");
    }



    private void populateUserFieldsAndSettings() {
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
                if (fetchedUser.getBoolean("orderByCreationDate")) {
                    order = OrderDialogFragment.Order.BY_CREATION_DATE;
                } else {
                    order = OrderDialogFragment.Order.BY_SCHEDULED_DATE;
                }
                if (fetchedUser.getBoolean("hidePastEvents")) {
                    swOldEventVisibility.setChecked(true);
                } else {
                    swOldEventVisibility.setChecked(false);
                }
            }
        });
    }

    @Override
    public void sendSelectedRadioButton(OrderDialogFragment.Order order) {
        this.order = order;
    }
}