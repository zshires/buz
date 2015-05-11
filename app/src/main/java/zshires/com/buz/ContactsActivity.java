package zshires.com.buz;

import android.app.Activity;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.database.Cursor;
import android.nfc.Tag;
import android.provider.ContactsContract;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import net.callumtaylor.asynchttp.AsyncHttpClient;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class ContactsActivity extends Activity {
    ArrayAdapter<String> adapter;
    ArrayList<String> listItems = new ArrayList<String>();
    User currUser;
    String url = "https://still-journey-7705.herokuapp.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        ArrayList<ContactTuple> myContacts = fetchContactsCProviderClient();
        currUser = MainActivity.getCurrUser();

        ListAdapter theAdapter = new ContactAdapter(this, myContacts);

        ListView theListView = (ListView) findViewById(R.id.theListView);
        theListView.setAdapter(theAdapter);



        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ContactTuple contact = (ContactTuple) parent.getItemAtPosition(position);
                final String inviteNotif = "Invitation sent to " + contact.name;


                ParseObject gameScore = new ParseObject("Friends");
                gameScore.put("username1", ParseUser.getCurrentUser().getUsername());
                gameScore.put("username2", contact.name);
                gameScore.saveInBackground();
                Toast.makeText(ContactsActivity.this, "Adding  " + contact.name + " to your network of friends", Toast.LENGTH_SHORT).show();
/*
                //look in our user table to see if they exist
                ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
                query.whereEqualTo("username", contact.name);
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                        boolean foundUser = false;
                        if (e == null) {


                                    Toast.makeText(ContactsActivity.this, "Adding  " + contact.name + " toy our network of friends", Toast.LENGTH_SHORT).show();

                                    ParseObject newFriend = new ParseObject("Friendz");
                                    newFriend.put(currUser.getName(), contact.name);
                                    newFriend.saveInBackground();

                                    foundUser = true;



                            if(!foundUser){
                                Toast.makeText(ContactsActivity.this, "Sending an invitation to " + contact.name, Toast.LENGTH_SHORT).show();
                                //TODO send sms to that contact
                            }
                        } else {

                        }
                    }
                });
*/

                ImageView img = (ImageView) view.findViewById(R.id.imageView1);
                img.setImageResource(R.drawable.ios7_plus_grey);
            }
        });
    }

    private ArrayList<ContactTuple> fetchContactsCProviderClient()
    {
        ArrayList<ContactTuple> mContactList = null;
        try
        {
            ContentResolver cResolver= this.getContentResolver();
            ContentProviderClient mCProviderClient = cResolver.acquireContentProviderClient(ContactsContract.Contacts.CONTENT_URI);
            Cursor mCursor = mCProviderClient.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            if (mCursor != null && mCursor.getCount() > 0)
            {
                mContactList = new ArrayList<ContactTuple>();
                mCursor.moveToFirst();
                while (mCursor.moveToNext())
                {
                    String displayName =
                            mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                    String contactId =
                            mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts._ID));
                    Cursor phones = cResolver.query(Phone.CONTENT_URI, null,
                            Phone.CONTACT_ID + " = " + contactId, null, null);

                    while (phones.moveToNext()) {
                        String number = phones.getString(phones.getColumnIndex(Phone.NUMBER));
                        ContactTuple ct = new ContactTuple(displayName, number);
                        mContactList.add(ct);
                        break;
                    }
                    phones.close();
                }
            }
            mCursor.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            mContactList = null;
        }

        return mContactList;
    }

    //METHOD WHICH WILL HANDLE DYNAMIC INSERTION
    public void addItems(View v, String toAdd) {
        listItems.add(toAdd);
        adapter.notifyDataSetChanged();
    }


    public class ContactTuple{
        public String name;
        public String number;
        public ContactTuple(String name, String number){
            this.number = number;
            this.name = name;
        }
    }

    public void sendMessage(String phoneNum, String message) {
        try{
            // Handles sending and receiving data and text
            SmsManager smsManager = SmsManager.getDefault();
            // Sends the text message
            // 2nd is for the service center address or null
            // 4th if not null broadcasts with a successful send
            // 5th if not null broadcasts with a successful delivery
            smsManager.sendTextMessage(phoneNum, null, message, null, null);
        }
        catch (IllegalArgumentException ex){
            Log.e("TEXTING", "Destination Address or Data Empty");
            Toast.makeText(this, "Enter a Phone Number and Message", Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
        catch (Exception ex) {
            Toast.makeText(this, "Message Not Sent", Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

}

