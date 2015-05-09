package zshires.com.buz;

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
import android.widget.ArrayAdapter;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import java.util.ArrayList;


public class ContactsActivity extends ListActivity {
    ArrayAdapter<String> adapter;
    ArrayList<String> listItems=new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        ArrayList<ContactTuple> myContacts = fetchContactsCProviderClient();

        for (ContactTuple contact : myContacts){
            addItems(getListView(),contact.name);
            addItems(getListView(),contact.number);
        }
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
                while (!mCursor.isLast())
                {
                    String displayName =
                            mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                    String contactId =
                            mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts._ID));
                    Cursor phones = cResolver.query(Phone.CONTENT_URI, null,
                            Phone.CONTACT_ID + " = " + contactId, null, null);

                    while (phones.moveToNext()) {
                        String number = phones.getString(phones.getColumnIndex(Phone.NUMBER));
                        int type = phones.getInt(phones.getColumnIndex(Phone.TYPE));

                        if (type == Phone.TYPE_MOBILE) {
                            ContactTuple ct = new ContactTuple(displayName, number);
                            mContactList.add(ct);
                            break;
                        }
                    }
                    phones.close();
                    mCursor.moveToNext();
                }
                if (mCursor.isLast())
                {
                    String displayName = mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                    String contactId =
                            mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts._ID));
                    Cursor phones = cResolver.query(Phone.CONTENT_URI, null,
                            Phone.CONTACT_ID + " = " + contactId, null, null);

                    while (phones.moveToNext()) {
                        String number = phones.getString(phones.getColumnIndex(Phone.NUMBER));
                        int type = phones.getInt(phones.getColumnIndex(Phone.TYPE));

                        if (type == Phone.TYPE_MOBILE) {
                            ContactTuple ct = new ContactTuple(displayName, number);
                            mContactList.add(ct);
                            break;
                        }
                        phones.close();
                    }
                }
            }

            mCursor.close();
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            mContactList = null;
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

}

