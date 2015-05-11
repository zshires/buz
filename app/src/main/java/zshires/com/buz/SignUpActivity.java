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

import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import net.callumtaylor.asynchttp.AsyncHttpClient;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


public class SignUpActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
    }

    public void signUp(View view){
        EditText pass = (EditText)findViewById(R.id.passWord);
        EditText userName = (EditText)findViewById(R.id.userName);
        String password = pass.getText().toString();
        String username = userName.getText().toString();

        TelephonyManager tMgr = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        String phonenumber = "\"" + tMgr.getLine1Number() + "\"";

        if (password.length() >= 3 && username.length() >= 3) {
            ParseUser user = new ParseUser();
            user.setUsername(username);
            user.setPassword(password);
            user.put("phone", phonenumber);

            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(com.parse.ParseException e) {
                    if (e == null) {
                        // Hooray! Let them use the app now.
                        Toast.makeText(SignUpActivity.this, "Success!.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                    } else {
                        Toast.makeText(SignUpActivity.this, "Sign up failed, try again.", Toast.LENGTH_SHORT).show();
                        Log.e("SignUpError", "something went wrong in sign up");
                    }
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
