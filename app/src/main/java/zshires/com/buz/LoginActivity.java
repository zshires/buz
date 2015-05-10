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

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends FragmentActivity {
    private MainFragment mainFragment;
    String url = "https://still-journey-7705.herokuapp.com/";
    String failString = "{\"result\":\"fail\"}";


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
        final String password = pass.getText().toString();
        String username = user.getText().toString();

        AsyncHttpClient client = new AsyncHttpClient(url);

        /*StringEntity jsonParams = null;
        try{
            JSONObject json = new JSONObject();
            json.put("password", password);
            jsonParams = new StringEntity(json.toString());
        }catch (Exception e){
            e.printStackTrace();
        }*/

//        List<NameValuePair> params = null;
//        try {
//            //JSONObject json = new JSONObject();
//            params = new ArrayList<NameValuePair>();
//            //params.add(new BasicNameValuePair("username", username));
//            params.add(new BasicNameValuePair("password", password));
//            //json.put("username", username);
//            //json.put("phonenumber", "2629021681");
//            //json.put("latitude", "333");
//            //json.put("longitude", "444");
//            //jsonParams = new StringEntity(json.toString());
//            //Log.d(TAG, json.toString());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Accept", "application/json"));
        headers.add(new BasicHeader("Content-Type", "application/json"));

        //client.post("users.json", jsonParams, headers,new JsonResponseHandler() {
        final LoginActivity loginActivity = this;
        //user path
        String userPath = "users/" + username + ".json?password=" + password;

        //TODO check if headers are necessary
        client.get(userPath, null, headers, new JsonResponseHandler() {
            @Override
            public void onSuccess() {
                String result = String.valueOf(getContent());
                Log.d("Test",String.valueOf(getContent()));
                Log.d("Test", String.valueOf(getContent()));

                if(result.equals(failString)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(getApplicationContext(), "Incorrect username or password", Toast.LENGTH_SHORT);
                            toast.show();
                        }

                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(getApplicationContext(), "Login Success", Toast.LENGTH_SHORT);
                            toast.show();
                        }

                    });
                    Log.d("Result", "Load returned: " + result);
                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
                    User user = gson.fromJson(result, User.class);
                    Log.d("User:" ,user.toString());

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    /*
                    intent.putExtra("username", user.getName());
                    intent.putExtra("phonenumber", user.getPhonenumber());
                    intent.putExtra("friends",user.getFriends());
                    intent.putExtra("id", user.getID());
                    */
                    intent.putExtra("user",user);
                    intent.putExtra("password", password);
                    startActivity(intent);
                    loginActivity.finish();


                }//this is where end of else

            }

            public void onFailure() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(getApplicationContext(), "Server Communication Error", Toast.LENGTH_SHORT);
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
