package zshires.com.buz;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v4.app.FragmentActivity;
import com.facebook.android.Facebook;

public class LoginActivity extends FragmentActivity {
    private MainFragment mainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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
        try {
            EditText name = (EditText) findViewById(R.id.username);
            Integer idPref = Integer.parseInt(name.getText().toString());
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            prefs.edit().putInt("idPref", idPref).commit();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }catch (Exception e){}
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
