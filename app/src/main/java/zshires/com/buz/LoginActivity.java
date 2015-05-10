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

import net.callumtaylor.asynchttp.AsyncHttpClient;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends FragmentActivity {
    private MainFragment mainFragment;
    String url = "https://still-journey-7705.herokuapp.com/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
        EditText user = (EditText)findViewById(R.id.username);
        String password = pass.getText().toString();
        String username = user.getText().toString();

        AsyncHttpClient client = new AsyncHttpClient(url);
        StringEntity jsonParams = null;
        List<NameValuePair> params = null;
        try {
            //JSONObject json = new JSONObject();
            params = new ArrayList<NameValuePair>();
            //params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));
            //json.put("username", username);
            //json.put("phonenumber", "2629021681");
            //json.put("latitude", "333");
            //json.put("longitude", "444");
            //jsonParams = new StringEntity(json.toString());
            //Log.d(TAG, json.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Accept", "application/json"));
        headers.add(new BasicHeader("Content-Type", "application/json"));

        //client.post("users.json", jsonParams, headers,new JsonResponseHandler() {
        final LoginActivity loginActivity = this;
        //user path
        String userPath = "users/" + username + ".json";
        client.get(userPath, params, headers, new JsonResponseHandler() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(getApplicationContext(), "Login success", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                });
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                loginActivity.finish();
            }

            public void onFailure() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                });
            }
        });

        /*try {
            EditText name = (EditText) findViewById(R.id.username);
            Integer idPref = Integer.parseInt(name.getText().toString());
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            prefs.edit().putInt("idPref", idPref).apply();

        }catch (Exception e){
            Log.e("Login Activity", "login button error");}
        finally {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }*/
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
