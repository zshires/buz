package zshires.com.buz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by zshires on 5/9/2015.
 */
public class FriendsActivity extends ActionBarActivity {
    ArrayList<User> friendsToRemove = new ArrayList<User>();
    User currUser = MainActivity.getCurrUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        View view = getWindow().getDecorView().findViewById(android.R.id.content);
        view.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeRight() {
                finish();
            }
        });

        ArrayList<User> friendList = MainActivity.getCurrUser().getFriends();
        ListAdapter theAdapter = new FriendAdapter(this, friendList);
        ListView theListView = (ListView) findViewById(R.id.friendsList);
        theListView.setAdapter(theAdapter);


        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User myFriend = (User) parent.getItemAtPosition(position);
                if(!friendsToRemove.contains(myFriend)) {
                    friendsToRemove.add(myFriend);
                    ImageView img = (ImageView) view.findViewById(R.id.imageView1);
                    img.setImageResource(R.drawable.close);
                } else {
                    friendsToRemove.remove(myFriend);
                    ImageView img = (ImageView) view.findViewById(R.id.imageView1);
                    img.setImageResource(R.drawable.check);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_friends);

        View view = getWindow().getDecorView().findViewById(android.R.id.content);
        view.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeRight() {
                finish();
            }
        });

        ArrayList<User> friendList = MainActivity.getCurrUser().getFriends();
        ListAdapter theAdapter = new FriendAdapter(this, friendList);
        ListView theListView = (ListView) findViewById(R.id.friendsList);
        theListView.setAdapter(theAdapter);


        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User myFriend = (User) parent.getItemAtPosition(position);
                if(!friendsToRemove.contains(myFriend)) {
                    friendsToRemove.add(myFriend);
                    ImageView img = (ImageView) view.findViewById(R.id.imageView1);
                    img.setImageResource(R.drawable.close);
                } else {
                    friendsToRemove.remove(myFriend);
                    ImageView img = (ImageView) view.findViewById(R.id.imageView1);
                    img.setImageResource(R.drawable.check);
                }
            }
        });
    }


    public void send_a_message(View v) {
        TextView theTextView = (TextView) v.findViewById(R.id.friendNameView);
        Intent intent = new Intent(getBaseContext(), MessagesActivity.class);
        intent.putExtra("nameOfRecipient",  theTextView.getText());
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        final ArrayList<String> friendsToRemoveNames = new ArrayList<String>();

        for(User u : friendsToRemove){
            friendsToRemoveNames.add(u.getName());
        }

        for(String del : friendsToRemoveNames){
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Friends");
            query.whereEqualTo("username1", currUser.getName());
            query.whereEqualTo("username2", del);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                    if (e == null) {
                        for(ParseObject p : parseObjects) {
                           p.deleteInBackground();
                        }
                    } else {
                        Log.d("Error", "Error: " + e.getMessage());
                    }
                }
            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_friends, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_contacts:
                openContacts();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openContacts() {
        Intent intent = new Intent(this, ContactsActivity.class);
        startActivity(intent);
    }


}
