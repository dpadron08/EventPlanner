package com.example.eventplanner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventplanner.R;
import com.example.eventplanner.models.Event;
import com.parse.ParseFile;

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
        TextView tvDescription;
        TextView tvAuthor;
        TextView tvLocation;
        TextView tvDate;
        TextView tvRestrictions;
        ImageView ivImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvRestrictions = itemView.findViewById(R.id.tvRestrictions);
            ivImage = itemView.findViewById(R.id.ivImage);

        }

        public void bind(Event event) {
            tvTitle.setText(event.getTitle());
            tvDescription.setText(event.getDescription());
            tvAuthor.setText(event.getAuthor().getUsername());

            tvLocation.setText(event.getLocation().toString()); // getting geo point

            String date = ((Date) event.getDate()).toString();
            tvDate.setText(date); // get Datetime
            tvRestrictions.setText(event.getRestrictions());

            ParseFile image = event.getImage();
            if (image != null) {
                Glide.with(context).load(image.getUrl()).into(ivImage);
            }
        }
    }
}
