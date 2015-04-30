package zshires.com.buz;

import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import net.callumtaylor.asynchttp.AsyncHttpClient;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;

import java.util.ArrayList;
import java.util.List;


public class SignUpActivity extends ActionBarActivity {
    String url = "https://still-journey-7705.herokuapp.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void signUp(View view){
        EditText pass = (EditText)findViewById(R.id.passWord);
        EditText user = (EditText)findViewById(R.id.userName);
        String password = pass.getText().toString();
        String username = user.getText().toString();

        AsyncHttpClient client = new AsyncHttpClient(url);
        StringEntity jsonParams = null;
        List<NameValuePair> params = null;

        try{
            //JSONObject json = new JSONObject();
            params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("username", username));
            //json.put("username", username);
            //json.put("phonenumber", "2629021681");
            //json.put("latitude", "333");
            //json.put("longitude", "444");
            //jsonParams = new StringEntity(json.toString());
            //Log.d(TAG, json.toString());

        }catch(Exception e){
            e.printStackTrace();
        }

        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Accept", "application/json"));
        headers.add(new BasicHeader("Content-Type", "application/json"));

        //client.post("users.json", jsonParams, headers,new JsonResponseHandler() {

        client.post("users.json", params, headers,new JsonResponseHandler() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(getApplicationContext(), "Sign up success", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                });
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(intent);
            }

            public void onFailure(){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(getApplicationContext(), "Sign up failed", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                });
            }
        });

    }
}
