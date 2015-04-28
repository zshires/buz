package zshires.com.buz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.google.android.gms.gcm.GoogleCloudMessaging;
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

import java.io.IOException;
import java.util.ArrayList;


import com.google.gson.*;
import net.callumtaylor.asynchttp.AsyncHttpClient;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;
import org.apache.http.Header;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends ActionBarActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    public static MapFragment map;
    public GoogleMap gmap;
    private String SERVER_URL = "https://still-journey-7705.herokuapp.com/";
    private static final String TAG = "MainActivity";
    private final int RANGE = 60; //Distance in meters that controls how far you can see your friends
    private User currUser;
    Circle mapCircle;

    /*GCM STUFF*/
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    String SENDER_ID = getString(R.string.gcmSenderId);
    TextView mDisplay;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;
    String regid;


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

        /*
        currUser = new User(0,0,0);
        currUser.addFriend((new User(43.068762, -89.408195, 1, "A")));
        currUser.addFriend((new User(43.068619, -89.408314, 2, "B")));
        currUser.addFriend((new User(43.068873, -89.408581, 3, "C")));
        currUser.addFriend((new User(43.068317, -89.408142, 4, "D")));*/

        /* Start Grabbing your current location */
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener ll = new myLocationListener();
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, ll);
        View view = getWindow().getDecorView().findViewById(android.R.id.content);

        /* Grab the map and initialize */
        map = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map.getMapAsync(this);
        gmap  = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        /*Initialize GCM*/
        mDisplay = (TextView) findViewById(R.id.display);
        context = getApplicationContext();

        gcm = GoogleCloudMessaging.getInstance(this);
        regid = getRegistrationId(context);
        if (regid.isEmpty()) {
            registerInBackground();
        }

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




    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }
    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the registration ID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask() {
            protected void onPostExecute(String msg) {
                mDisplay.append(msg + "\n");
            }

            @Override
            protected Object doInBackground(Object[] params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the registration ID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }
        }.execute(null, null, null);
    }
    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
    }
    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    // Send an upstream message.
    public void onClick(final View view) {

        if (view == findViewById(R.id.send)) {
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    String msg = "";
                    try {
                        Bundle data = new Bundle();
                        data.putString("my_message", "Hello World");
                        data.putString("my_action", "com.google.android.gcm.demo.app.ECHO_NOW");
                        String id = Integer.toString(msgId.incrementAndGet());
                        gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
                        msg = "Sent message";
                    } catch (IOException ex) {
                        msg = "Error :" + ex.getMessage();
                    }
                    return msg;
                }

                @Override
                protected void onPostExecute(String msg) {
                    mDisplay.append(msg + "\n");
                }
            }.execute(null, null, null);
        }
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

        new AsyncTask() {
            protected Object doInBackground(Object[] params) {
                String msg = "";
                try {
                    Bundle data = new Bundle();
                    data.putString("my_message", "Hello World");
                    data.putString("my_action",
                            "com.google.android.gcm.demo.app.ECHO_NOW");
                    String id = Integer.toString(msgId.incrementAndGet());
                    gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
                    msg = "Sent message";
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }


            protected void onPostExecute(String msg) {
                mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
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
