package com.example.eventplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.eventplanner.models.Event;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

public class ComposeEventActivity extends AppCompatActivity {

    private static final String TAG = "ComposeEventActivity";

    ImageView ivEventImage;
    Button btnAddPicture;
    EditText etTitle;
    EditText etDescription;
    EditText etRestrictions;
    Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_event);

        // find views
        ivEventImage = findViewById(R.id.ivEventImage);
        btnAddPicture = findViewById(R.id.btnAddPicture);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etRestrictions = findViewById(R.id.etRestrictions);
        btnSubmit = findViewById(R.id.btnSubmit);

        ivEventImage.setImageResource(R.drawable.blankpfp);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etTitle.getText().toString().isEmpty() || etDescription.getText().toString().isEmpty()
                        || etRestrictions.getText().toString().isEmpty()) {
                    Toast.makeText(ComposeEventActivity.this, "Please add in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // create the new event and return to old activity
                saveEventAndReturn(etTitle.getText().toString(), etDescription.getText().toString(), etRestrictions.getText().toString(),
                        ParseUser.getCurrentUser());


            }
        });
    }

    private void saveEventAndReturn(String title, String description, String restrictions, ParseUser user) {
        final Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setRestrictions(restrictions);
        event.setAuthor(user);
        event.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving event");
                    Toast.makeText(ComposeEventActivity.this, "Error while saving", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(ComposeEventActivity.this, "Post saved successfully", Toast.LENGTH_SHORT).show();


                // return to old context with this new data
                Intent intent = new Intent();
                intent.putExtra("event", Parcels.wrap(event));
                setResult(RESULT_OK, intent);
                finish();  // closes the activity, pass data to parent
            }
        });


    }
}