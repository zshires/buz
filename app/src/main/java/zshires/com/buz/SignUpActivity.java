package zshires.com.buz;

import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
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


public class SignUpActivity extends Activity {
    String url = "https://still-journey-7705.herokuapp.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
    }

    public void signUp(View view){
        EditText pass = (EditText)findViewById(R.id.passWord);
        EditText user = (EditText)findViewById(R.id.userName);
        String password = pass.getText().toString();
        String username = user.getText().toString();

        TelephonyManager tMgr = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        String phonenumber = tMgr.getLine1Number();

        Toast.makeText(this, phonenumber, Toast.LENGTH_SHORT);

        AsyncHttpClient client = new AsyncHttpClient(url);
        StringEntity jsonParams = null;
        List<NameValuePair> params = null;
        if (password.length() >= 3 && username.length() >= 3) {
            try {
                //JSONObject json = new JSONObject();
                params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));
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
            //headers.add(new BasicHeader("Content-Type", "application/json"));

            //client.post("users.json", jsonParams, headers,new JsonResponseHandler() {

            client.post("users.json", params, headers, new JsonResponseHandler() {
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

                public void onFailure() {
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
        else{
            Toast toast = Toast.makeText(getApplicationContext(),"Username and Password must be " +
                    "greater than 3 characters each", Toast.LENGTH_SHORT);
            toast.show();
        }

    }
}
