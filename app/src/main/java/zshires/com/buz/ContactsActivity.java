package zshires.com.buz;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class ContactsActivity extends Activity {
    ArrayAdapter<String> adapter;
    ArrayList<String> listItems=new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        ArrayList<ContactTuple> myContacts = fetchContactsCProviderClient();
        ArrayList<String> test = new ArrayList<String>();

        for(ContactTuple contact :myContacts){
            test.add(contact.name);
            test.add(Integer.toString(contact.number));
        }

        ListAdapter theAdapter = new MyAdapter(this, test);

        ListView theListView = (ListView) findViewById(R.id.theListView);
        theListView.setAdapter(theAdapter);

        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String helloWorld = "yolo";
                String.valueOf(parent.getItemAtPosition(position));

                Toast.makeText(ContactsActivity.this, helloWorld, Toast.LENGTH_SHORT).show();
            }
        });
/*
        for (ContactTuple contact : myContacts){
            addItems(getListView(),contact.name);
            addItems(getListView(),Integer.toString(contact.number));
        }*/

    }

    private ArrayList<ContactTuple> fetchContactsCProviderClient()
    {
        ArrayList<ContactTuple> mContactList = null;
        try
        {
            ContentResolver cResolver= this.getContentResolver();
            //ContentProviderClient mCProviderClient = cResolver.acquireContentProviderClient(ContactsContract.Contacts.CONTENT_URI);
            Cursor mCursor = cResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            if (mCursor != null && mCursor.getCount() > 0)
            {
                mContactList = new ArrayList<ContactTuple>();
                mCursor.moveToFirst();
                while (!mCursor.isLast())
                {
                    String displayName = mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                    int numberIndex = mCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    ContactTuple ct = new ContactTuple(displayName, numberIndex);
                    mContactList.add(ct);
                    mCursor.moveToNext();
                }
                if (mCursor.isLast())
                {
                    String displayName = mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                    int numberIndex = mCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    ContactTuple ct = new ContactTuple(displayName,numberIndex);
                    mContactList.add(ct);
                }
            }

            mCursor.close();
        }
        /*catch (RemoteException e)
        {
            e.printStackTrace();
            mContactList = null;
        }*/
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
        public int number;
        public ContactTuple(String name, int number){
            this.number = number;
            this.name = name;
        }
    }

}

