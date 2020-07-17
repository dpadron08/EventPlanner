package com.example.eventplanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.eventplanner.fragments.DatePickerFragment;
import com.example.eventplanner.fragments.TimePickerFragment;
import com.example.eventplanner.models.Event;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class ComposeEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private static final String TAG = "ComposeEventActivity";

    // PICK_PHOTO_CODE is a constant integer
    public final static int PICK_PHOTO_CODE = 1046;
    public static final int PLACE_PICKER_REQUEST = 1;

    ImageView ivEventImage;
    Button btnAddPicture;
    EditText etTitle;
    EditText etDescription;
    EditText etRestrictions;
    Button btnSubmit;
    Button btnPickDate;
    Button btnPickTime;
    TextView tvDateTime;
    Button btnPickLocation;
    TextView tvLocationDisplay;
    ConstraintLayout constraintLayout;

    // for the progress loading action item
    MenuItem miActionProgressItem;

    // for getting time, date, location, image for event
    private File photoFile = null;
    Calendar calendar = Calendar.getInstance();
    Date date =  null;
    boolean timePicked = false;
    boolean datePicked = false;
    LatLng eventLocation = null;

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
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickTime = findViewById(R.id.btnPickTime);
        tvDateTime = findViewById(R.id.tvDateTime);
        btnPickLocation = findViewById(R.id.btnPickLocation);
        tvLocationDisplay = findViewById(R.id.tvLocationDisplay);
        constraintLayout = findViewById(R.id.constraintLayout);

        ivEventImage.setImageResource(R.drawable.blankpfp);
        eventLocation = null;
        date = null;
        photoFile = null;

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etTitle.getText().toString().isEmpty() || etDescription.getText().toString().isEmpty()
                        || etRestrictions.getText().toString().isEmpty()) {
                    // prevent user from not adding a title, description, or restriction
                    //Toast.makeText(ComposeEventActivity.this, "Please add in all fields", Toast.LENGTH_SHORT).show();
                    Snackbar.make(constraintLayout, "Please fill in all fields", Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                // create the new event and return to old activity
                saveEventAndReturn(etTitle.getText().toString(), etDescription.getText().toString(), etRestrictions.getText().toString(),
                        ParseUser.getCurrentUser());


            }
        });

        btnAddPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // launch gallery view
                onPickPhoto(view);
            }
        });

        btnPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // launch date picker
                showDatePickerDialog(view);
            }
        });

        btnPickTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // launch time picker
                showTimePickerDialog(view);
            }
        });

        // launch location picker
        btnPickLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(ComposeEventActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    // saving event to database
    private void saveEventAndReturn(String title, String description, String restrictions, ParseUser user) {
        if (miActionProgressItem != null) {
            showProgressBar();
            Log.i(TAG, "save event and return");
        }
        final Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setRestrictions(restrictions);
        event.setAuthor(user);

        // only set datetime if both date and time were set
        if (datePicked && timePicked) {
            Date date = calendar.getTime();
            Log.i(TAG, "Date: "+date);
            event.setDate(date);
        }
        if (photoFile != null) {
            event.setImage(new ParseFile(photoFile));
        }
        if (eventLocation != null) {
            ParseGeoPoint parseGeoPoint = new ParseGeoPoint(eventLocation.latitude, eventLocation.longitude);
            event.setLocation(parseGeoPoint);
        }
        event.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (miActionProgressItem != null) {
                    hideProgressBar();
                }
                if (e != null) {
                    Log.e(TAG, "Error while saving event");
                    Toast.makeText(ComposeEventActivity.this, "Error while saving", Toast.LENGTH_SHORT).show();
                }
                //Toast.makeText(ComposeEventActivity.this, "Post saved successfully", Toast.LENGTH_SHORT).show();

                // return to old context with this new data
                Intent intent = new Intent();
                intent.putExtra("event", Parcels.wrap(event));
                setResult(RESULT_OK, intent);
                finish();  // closes the activity, pass data to parent
            }
        });

    }

    // Trigger gallery selection for a photo
    public void onPickPhoto(View view) {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if(Build.VERSION.SDK_INT > 27){
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    // see https://stackoverflow.com/questions/7769806/convert-bitmap-to-file
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            // TODO check if resultCode is OK
            Uri photoUri = data.getData();

            // Load the image located at photoUri into selectedImage
            Bitmap selectedImage = loadFromUri(photoUri);

            // convert the bitmap to file format
            photoFile = new File(this.getCacheDir(), "photo");
            try {
                photoFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Bitmap bitmap = selectedImage;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
            byte[] bitmapdata = bos.toByteArray();
            // write the bytes to a file
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(photoFile);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // load selected image into a preview
            ivEventImage.setImageBitmap(selectedImage);

        }
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode != RESULT_OK) {
                return;
            }
            Place place = PlacePicker.getPlace(data, this);
            eventLocation = place.getLatLng();
            //Toast.makeText(this, "Location Set!", Toast.LENGTH_SHORT).show();
            Snackbar.make(constraintLayout, "Location set", Snackbar.LENGTH_SHORT)
                    .show();
            tvLocationDisplay.setText("Location set!");
        }

    }

    // attach to an onclick handler to show the date picker
    public void showDatePickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    // once date is set, adjust internal variables
    @Override
    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        datePicked = true;
        // tell user to pick both time and date
        if (timePicked && datePicked) {
            tvDateTime.setText(calendar.getTime().toString());
        }
        if (!timePicked) {
            Snackbar.make(constraintLayout, "Please pick a time", Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    // attach to an onclick handler to show the time picker
    public void showTimePickerDialog(View v) {
        TimePickerFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    // once time set, adjust internal variables
    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        timePicked = true;

        // tell user to pick both time and date
        if (timePicked && datePicked) {
            tvDateTime.setText(calendar.getTime().toString());
        }
        if (!datePicked) {
            Snackbar.make(constraintLayout, "Please choose a date", Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    // for action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu, this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (miActionProgressItem != null) {
            hideProgressBar();
        }

        return super.onCreateOptionsMenu(menu);
    }

    // for progress bar
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Store instance of the menu item containing progress
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        if (miActionProgressItem != null) {
            hideProgressBar();
        }

        // return to finish
        return super.onPrepareOptionsMenu(menu);
    }

    // making the progress bar visible and invisible
    public void showProgressBar() {
        // Show progress item
        miActionProgressItem.setVisible(true);
    }

    public void hideProgressBar() {
        // Hide progress item
        miActionProgressItem.setVisible(false);
    }
}