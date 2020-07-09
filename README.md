Original App Design Project - README Template
===

# Event Planner
## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
An app that serves as a platform for students to organize in-person get-togethers with social distancing guidelines in mind.

### App Evaluation
[Evaluation of your app across the following attributes]

- **Category:** Social networking
- **Mobile:** Primarily a mobile experience
- **Story:** With the worldwide pandemic severely restricting the kinds of social activities people can participate in, there becomes a need for an organizational tool that can be used to remind people of important guidelines to follow should they decide to meet in-person. This app will serve as an event planning tool where people can register events with their friends and impose capacity caps, timing restrictions, or other requirements that the event organizer imposes.
- **Market:** Anyone interested in having in-person social experiences that observe social distancing guidelines
- **Habit:** Users can submit events with caps on capacity and submit their events to their friends' feeds along with location and scheduled times
- **Scope:** Anyone looking to connect with their friends in an in-person model while observing social distancing

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* User can make a new profile
* User can log in to their own profile
* Users can set certain aspects of their profile (full name, email, other social media handles, favorite activities)
* Users can see the events the specifically subscribed to 
* Users can take a picture of venue to set up as part of the event profile
* Users can create a new event
* Users can delete their event
* Users can take a photo of themselves and set as their profile picture
* Users can submit a location to their event and see where it is on a map using the Google Maps SDK
* Users can have "friends" on the app, send friend requests, and accept/decline friend requests
* Users can see the events their friends have posted on the app as part of a "timeline"
* The app will use a special algorithm to suggest potential friend matches to users


**Optional Nice-to-have Stories**

* Users can sync the events they plan to go to with an external calendar (e.g. gCal, Outlook Calendar, etc)
* Users can choose to mute events they've already seen but don't want deleted. Can view list of muted events
* Users can temporarily view list of events that already occurred
* Users can exchange details about the event in a comment section / group chat specific for the event
* Users can tag their profiles with "favorite activity tags" which will signal to users what kind of activities the user prefers (e.g. "tennis", "birdwatching", "hiking")


### 2. Screen Archetypes
* Log in or sign up screen
   * User can make a new profile
   * User can log in to their own profile
* User profile screen
   * Users can tailor certain aspects of their profile (full name, email, other social media handles, favorite activities)
   * Users can take a photo of themselves and set as their profile picture
   * Users can tag their profiles with "favorite activity tags" which will signal to users what kind of activities the user prefers (e.g. "tennis", "birdwatching", "hiking")
   * Users can see the events the specifically subscribed to 
* Screen showing a timeline or list of events posted by their friends that are ongoing or planned
   * Users can see the events their friends have posted on the app as part of a "timeline"
* Friends screen
   * Users can have "friends" on the app, send friend requests, and accept/decline friend requests
   * The app will use a special algorithm to suggest potential friend matches to users
* Create a new event screen
   * Users can create a new event
   * Users can take a picture of venue to set up as part of the event profile
* Event details screen
   * Users can delete their events
   * Users can submit a location to their event and see where it is on a map using the Google Maps SDK
* Map screen
    * Users can see a map with pins on where the events on their timelines are going to be


### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Events Timeline tab
* Friends tab
* Profile tab
* Map tab

**Flow Navigation** (Screen to Screen)

* Login screen
   * The first landing screen. Redirects to main screen below
* Main screen
   * Contains the timelines tab, friends tab, profile tab, and map tab
* Create a new events screen
   * Click button on main screen to get to New events screen
* Event details screen
   * Users click list of events on timeline and go to the Event details screen


## Wireframes
Open image in new tab for more detail:
<img src="YOUR_WIREFRAME_IMAGE_URL" width=600>![](https://i.imgur.com/pN1xi8Q.jpg)


### [BONUS] Digital Wireframes & Mockups

### [BONUS] Interactive Prototype

## Schema 

### Models

User 

| Property | Type     | Description |
| -------- | -------- | -------- |
| objectId | String | unique id for the user|
| username | String | For logging in|
| password | String | For authentication |
| profilePicture | File | Profile picture set by user |
| interests | String | Contains the interests of the user (hiking, bird-watching, etc) |
| subscriptions | Relation to Event | The list of events this user is going to go to |
| friends | Relation to User | The list of friends this user has |
| createdAt | DateTime | date when user is created (default field) |
| updatedAt | DateTime | date when user is last updated (default field) |


Event

| Property | Type     | Description |
| -------- | -------- | -------- |
| objectId | String  | unique id for the event |
| name | String | Title or name of the event |
| author | Pointer to User | The author of this event |
| date | DateTime | Date and time of the event |
| description | String | Description of the event |
| image | File | image caption by the user |
| location | GeoPoint | Location of where the event is taking place |
| restrictions | String | List of required restrictions (such as requirement to wear masks and/or gloves) set by user |
| subscribers | Relation to User | The people who said they were going to this event |
| createdAt | DateTime | date when post is created (default field) |
| updatedAt | DateTime | date when post is last updated (default field) |



### Networking
List of network requests by screen and basic snippets for each Parse network request

* Log in or sign up screen
    * (POST) Create a new user on sign up
    ```java
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Unable to sign in user");
                    return;
                }
                goMainActivity();
            }
        });
    ```
    * (POST) Send username and password to authenticate user
    ```java
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    // TODO better error handling
                    Log.e(TAG, "Issue with log in", e);
                    return;
                }
                goMainActivity();
            }
        });
    ```
* User profile screen
    * (GET) Query logged in user object
    ```java
        ParseUser.getCurrentUser()
    ```
    * (GET) Query list of posts the user subscribed to
    ```java
        // fetched user is currentUser
        fetchedUser.getEventRelation().getQuery().findInBackground(new FindCallback<Event>() {
        void done(List<Event> results, ParseException e) {
          if (e == null) {
            // results have all the Posts the current user liked.
          } else {
            // There was an error
          }
        }
    });
    ```
    * (PUT) Update user interests and profile picture
    ```java
        ParseUser user = ParseUser.getCurrentUser();
        user.setUsername();
        // etc, set other properties
    ```
    * (DELETE) Unsubscribe to user events
* Screen showing a timeline or list of events posted by their friends that are ongoing or planned
    * (GET) Query events that have been posted
    ```java
        ParseQuery<Post> query = ParseQuery.getQuery(Event.class);
        query.include(Event.KEY_USER); // to get author
        query.include(EVENT.KEY_SUBSCRIBERS); // to get subscribers
        // limit query to latest 20 items
        query.setLimit(20);
        // order posts by creation date (newest first)
        query.addDescendingOrder(Event.KEY_CREATED_KEY);
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> events, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting events", e);
                    return;
                }

                // save received posts to list and notify adapter of new data
                //posts.clear();
                allEvents.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });
    ```
    * (DELETE) Unsubscribe to user events
* Friends screen
    * (GET) Query user objects
    ```java
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereFindFriends(); // find this users' friends
        query.findInBackground(new FindCallback<ParseUser>() {
          public void done(List<ParseUser> objects, ParseException e) {
            if (e == null) {
                // The query was successful.
            } else {
                // Something went wrong.
            }
          }
        });
    ```
* Create a new event screen
    * (POST) Create a new event
    ```java
        Event event = new Event();
        event.setDescription(description);
        event.setImage(new ParseFile(photoFile));
        /* Set friends, location, etc*/
        event.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving", e);
                }
                Log.i(TAG, "Post was saved successfully");
            }
        });
    ```
* Event details screen
    * (GET) Query event object
    ```java
        ParseQuery<Event> query = ParseQuery.getQuery(Event.class);
        // Specify the object id
        query.getInBackground(objectId, new GetCallback<Event>() {
          public void done(TodoItem item, ParseException e) {
            if (e == null) {
              // Access data using the `get` methods for the object
              String body = item.getBody();
              // Access special values that are built-in to each object
              String objectId = item.getObjectId();
              Date updatedAt = item.getUpdatedAt();
              Date createdAt = item.getCreatedAt();
              // Do whatever you want with the data...
              Toast.makeText(TodoItemsActivity.this, body, Toast.LENGTH_SHORT).show();
            } else {
              // something went wrong
            }
          }
        });
    ```

SDK
* Map screen
    * (GET) Query event locations
    ```java
        ParseQuery<Post> query = ParseQuery.getQuery(Event.class);
        query.include(Event.KEY_USER); // to get author
        query.include(EVENT.KEY_SUBSCRIBERS); // to get subscribers
        // limit query to latest 20 items
        query.setLimit(20);
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> events, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting events", e);
                    return;
                }

                events.get(0).getLocation(); // get location of first event
            }
        });
    ```

- [OPTIONAL: List endpoints if using existing API such as Yelp]
