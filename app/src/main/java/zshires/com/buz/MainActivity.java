package zshires.com.buz;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

import java.util.ArrayList;


import com.google.gson.*;
import net.callumtaylor.asynchttp.AsyncHttpClient;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;
import org.apache.http.Header;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.json.JSONObject;

import java.util.List;


public class MainActivity extends ActionBarActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {
    public static MapFragment map;
    public GoogleMap gmap;
    private String SERVER_URL = "https://still-journey-7705.herokuapp.com/";
    private static final String TAG = "MainActivity";
    private final int RANGE = 60; //Distance in meters that controls how far you can see your friends
    private static User currUser;
    Circle mapCircle;

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

        currUser = (User) savedInstanceState.getSerializable("currUser");
//        currUser = new User(0,0,0,"TempUserName");
//        currUser.addFriend((new User(43.068762, -89.408195, 1, "Zak Shires")));
//        currUser.addFriend((new User(43.068619, -89.408314, 2, "Mike Fix")));
//        currUser.addFriend((new User(43.068873, -89.408581, 3, "Charlie")));
//        currUser.addFriend((new User(43.068317, -89.408142, 4, "Dave")));

        /* Start Grabbing your current location */
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener ll = new myLocationListener();
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, ll);
        View view = getWindow().getDecorView().findViewById(android.R.id.content);

        /* Grab the map and initialize */
        map = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map.getMapAsync(this);
        gmap  = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();


        /* Swipe Listener
        view.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                openMessages();
            }
        });

        /* Initialize our user*/
        /* Populate friends from backend
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        currUser = new User(latitude,longitude, prefs.getInt("idPref", 1));
        getFriends(currUser, new BackendCallback() {
            @Override
            public void onRequestCompleted(Object result) {
                currUser = (User) result;
            }

            @Override
            public void onRequestFailed(String message) {
                Log.d("LoadUserError", message);
            }
        });
        */
    }

    private void getFriends(User me , final BackendCallback callback) {
        /* Backend call to get friends
        AsyncHttpClient client = new AsyncHttpClient(SERVER_URL);

        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Accept", "application/json"));
        headers.add(new BasicHeader("Content-Type", "application/json"));
        //headers.add(new BasicHeader("X-USER-ID", Integer.toString(user.backendId)));
        //headers.add(new BasicHeader("X-AUTHENTICATION-TOKEN", user.authToken));

        client.get("users/" + me.getID(), null, headers, new JsonResponseHandler() {
            @Override
            public void onSuccess() {
                JsonObject result = getContent().getAsJsonObject();
                //JsonObject array = parser.parse(inputLine).getAsJsonArray();
                //Sugar and GSON don't play nice, need to ensure the ID property is mapped correctly

               // for (JsonElement element: result) {
               //     JsonObject casted = element.getAsJsonObject();
               //     casted.addProperty("backendId", casted.get("id").toString());
               //     casted.remove("id");
               // }

                Log.d(TAG, "Load returned: " + result);
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
                User user = gson.fromJson(result, User.class);
                Log.d("User:" ,user.toString());

                callback.onRequestCompleted(user);
            }

            @Override
            public void onFailure() {
                callback.onRequestFailed(handleFailure(getContent()));
            }
        });
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

    public void updateLocation(User me, final BackendCallback callback){
        AsyncHttpClient client = new AsyncHttpClient(SERVER_URL);
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Accept", "application/json"));
        headers.add(new BasicHeader("Content-Type", "application/json"));
        //headers.add(new BasicHeader("X-USER-ID", Integer.toString(user.backendId)));
        //headers.add(new BasicHeader("X-AUTHENTICATION-TOKEN", user.authToken));
        StringEntity jsonParams = null;
        try{
            JSONObject json = new JSONObject();
            json.put("latitude", currUser.getLatitude());
            json.put("longitude", currUser.getLongitude());
            jsonParams = new StringEntity(json.toString());
        }catch (Exception e){
            Log.d("Error:" ,"Exception thrown in updateLocation");
        }
        client.put("users/" + me.getID() + ".json", jsonParams, null, new JsonResponseHandler() {
            @Override
            public void onSuccess() {
                JsonObject result = getContent().getAsJsonObject();
                //JsonObject array = parser.parse(inputLine).getAsJsonArray();
                //Sugar and GSON don't play nice, need to ensure the ID property is mapped correctly
                /*
                for (JsonElement element: result) {
                    JsonObject casted = element.getAsJsonObject();
                    casted.addProperty("backendId", casted.get("id").toString());
                    casted.remove("id");
                }*/

                Log.d(TAG, "Load returned: " + result);

                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
                User user = gson.fromJson(result, User.class);
                Log.d("User:" ,user.toString());

                callback.onRequestCompleted(user);
            }

            @Override
            public void onFailure() {
                callback.onRequestFailed(handleFailure(getContent()));
            }
        });

    }


    class myLocationListener implements LocationListener {
        ArrayList<Marker> dummyMarkers = new ArrayList<Marker>();

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                /* Update coordinates for our user */
                currUser.setLatitude(location.getLatitude());
                currUser.setLongitude(location.getLongitude());
                LatLng loc = new LatLng(currUser.getLatitude(), currUser.getLongitude());
                gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 18)); // have the map follow the user as they move
                if(mapCircle != null){
                    mapCircle.remove(); // clear the old circles on the map
                }
                mapCircle = gmap.addCircle(new CircleOptions().center(loc).radius(RANGE).strokeColor(Color.YELLOW).strokeWidth(4).visible(true));
               /* try{
                if(initialZoom) {
                    GoogleMap gmap2 = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
                    gmap2.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 18));
                    gmap2.animateCamera(CameraUpdateFactory.zoomIn());
                    initialZoom = false;
                } } catch (Exception e){}
                */
                for(Marker dummy : dummyMarkers){
                    if(dummy != null) {
                        dummy.remove(); //remove all the old markers on the map
                    }
                }
                dummyMarkers.clear(); // get rid of these markers. we will add the updated ones later
                //Push to server "me"


                //TODO this was commmented
                updateLocation(currUser, new BackendCallback() {
                    @Override
                    public void onRequestCompleted(Object result) {
                        Log.d("Put", "Success");
                    }

                    @Override
                    public void onRequestFailed(String message) {
                        Log.e("Error", "Put failure");
                    }
                });
                getFriends(currUser, new BackendCallback() {
                    @Override
                    public void onRequestCompleted(Object result) {
                        currUser = (User) result;
                    }

                    @Override
                    public void onRequestFailed(String message) {
                        Log.d("LoadUserError", message);
                    }
                });

                    //TODO check if needs to be hereUser me = new User(latitude,longitude,1);

                /* Populate the map with users friends*/
                    if (currUser != null && currUser.getFriends() != null){
                        for (User friend : currUser.getFriends()){
                            if (friend.isInRange(currUser,RANGE)) {
                                dummyMarkers.add(addMapMarker(gmap,friend.getLatitude(),friend.getLongitude(), friend.getName()));
                            }
                        }
                    }
                    else
                        Log.e("Null Error", "currUser is null or currUser.Friends is null");

            }
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
