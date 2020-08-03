package com.example.eventplanner.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventplanner.EventDetailsActivity;
import com.example.eventplanner.MainActivity;
import com.example.eventplanner.R;
import com.example.eventplanner.models.Event;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder>{

    Context context;
    List<Event> events;
    int lastSelectedPosition = -1;

    public EventsAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
    }

    public Event getEvent(int position) {
        return events.get(position);
    }
    public int getLastPosition() {
        return lastSelectedPosition;
    }
    public void setLastPosition(int position) {
        lastSelectedPosition = position;
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
        holder.bind(event, position);
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
        public static final int TITLE_MAX_CHARACTER_LIMIT = 30;
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

        public void bind(final Event event, int position) {
            String title = event.getTitle();
            if (title.length() > TITLE_MAX_CHARACTER_LIMIT) {
                title = title.substring(0, 28) + "...";
            }
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
                SimpleDateFormat targetFormat = new SimpleDateFormat("MM/dd/yyyy h:mm a", Locale.ENGLISH);
                Date date = event.getDate();
                String formattedDate = targetFormat.format(date);
                tvDate.setText(formattedDate);
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

            View.OnTouchListener onTouchListener= new View.OnTouchListener() {
                private GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        goEventDetailsActivity(event, position);
                        return true;
                    }

                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        Log.i(TAG, "onDoubleTap: ");
                        toggleSubscription(event);
                        // get most recent information of event
                        event.fetchInBackground(new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject object, ParseException e) {
                                goEventDetailsActivity((Event)object, position);
                            }
                        });

                        return true;
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        super.onLongPress(e);
                        toggleSubscription(event);
                    }
                });

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    container.performClick();
                    return gestureDetector.onTouchEvent(motionEvent);
                }
            };
            container.setOnTouchListener(onTouchListener);

            // draw colored borders depending if users are subscribed to event
            styleHighlightAndCapacity(event);
        }

        private void goEventDetailsActivity(Event event, int position) {
            Intent intent = new Intent(context, EventDetailsActivity.class);
            intent.putExtra("event", Parcels.wrap(event));
            intent.putExtra("position", position);
            context.startActivity(intent);
            lastSelectedPosition = position;
            //((MainActivity) context).startActivityForResult(intent, 52);
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
                        cardView.setStrokeColor(context.getResources().getColor(R.color.colorWhite));
                        Snackbar.make(container, "Unsubscribed!", Snackbar.LENGTH_SHORT)
                                .show();
                        population--;

                    } else {
                        // subscribe and change border color
                        event.getRelation("subscribers").add(ParseUser.getCurrentUser());
                        ParseUser.getCurrentUser().getRelation("subscriptions").add(event);
                        cardView.setStrokeColor(context.getResources().getColor(R.color.colorSubscribed));
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
