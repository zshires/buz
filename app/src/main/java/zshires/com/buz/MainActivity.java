package zshires.com.buz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.w3c.dom.Text;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    public static MapFragment map;
    TextView textLat;
    TextView textLng;
    private String SERVER_URL = "http://www.herokuapp.com/buz";
    private double latitude;
    private double longitude;
    Marker myMarker;


    private void setLatitude(double latitude){
        this.latitude = latitude;
    }
    private void setLongitude(double longitude){
        this.longitude = longitude;
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

        /*Current Location information*/
        textLat = (TextView) findViewById(R.id.lat);
        textLng = (TextView) findViewById(R.id.lng);
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener ll = new myLocationListener();
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
        View view = getWindow().getDecorView().findViewById(android.R.id.content);
        map = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map.getMapAsync(this);
        //other options: replace 'this' with Your_View.getContext(),Your_Activity_Name.this
        //or getApplicationContext()
        view.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                openMessages();
            }
        });

    }


    private ArrayList<User> getFriendsNearby() {
        ArrayList<User> friends = new ArrayList<User>();
        //getFriendsNearby();

        friends.add(new User(43.055, -89.4701468, 1, "A"));
        friends.add(new User(42.073286, -90.400713, 2, "B"));
        friends.add(new User(44.073286, -88.400713, 3, "C"));
        friends.add(new User(43.059, -89.4711468, 4, "D"));
        friends.add(new User(43.054, -89.4710468, 5, "E"));
        friends.add(new User(43.054661, -89.467234, 6, "F"));
        friends.add(new User(43.053995, -89.467384, 7, "G"));
        friends.add(new User(43.054164, -89.468038, 8, "H"));
        friends.add(new User(43.054618, -89.466944, 9, "I"));
        friends.add(new User(43.055026, -89.466815, 10, "J"));
        friends.add(new User(43.055026, -89.466815, 11,"K"));
        friends.add(new User(43.053773, -89.468634, 12, "L"));
        friends.add(new User(43.054120, -89.466462, 13, "M"));
        return friends;
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openMessages() {
        Intent intent = new Intent(this, MessagesActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }


    public void onClick_Madison(View v) {
        // CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(43.073286, -89.400713), 14);
        //   map.animateCamera(update);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.setOnMarkerClickListener(this);
        map.setMyLocationEnabled(true);
    }

    public Marker addMapMarker(GoogleMap map, double lat, double lon, String title) {
        return map.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title(title).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
    }

    public void startLocationService() {
        Intent i = new Intent(this, LocationService.class);
        startService(i);
    }

    public void stopLocationService() {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        //if (marker.equals(myMarker)) {
            Toast toast = Toast.makeText(getApplicationContext(), "You clicked a marker", Toast.LENGTH_SHORT);
            toast.show();
        //}
        return false;
    }

    class myLocationListener implements LocationListener {
        ArrayList<Marker> dummyMarkers = new ArrayList<Marker>();
        ArrayList<User> friends = getFriendsNearby();
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                for(Marker dummy : dummyMarkers){
                    if(dummy != null) {
                        dummy.remove(); //remove all the old markers on the map
                    }
                }
                dummyMarkers.clear(); // get rid of these markers. we will add the updated ones later
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                try{
                    GoogleMap gmap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
                   // now = addMapMarker(gmap,latitude,longitude,"me");
                    User me = new User(latitude,longitude);
                    for (User friend: friends){
                        if (friend.isInRange(me,500)) {
                            double lat = friend.getLatitude();
                            double lon = friend.getLongitude();
                            String name = friend.getName();
                            dummyMarkers.add(addMapMarker(gmap,lat,lon, name));
                        }
                    }
                } catch (Exception e){
                    Log.d("ERROR: Exception Thrown", "some error when trying to open the gmap");
                }

                setLatitude(latitude);
                setLongitude(longitude);
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

        }
    }

}
