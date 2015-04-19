package zshires.com.buz;
/* Hey Geoff */

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

public class MainActivity extends ActionBarActivity implements OnMapReadyCallback {
    private MapFragment map;
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

        startLocationService();
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
        map.addMarker(new MarkerOptions().position(new LatLng(43.073286, -89.400713)).title("Madison"));
        map.setMyLocationEnabled(true);
    }

    public void startLocationService(){
        Intent i = new Intent(this, LocationService.class);
        startService(i);
    }

    public void stopLocationService(){

    }

}
