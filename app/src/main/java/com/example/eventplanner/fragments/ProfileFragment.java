package com.example.eventplanner.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.eventplanner.LoginActivity;
import com.example.eventplanner.R;
import com.example.eventplanner.adapters.EventsAdapter;
import com.example.eventplanner.models.Event;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final String TAG = "ProfileFragment";
    // PICK_PHOTO_CODE is a constant integer
    public final static int PICK_PHOTO_CODE = 1046;
    File photoFile = null;

    // for recycler view list of events
    RecyclerView rvEvents;
    protected EventsAdapter adapter;
    protected List<Event> allEvents;

    // other profile elements
    ImageView ivProfilePic;
    TextView tvUsername;
    TextView tvInterests;
    Button btnAddProfilePicture;
    Button btnLogout;

    // for the progress loading action item
    MenuItem miActionProgressItem;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    // triggered soon after onCreateView
    // any view setup should occur here. View lookups, attaching view listeners
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivProfilePic = view.findViewById(R.id.ivProfilePicture);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvInterests = view.findViewById(R.id.tvInterests);
        btnAddProfilePicture = view.findViewById(R.id.btnAddProfilePicture);
        btnLogout = view.findViewById(R.id.btnLogout);
        queryUserProfile(); // get user attributes and populate the views with data


        rvEvents = view.findViewById(R.id.rvEvents);
        allEvents = new ArrayList<>();
        adapter = new EventsAdapter(getContext(), allEvents);
        rvEvents.setAdapter(adapter);
        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        querySubscribedEvents();

        btnAddProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPickPhoto(view);
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
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();


    }

    private void onPickPhoto(View view) {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
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
                ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }


    // get user profile attributes
    private void queryUserProfile() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        currentUser.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e != null) {
                    Log.i(TAG, "Failed to query current user");
                    return;
                }
                // once user is found, populate the views with user data
                tvUsername.setText(((ParseUser)object).getUsername());
                String interests = "Interests: " + ((ParseUser)object).getString("interests");
                tvInterests.setText(interests);
                ParseFile image = ((ParseUser)object).getParseFile("profilePicture");
                if (image != null) {
                    Glide.with(getContext()).load(image.getUrl()).into(ivProfilePic);
                } else {
                    ivProfilePic.setImageResource(R.drawable.blankpfp);
                }
            }
        });


    }

    // get the events the user subscribed to
    private void querySubscribedEvents() {
        if (miActionProgressItem != null) {
            showProgressBar();
        }
        adapter.clear();
        ParseRelation<Event> relation = ParseUser.getCurrentUser().getRelation("subscriptions");
        ParseQuery<Event> query = relation.getQuery();
        query.include("author");
        query.addDescendingOrder(Event.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {
                if (miActionProgressItem != null) {
                    hideProgressBar();
                }
                if (e != null) {
                    Log.e(TAG, "Failed to query subscribed to events");
                    return;
                }
                allEvents.addAll(objects);
                adapter.notifyDataSetChanged();;

            }
        });
    }

    // returning from getting a picture for profile
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();

            // Load the image located at photoUri into selectedImage
            Bitmap selectedImage = loadFromUri(photoUri);

            // convert the bitmap to file format
            photoFile = new File(getContext().getCacheDir(), "photo");
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
            ivProfilePic.setImageBitmap(selectedImage);
            saveProfilePic();

        }

    }

    private void saveProfilePic() {
        ParseUser user = ParseUser.getCurrentUser();
        user.put("profilePicture", new ParseFile(photoFile));
        user.saveInBackground();
        Toast.makeText(getContext(), "PFP saved!", Toast.LENGTH_SHORT).show();
    }

    // for inflating our action bar
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu, this adds items to the action bar if it is present
        inflater.inflate(R.menu.menu_main, menu);
    }


    // for progress bar
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // Store instance of the menu item containing progress
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        Log.i(TAG, "Get here?");

        super.onPrepareOptionsMenu(menu);
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