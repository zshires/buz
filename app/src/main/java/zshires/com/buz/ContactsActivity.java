package zshires.com.buz;

import android.app.Activity;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.database.Cursor;
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

import net.callumtaylor.asynchttp.AsyncHttpClient;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;


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
                ContactTuple contact = (ContactTuple) parent.getItemAtPosition(position);
                String inviteNotif = "Invitation sent to " + contact.name;
                String yourName = currUser.getName();
                sendMessage("4148078600", yourName + " wants to add you to Buz! Download Buz on the Play Store to join the Hive!");
                Toast.makeText(ContactsActivity.this, inviteNotif, Toast.LENGTH_SHORT).show();

                ImageView img= (ImageView) view.findViewById(R.id.imageView1);
                img.setImageResource(R.drawable.ios7_plus_grey);

                /*
                AsyncHttpClient client = new AsyncHttpClient(url);
                List<Header> headers = new ArrayList<Header>();
                headers.add(new BasicHeader("Accept", "application/json"));
                headers.add(new BasicHeader("Content-Type", "application/json"));

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("phonenumber", contact.number));

                client.post("users/" + currUser.getID() + ".json", params, headers, new JsonResponseHandler() {
                    @Override
                    public void onSuccess() {
                        //TODO make view above final, will that work?
                        ImageView img = (ImageView) view.findViewById(R.id.imageView1);
                        img.setImageResource(R.drawable.ios7_plus_grey);
                    }

                    @Override
                    public void onFailure() {
                        //TODO make view above final, will that work?
                        ImageView img = (ImageView) view.findViewById(R.id.imageView1);
                        img.setImageResource(R.drawable.check);
                    }
                });
                */
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


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_contacts, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

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

