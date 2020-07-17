package com.example.eventplanner.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventplanner.EventDetailsActivity;
import com.example.eventplanner.R;
import com.example.eventplanner.models.Event;
import com.parse.ParseFile;

import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.util.Date;
import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder>{

    Context context;
    List<Event> events;

    public EventsAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void clear() {
        events.clear();
        notifyDataSetChanged();
    }
    public void addAll(List<Event> list) {
        events.addAll(list);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        //TextView tvDescription;
        TextView tvAuthor;
        TextView tvLocation;
        TextView tvDate;
        //TextView tvRestrictions;
        ImageView ivImage;
        ConstraintLayout container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            //tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvDate);
            //tvRestrictions = itemView.findViewById(R.id.tvRestrictions);
            ivImage = itemView.findViewById(R.id.ivImage);
            container = itemView.findViewById(R.id.container);

        }

        public void bind(final Event event) {
            String title = event.getTitle();
            tvTitle.setText(title);
            //tvDescription.setText(event.getDescription());

            String author = "By: " + event.getAuthor().getUsername();
            tvAuthor.setText(author);

            // display Location if it exists
            if (event.getLocation() != null) {
                String locationStr = "Where: " + event.getLocation().toString();
                tvLocation.setText(locationStr);
            }

            // display datetime if it exists
            if (event.getDate() != null) {
                String date = "When: " + ((Date) event.getDate()).toString();
                tvDate.setText(date); // get Datetime
            }

            //tvRestrictions.setText(event.getRestrictions());

            ParseFile image = event.getImage();
            if (image != null) {
                // display image if it exists
                Glide.with(context).load(image.getUrl()).into(ivImage);
            } else {
                // else use blank placeholder
                ivImage.setImageResource(R.drawable.blankpfp);
            }

            // when user clicks on an event, this takes them to details view
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, EventDetailsActivity.class);
                    intent.putExtra("event", Parcels.wrap(event));
                    context.startActivity(intent);
                }
            });

        }
    }
}
