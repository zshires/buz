package zshires.com.buz;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.FragmentActivity;
import com.facebook.android.Facebook;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import net.callumtaylor.asynchttp.AsyncHttpClient;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class LoginActivity extends FragmentActivity {
    private MainFragment mainFragment;
    public ParseUser user;
    String url = "https://still-journey-7705.herokuapp.com/";
    String failString = "{\"result\":\"fail\"}";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "eArOXwN5rOCtS0mlykpVf96A4EFqVTz3tw6PAGfe", "VPOIyz24WSvZRQnIOc9mllhd4tNfvKJz0WChhFKc");


    //save test obj
        //ParseObject testObject = new ParseObject("TestObject");
        //testObject.put("foo", "bar");
        //testObject.saveInBackground();



        TextView tv = (TextView)findViewById(R.id.loginTitle);
        Typeface face = Typeface.createFromAsset(getAssets(),"fonts/Aventura-Bold.otf");
        tv.setTypeface(face);

        if (savedInstanceState == null) {
            // Add the fragment on initial activity setup
            mainFragment = new MainFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, mainFragment)
                    .commit();
        } else {
            // Or set the fragment from restored state info
            mainFragment = (MainFragment) getSupportFragmentManager()
                    .findFragmentById(android.R.id.content);
        }
    }

    public void login(View view){
        EditText pass = (EditText)findViewById(R.id.password);
        EditText userName = (EditText)findViewById(R.id.username);
        final String password = pass.getText().toString();
        String username = userName.getText().toString();

        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, com.parse.ParseException e) {
                if (parseUser != null) {
                    // Hooray! The user is logged in.
                    Toast.makeText(LoginActivity.this, "Success!.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed, try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void forgotPassword(View view){
        Toast toast = Toast.makeText(getApplicationContext(), "You should think harder", Toast.LENGTH_SHORT);
        toast.show();
    }

    public void signUp(View view){
        //Toast toast = Toast.makeText(getApplicationContext(), "FEATURE NOT YET IMPLEMENTED", Toast.LENGTH_SHORT);
        //toast.show();
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

}
