package com.example.eventplanner.adapters;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        private static final String TAG = "EventsAdapterViewHolder";
        TextView tvTitle;
        //TextView tvDescription;
        TextView tvAuthor;
        TextView tvLocation;
        TextView tvDate;
        //TextView tvRestrictions;
        ImageView ivImage;
        ConstraintLayout container;
        MaterialCardView cardView;

        TextView tvCapacity;

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
            cardView = itemView.findViewById(R.id.cardView);
            tvCapacity = itemView.findViewById(R.id.tvCapacity);
        }

        public void bind(final Event event) {
            String title = event.getTitle();
            tvTitle.setText(title);
            //tvDescription.setText(event.getDescription());

            String author = "By: " + event.getAuthor().getUsername();
            tvAuthor.setText(author);

            // display Location if it exists
            if (event.getLocation() != null) {
                String locationStr = "Where: " + getAddress(event.getLocation());
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

            container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    toggleSubscription(event);
                    return true;
                }
            });

            // draw colored borders depending if users are subscribed to event
            styleHighlightAndCapacity(event);
        }

        private String getAddress(ParseGeoPoint location)  {
            Geocoder geocoder;
            List<Address> addresses = null;
            geocoder = new Geocoder(context, Locale.getDefault());
            try {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
                return "No address found";
            }
            return addresses.get(0).getAddressLine(0);
        }

        private void toggleSubscription(final Event event) {
            // change the subscription status of this event for the user
            ParseRelation<ParseUser> relation = event.getRelation("subscribers");
            ParseQuery<ParseUser> query = relation.getQuery();
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(final List<ParseUser> objects, ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Unable to retrieve subscribers for event when toggling subscription", e);
                        return;
                    }
                    int population = objects.size();
                    boolean isUserSubscribed = false;
                    for (ParseUser subscriber : objects) {
                        if (subscriber.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                            isUserSubscribed = true;
                            break;
                        }
                    }
                    if (isUserSubscribed) {
                        // unsubscribe and change border color
                        event.getRelation("subscribers").remove(ParseUser.getCurrentUser());
                        ParseUser.getCurrentUser().getRelation("subscriptions").remove(event);
                        cardView.setStrokeWidth(0);
                        Snackbar.make(container, "Unsubscribed!", Snackbar.LENGTH_SHORT)
                                .show();
                        population--;

                    } else {
                        // subscribe and change border color
                        event.getRelation("subscribers").add(ParseUser.getCurrentUser());
                        ParseUser.getCurrentUser().getRelation("subscriptions").add(event);
                        cardView.setStrokeWidth(4);
                        ParseUser.getCurrentUser().getRelation("subscriptions").describeContents();
                        Snackbar.make(container, "Subscribed!", Snackbar.LENGTH_SHORT)
                                .show();
                        population++;
                    }
                    final int finalPopulation = population;
                    //possible problem with race conditions
                    event.setSubscriberCount(finalPopulation);
                    event.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "done: Unable to add to event's subsscribers", e);
                                return;
                            }
                            if (event.getCapacity() > 0) {
                                String capacity = finalPopulation +"/" +event.getCapacity();
                                tvCapacity.setText(capacity);
                            }
                            ParseUser.getCurrentUser().saveInBackground();
                        }
                    });


                }
            });
        }

        public void styleHighlightAndCapacity(final Event event) {
            ParseRelation<ParseUser> relation = event.getRelation("subscribers");
            ParseQuery<ParseUser> query = relation.getQuery();

            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> objects, ParseException e) {
                    // once subscribers for event are found,

                    /*
                    for (ParseUser u : objects) {
                        Log.i(TAG, "Username subscribed: "+ u.getUsername());
                    }
                     */
                    cardView.setStrokeColor(context.getResources().getColor(R.color.colorWhite));
                    for (ParseUser subscriber : objects) {
                        if (subscriber.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                            cardView.setStrokeColor(context.getResources().getColor(R.color.colorSubscribed));
                            break;
                        }
                    }

                    String capacity;
                    if (event.getCapacity() > 0) {
                        capacity = objects.size() + "/" + event.getCapacity();
                    } else {
                        capacity = "No capacity";
                    }
                    tvCapacity.setText(capacity);

                }
            });
        }
    }
}
