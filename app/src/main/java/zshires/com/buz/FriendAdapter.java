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
class FriendAdapter extends ArrayAdapter<User> {

    public FriendAdapter(Context context, ArrayList<User> values){
        super(context, R.layout.row_layout_3, values);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater theInflater = LayoutInflater.from(getContext());
        View theView = theInflater.inflate(R.layout.row_layout_3, parent, false);
        String friendName = getItem(position).getName();
        TextView friendNameView = (TextView) theView.findViewById(R.id.friendNameView);
        friendNameView.setText(friendName);
        return theView;
    }
}