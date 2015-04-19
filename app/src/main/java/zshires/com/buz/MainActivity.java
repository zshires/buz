package zshires.com.buz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.facebook.AppEventsLogger;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.w3c.dom.Text;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity implements OnMapReadyCallback {
    private MapFragment map;
    private ArrayList<User> friends;
    TextView textLat;
    TextView textLng;
    private String SERVER_URL = "http://www.herokuapp.com/buz";

    private double latitude;
    private double longitude;


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
        setContentView(R.layout.gps);
        textLat = (TextView) findViewById(R.id.lat);
        textLng = (TextView) findViewById(R.id.lng);
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener ll = new myLocationListener();
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
    }
        class myLocationListener implements LocationListener{
            @Override
            public void onLocationChanged(Location location) {
                if(location != null){
                    double pLong = location.getLongitude();
                    double pLat = location.getLatitude();

                    //TODO check if this works
                    setLatitude(pLat);
                    setLongitude(pLong);
                    //

                    textLat.setText(Double.toString(pLat));
                    textLng.setText(Double.toString(pLong));
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

/*
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

        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .build();



    }
*/

    private ArrayList<User> getFriendsNearby(){
        User mike = new User(43.073286,-89.400713,1, "Mike");
        User geoff = new User(42.073286,-90.400713,2, "Geoff");
        User zak = new User(44.073286,-88.400713,3, "Zak");
        ArrayList myFriends = new ArrayList<User>();
        myFriends.add(mike);
        myFriends.add(geoff);
        myFriends.add(zak);
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

    public void openMessages(){
        Intent intent = new Intent(this, MessagesActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }


    public void onClick_Madison(View v){
       // CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(43.073286, -89.400713), 14);
     //   map.animateCamera(update);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        friends = getFriendsNearby();
        User me = new User(this.latitude,this.longitude);

        for (User friend: friends){
            if (friend.isInRange(me)) {
                double lat = friend.getLatitude();
                double lon = friend.getLongitude();
                String name = friend.getName();

                map.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title(name));
            }
        }
        map.setMyLocationEnabled(true);
    }

    public void startLocationService(){
        Intent i = new Intent(this, LocationService.class);
        startService(i);
    }

    public void stopLocationService(){

    }

}
