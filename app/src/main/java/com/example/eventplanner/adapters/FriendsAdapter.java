package com.example.eventplanner.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventplanner.FriendDetailsActivity;
import com.example.eventplanner.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    Context context;
    List<ParseUser> friends;

    public FriendsAdapter(Context context, List<ParseUser> friends) {
        this.context = context;
        this.friends = friends;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParseUser user = friends.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        friends.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<ParseUser> list) {
        friends.addAll(list);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private static final String TAG = "ViewHolder";
        TextView tvUsername;
        TextView tvInterests;
        ImageView ivProfilePicture;
        ChipGroup cgInterests;

        // for being able to click on a friend
        ConstraintLayout container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvInterests = itemView.findViewById(R.id.tvInterests);
            ivProfilePicture = itemView.findViewById(R.id.ivProfilePicture);
            container = itemView.findViewById(R.id.container);
            cgInterests = itemView.findViewById(R.id.cgInterests);
        }

        public void bind(final ParseUser user) {
            tvUsername.setText(user.getUsername());

            String interests = user.getString("interests");
            if (interests != null) {
                populateChips(interests);
                tvInterests.setVisibility(View.GONE);
                //interests = "Interests: " + user.getString("interests");
            } else {
                interests = "None";
                tvInterests.setText(interests);
            }

            ParseFile image = user.getParseFile("profilePicture");
            if (image != null) {
                Glide.with(context).load(image.getUrl()).into(ivProfilePicture);
            } else {
                // set blank profile picture if friend has no profile pic
                ivProfilePicture.setImageResource(R.drawable.blankpfp);
            }
            
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(TAG, "onClick: Clicked friend");

                    // launch friend details activity
                    Intent intent = new Intent(context, FriendDetailsActivity.class);
                    intent.putExtra("user", Parcels.wrap(user));
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                            (Activity)context, itemView, context.getString(R.string.friend_transition_animation));
                    context.startActivity(intent, options.toBundle());
                }
            });
        }

        private void populateChips(String interests) {
            if (cgInterests.getChildCount() > 0)
                return;

            String[] interestsArr = interests.split(",");
            for (String interest : interestsArr) {
                Chip chip = new Chip(context);
                chip.setText(interest);
                int[] colors = {Color.RED, Color.BLACK, Color.GRAY, Color.GREEN, Color.CYAN};
                //chip.setBackgroundColor(colors[(int)(Math.random()*5)]);
                chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary)));
                cgInterests.addView(chip);

            }
        }

    }
}
