package zshires.com.buz;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.ArrayList;


import com.google.gson.*;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import net.callumtaylor.asynchttp.AsyncHttpClient;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;
import org.apache.http.Header;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;


public class MainActivity extends ActionBarActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {
    public static MapFragment map;
    public GoogleMap gmap;
    private String SERVER_URL = "https://still-journey-7705.herokuapp.com/";
    private static final String TAG = "MainActivity";
    private final int RANGE = 60; //Distance in meters that controls how far you can see your friends
    private static User currUser;
    Circle mapCircle;
    public static String password;
    public ParseUser currDBUser;
    public HashMap<String, User> allUsers = new HashMap<String, User>();
    public ArrayList<String> myfriendsNames = new ArrayList<String>();

    // Allows us to notify the user that something happened in the background
    NotificationManager notificationManager;

    // Used to track notifications
    int notifID = 33;
    // Used to track if notification is active in the task bar
    boolean isNotificActive = false;


    @Override
    public void onInfoWindowClick(Marker marker) {
        String title = marker.getTitle();
        //find the user you clicked on in the friends list
        for(User u : currUser.getFriends()){
            if(u.getName().equals(title)){
                //Buz! that user
                //TODO: this should be a notification on the receivers phone, not yours.
                //The notification should take you to the map. it currently takes you to some random page
                showNotification();
                break;
            }
        }
        Toast toast = Toast.makeText(getApplicationContext(), "You Buzed " + title + "!", Toast.LENGTH_SHORT);
        toast.show();
    }

    public void showNotification() {

        // Builds a notification
        NotificationCompat.Builder notificBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Message")
                .setContentText("New Message")
                .setTicker("Alert New Message")
                .setSmallIcon(R.drawable.ic_launcher);

        // Define that we have the intention of opening MoreInfoNotification
        Intent moreInfoIntent = new Intent(this, MoreInfoNotification.class);

        // Used to stack tasks across activites so we go to the proper place when back is clicked
        TaskStackBuilder tStackBuilder = TaskStackBuilder.create(this);

        // Add all parents of this activity to the stack
        tStackBuilder.addParentStack(MoreInfoNotification.class);

        // Add our new Intent to the stack
        tStackBuilder.addNextIntent(moreInfoIntent);

        // Define an Intent and an action to perform with it by another application
        // FLAG_UPDATE_CURRENT : If the intent exists keep it but update it if needed
        PendingIntent pendingIntent = tStackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Defines the Intent to fire when the notification is clicked
        notificBuilder.setContentIntent(pendingIntent);

        // Gets a NotificationManager which is used to notify the user of the background event
        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Post the notification
        notificationManager.notify(notifID, notificBuilder.build());

        // Used so that we can't stop a notification that has already been stopped
        isNotificActive = true;


    }


    public interface BackendCallback {
        public void onRequestCompleted(Object result);
        public void onRequestFailed(String message);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currDBUser = ParseUser.getCurrentUser();
        Intent i = getIntent();
        password = i.getStringExtra("password");
        currUser = new User(0,0,0,currDBUser.getString("username"));

        /* Start Grabbing your current location */
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener ll = new myLocationListener();
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, ll);
        View view = getWindow().getDecorView().findViewById(android.R.id.content);

        /* Grab the map and initialize */
        map = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map.getMapAsync(this);
        gmap  = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

            updateClientUserMap();

    }

    public void updateClientUserMap(){


        ParseQuery<ParseObject> query = ParseQuery.getQuery("Friends");
        query.whereEqualTo("username1", currUser.getName());
        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                if (e == null) {
                   for(ParseObject p : parseObjects){
                      myfriendsNames.add(p.getString("username2"));
                   }
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });


        /*
        try {
        //allUsers.clear();
        //look in our user table to see if they exist
        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                try {
                    if (e == null) {
                        for (ParseObject u : parseObjects) {
                            boolean contains = false;
                            String uName = u.getString("username").trim();
                            Double uLat = Double.parseDouble(u.getString("lat").trim());
                            Double uLon = Double.parseDouble(u.getString("lon").trim());
                            User use = new User(uLat, uLon, 0, uName);
                            //check for containment
                            for (String x : allUsers.keySet()) {
                                if (x.equals(uName)) {
                                    contains = true;
                                    break;
                                }
                            }
                            if (!contains) allUsers.put(uName, use);
                        }
                    }
                } catch (Exception ex) {
                    Log.e("update", "update client fail");
                }
            }
        });
        } catch (Exception e) {Log.e("update","update client fail");}

        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_friends:
                openFriends();
                return true;
            case R.id.action_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openFriends() {
        Intent intent = new Intent(this, FriendsActivity.class);
        startActivity(intent);
    }

    public void openSettings(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.setOnMarkerClickListener(this);
        map.setOnInfoWindowClickListener(this);
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.info_window, null);
                TextView tv = (TextView) view.findViewById(R.id.infoWindowInformation);
                tv.setText("Buz " + marker.getTitle());
                Typeface face = Typeface.createFromAsset(getAssets(),"fonts/Aventura-Bold.otf");
                tv.setTypeface(face);
                return view;
            }
        });
        map.setMyLocationEnabled(true);
        map.getUiSettings().setScrollGesturesEnabled(false);
        map.getUiSettings().setZoomGesturesEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
    }

    public Marker addMapMarker(GoogleMap map, double lat, double lon, String title) {
        return map.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title(title).icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarker)));
    }

    public void startLocationService() {
        Intent i = new Intent(this, LocationService.class);
        startService(i);
    }

    public void stopLocationService() {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
            marker.showInfoWindow();
        return true;
    }


    class myLocationListener implements LocationListener {
        ArrayList<Marker> dummyMarkers = new ArrayList<Marker>();

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                /* Update coordinates for our user */
                currUser.setLatitude(location.getLatitude());
                currUser.setLongitude(location.getLongitude());

                //update my location on backend
                ParseObject point = ParseObject.createWithoutData("_User", currDBUser.getObjectId());
                point.put("lat", Double.toString(currUser.getLatitude()));
                point.put("lon", Double.toString(currUser.getLongitude()));
                point.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(com.parse.ParseException e) {
                        if (e == null) {
                            //updated location on server
                        } else {
                            Log.e("LocationUpdateonserver", "Fail");
                        }
                    }
                });

                //map stuff
                LatLng loc = new LatLng(currUser.getLatitude(), currUser.getLongitude());
                gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 18)); // have the map follow the user as they move
                if (mapCircle != null) {
                    mapCircle.remove(); // clear the old circles on the map
                }
                mapCircle = gmap.addCircle(new CircleOptions().center(loc).radius(RANGE).strokeColor(Color.YELLOW).strokeWidth(4).visible(true));

                //marker logic
                for (Marker dummy : dummyMarkers) {
                    if (dummy != null) {
                        dummy.remove(); //remove all the old markers on the map
                    }
                }
                dummyMarkers.clear(); // get rid of these markers. we will add the updated ones later


                for(String friendName : myfriendsNames) {
                    //grab the coords from the DB and add a new friend to currUser
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
                    query.whereEqualTo("username", friendName);
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                            if (e == null) {
                                for(ParseObject p : parseObjects) {
                                    String uName = p.getString("username").trim();
                                    Double uLat = Double.parseDouble(p.getString("lat").trim());
                                    Double uLon = Double.parseDouble(p.getString("lon").trim());
                                    User use = new User(uLat, uLon, 0, uName);
                                    currUser.addFriend(use);
                                }
                            } else {
                                Log.d("Error", "Error: " + e.getMessage());
                            }
                        }
                    });

                }

                /* Populate the map with users friends*/
                Log.e("Debug", "trying to populate");
                    if (currUser != null && currUser.getFriends() != null) {
                        Log.e("Debug", "populate 2");
                        for (User friend : currUser.getFriends()) {
                            Log.e("Debug", "going throught the friends");
                            if (friend != null && friend.isInRange(currUser, RANGE)) {
                                Log.e("Debug", "adding markers to map");
                                dummyMarkers.add(addMapMarker(gmap, friend.getLatitude(), friend.getLongitude(), friend.getName()));
                            }
                        }
                    } else
                        Log.e("Null Error", "currUser is null or currUser.Friends is null");
                }

                updateClientUserMap();
               }



        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "Please enable your GPS",
                    Toast.LENGTH_LONG).show();
        }
    }

    /* Convenience methods */
    /**
     * Convenience method for parsing server error responses, since most of the handling is similar.
     * @param response the raw response from a server failure.
     * @return a string with an appropriate error message.
     */
    private static String handleFailure(JsonElement response) {
        String errorMessage = "unknown server error";

        if (response == null)
            return errorMessage;

        JsonObject result = response.getAsJsonObject();

        //Server will return all error messages (except in the case of a crash) as a single level JSON
        //with one key called "message". This is a convention for this server.
        try {
            errorMessage = result.get("message").toString();
        }
        catch (Exception e) {
            Log.d(TAG, "Unable to parse server error message");
        }

        return errorMessage;
    }

    public static User getCurrUser(){
        return currUser;
    }

}
