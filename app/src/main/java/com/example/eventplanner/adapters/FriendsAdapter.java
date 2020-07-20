package com.example.eventplanner.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventplanner.FriendDetailsActivity;
import com.example.eventplanner.R;
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

        // for being able to click on a friend
        ConstraintLayout container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvInterests = itemView.findViewById(R.id.tvInterests);
            ivProfilePicture = itemView.findViewById(R.id.ivProfilePicture);
            container = itemView.findViewById(R.id.container);
        }

        public void bind(final ParseUser user) {
            tvUsername.setText(user.getUsername());

            String interests;
            if (user.getString("interests") != null) {
                interests = "Interests: " + user.getString("interests");
            } else {
                interests = "None";
            }
            tvInterests.setText(interests);

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
                    context.startActivity(intent);
                }
            });
        }

    }
}
