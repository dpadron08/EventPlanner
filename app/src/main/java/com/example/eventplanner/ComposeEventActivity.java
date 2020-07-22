package com.example.eventplanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import com.google.android.material.textfield.TextInputEditText;
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
import java.util.List;
import java.util.Locale;

public class ComposeEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private static final String TAG = "ComposeEventActivity";

    // PICK_PHOTO_CODE is a constant integer
    public final static int PICK_PHOTO_CODE = 1046;
    public static final int PLACE_PICKER_REQUEST = 1;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    String photoFileNameWithCamera = "photo2";

    ImageView ivEventImage;
    Button btnAddPicture;
    Button btnTakePicture;
    TextInputEditText etTitle;
    EditText etDescription;
    TextInputEditText etRestrictions;
    TextInputEditText etCapacity;
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
        btnTakePicture = findViewById(R.id.btnTakePicture);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etRestrictions = findViewById(R.id.etRestrictions);
        etCapacity = findViewById(R.id.etCapacity);
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
                        || etRestrictions.getText().toString().isEmpty() || etCapacity.getText().toString().isEmpty()) {
                    // prevent user from not adding a title, description, or restriction
                    //Toast.makeText(ComposeEventActivity.this, "Please add in all fields", Toast.LENGTH_SHORT).show();
                    Snackbar.make(constraintLayout, "Please fill in all fields", Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                // create the new event and return to old activity
                saveEventAndReturn(etTitle.getText().toString(), etDescription.getText().toString(), etRestrictions.getText().toString(),
                        ParseUser.getCurrentUser(), Integer.parseInt(etCapacity.getText().toString()));


            }
        });

        btnAddPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // launch gallery view
                onPickPhoto(view);
            }
        });
        btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCamera();
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
    private void saveEventAndReturn(String title, String description, String restrictions, ParseUser user, int capacity) {
        if (miActionProgressItem != null) {
            showProgressBar();
            Log.i(TAG, "save event and return");
        }
        final Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setRestrictions(restrictions);
        event.setAuthor(user);
        event.setCapacity(capacity);

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
        event.getRelation("subscribers").add(ParseUser.getCurrentUser());
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
                overridePendingTransition(R.anim.no_animation, R.anim.slide_down_back);
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
            String locationStr = "Location: " + getAddress(eventLocation);
            tvLocationDisplay.setText(locationStr);
        }

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // RESIZE BITMAP, see section below
                Bitmap bitmapScaled = scaleToFitHeight(takenImage, 200);

                /* Without resizing
                // Load the taken image into a preview
                ivPostImage.setImageBitmap(takenImage);
                 */

                // Write the smaller bitmap back to disk
                // Configure byte output stream
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                // Compress the image further
                bitmapScaled.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
                // Create a new file for the resized bitmap (`getPhotoFileUri` defined above)
                File resizedFile = getPhotoFileUri(photoFileNameWithCamera + "_resized");
                try {
                    resizedFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(resizedFile);
                    // Write the bytes of the bitmap to file
                    fos.write(bytes.toByteArray());
                    fos.close();
                } catch (Exception e) {
                    Log.e(TAG, "Problem writing to disk");
                }

                ivEventImage.setImageBitmap(bitmapScaled);
                photoFile = resizedFile;

            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
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
    private String getAddress(LatLng location)  {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
            return "No address found";
        }
        return addresses.get(0).getAddressLine(0);
    }

    /* Launching camera */
    private void launchCamera() {
        photoFile = null;
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileNameWithCamera);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(this, "com.example.fileproviderAnother", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(this.getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    private File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);
        return file;
    }

    // Scale and maintain aspect ratio given a desired height
    // BitmapScaler.scaleToFitHeight(bitmap, 100);
    public static Bitmap scaleToFitHeight(Bitmap b, int height)
    {
        float factor = height / (float) b.getHeight();
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factor), height, true);
    }
}