package com.example.eventplanner.adapters;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.net.ParseException;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.eventplanner.R;
import com.example.eventplanner.models.Comment;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;
import java.util.Locale;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    Context context;
    List<Comment> comments;

    public CommentsAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        comments.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Comment> list) {
        comments.addAll(list);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvText;
        TextView tvUsername;
        ImageView ivProfilePicture;
        ConstraintLayout container;
        TextView tvTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvText);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            ivProfilePicture = itemView.findViewById(R.id.ivProfilePicture);
            container = itemView.findViewById(R.id.container);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }

        public void bind(Comment comment) {
            String text = comment.getText();
            String author = comment.getAuthor().getUsername();

            tvUsername.setText(author);
            tvText.setText(text);
            tvTimestamp.setText(getRelativeTimeAgo(comment.getCreatedAt().getTime()));

            ParseFile image = comment.getAuthor().getParseFile("profilePicture");
            if (image != null) {
                // display image if it exists
                Glide.with(context).load(image.getUrl())
                        .transform(new CircleCrop())
                        .into(ivProfilePicture);
            } else {
                // else use blank placeholder
                ivProfilePicture.setImageResource(R.drawable.blankpfp);
            }
        }

        public String getRelativeTimeAgo(long timeInMillis) {

            String relativeDate = "No date found";
            try {
                relativeDate = DateUtils.getRelativeTimeSpanString(timeInMillis,
                        System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
                if (relativeDate.equals("In 0 seconds")) {
                    return "just now";
                }

                relativeDate = relativeDate.replaceAll(" hours", "h");
                relativeDate = relativeDate.replaceAll(" hour", "h");
                relativeDate = relativeDate.replaceAll(" minutes", "m");
                relativeDate = relativeDate.replaceAll(" minute", "m");
                relativeDate = relativeDate.replaceAll(" days", "d");
                relativeDate = relativeDate.replaceAll(" day", "d");
                relativeDate = relativeDate.replaceAll(" seconds", "s");
                relativeDate = relativeDate.replaceAll(" second", "s");
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return relativeDate;
        }
    }
}
