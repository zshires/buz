package zshires.com.buz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import zshires.com.buz.R;

// We can create custom adapters
class ContactAdapter extends ArrayAdapter<ContactsActivity.ContactTuple> {

    public ContactAdapter(Context context, ArrayList<ContactsActivity.ContactTuple> values){

        super(context, R.layout.row_layout_2, values);

    }

    // Override getView which is responsible for creating the rows for our list
    // position represents the index we are in for the array.

    // convertView is a reference to the previous view that is available for reuse. As
    // the user scrolls the information is populated as needed to conserve memory.

    // A ViewGroup are invisible containers that hold a bunch of views and
    // define their layout properties.
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // The LayoutInflator puts a layout into the right View
        LayoutInflater theInflater = LayoutInflater.from(getContext());

        // inflate takes the resource to load, the parent that the resource may be
        // loaded into and true or false if we are loading into a parent view.
        View theView = theInflater.inflate(R.layout.row_layout_2, parent, false);

        // We retrieve the text from the array
        String contact = getItem(position).name;
        String phoneNum = getItem(position).number;

        // Get the TextView we want to edit
        TextView contactView = (TextView) theView.findViewById(R.id.contactView);
        TextView numberView = (TextView) theView.findViewById(R.id.numberView);

        // Put the next contact into the TextView
        contactView.setText(contact);
        numberView.setText(phoneNum);

    /*
        // Get the ImageView in the layout
        ImageView theImageView = (ImageView) theView.findViewById(R.id.imageView1);

        // We can set a ImageView like this
        theImageView.setImageResource(R.drawable.ic_launcher);
*/
        return theView;

    }
}