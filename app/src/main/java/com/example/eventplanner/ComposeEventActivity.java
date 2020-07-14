package com.example.eventplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.eventplanner.models.Event;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ComposeEventActivity extends AppCompatActivity {

    private static final String TAG = "ComposeEventActivity";

    // PICK_PHOTO_CODE is a constant integer
    public final static int PICK_PHOTO_CODE = 1046;

    ImageView ivEventImage;
    Button btnAddPicture;
    EditText etTitle;
    EditText etDescription;
    EditText etRestrictions;
    Button btnSubmit;

    private File photoFile = null;

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

        btnAddPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // launch gallery view
                onPickPhoto(view);
            }
        });
    }

    private void saveEventAndReturn(String title, String description, String restrictions, ParseUser user) {
        final Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setRestrictions(restrictions);
        event.setAuthor(user);
        if (photoFile != null) {
            event.setImage(new ParseFile(photoFile));
        }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
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

    }
}