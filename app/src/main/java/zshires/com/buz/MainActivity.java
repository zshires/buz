package zshires.com.buz;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

public class MainActivity extends ActionBarActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    public static MapFragment map;
    public GoogleMap gmap;
    private String SERVER_URL = "https://still-journey-7705.herokuapp.com/";
    private static final String TAG = "MainActivity";
    private final int RANGE = 60; //Distance in meters that controls how far you can see your friends
    private User currUser;
    Circle mapCircle;

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
        currUser = new User(0,0,0);
        currUser.addFriend((new User(43.068762, -89.408195, 1, "A")));
        currUser.addFriend((new User(43.068619, -89.408314, 2, "B")));
        currUser.addFriend((new User(43.068873, -89.408581, 3, "C")));
        currUser.addFriend((new User(43.068317, -89.408142, 4, "D")));

        /* Start Grabbing your current location */
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener ll = new myLocationListener();
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, ll);
        View view = getWindow().getDecorView().findViewById(android.R.id.content);

        /* Grab the map and initialize */
        map = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map.getMapAsync(this);
        gmap  = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        /* Swipe Listener */
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
            case R.id.action_messages:
                openMessages();
                return true;
            case R.id.action_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openMessages() {
        Intent intent = new Intent(this, MessagesActivity.class);
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
            String title = marker.getTitle();
            Toast toast = Toast.makeText(getApplicationContext(), "Buz " + title + "!", Toast.LENGTH_SHORT);
            toast.show();
        return false;
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
                /*
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
                */
                    //TODO check if needs to be hereUser me = new User(latitude,longitude,1);

                /* Populate the map with users friends*/
                    if (currUser != null && currUser.getFriends() != null){
                        for (User friend: currUser.getFriends()){
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

}
