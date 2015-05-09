package zshires.com.buz;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Michael on 4/18/2015.
 */
public class User {
    private double latitude;
    private double longitude;
    private int id;
    private String username;
    private ArrayList<User> friends = new ArrayList<User>();
    double phonenumber;

    public int getID(){
        return this.id;
    }
    /*
    public User(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }*/
    public User(double latitude, double longitude, int id){
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = id;
    }
    public User(double latitude, double longitude, int id, String name){
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = id;
        this.username = name;
    }

    public User(double latitude, double longitude, int id, String name, ArrayList<User> friends){
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = id;
        this.username = name;
        this.friends = friends;
    }

    public ArrayList<User> getFriends(){
        return this.friends;
    }
    public double getLongitude(){
        return this.longitude;
    }
    public void setLatitude(double latitude){
        this.latitude = latitude;
    }
    public void setLongitude(double longitude){
        this.longitude = longitude;
    }
    public double getLatitude(){
        return this.latitude;
    }
    public String getName(){
        return this.username;
    }

    public String toString(){
        String myString = "" + username + " lat: " + latitude + " long: " + longitude + " friends: ";
        if (friends != null){
            for(User u: friends){
                myString += u.toString();
            }
        }
        return myString;
    }

    public void addFriend(User user){
        this.friends.add(user);
    }
    public void removeFriend(User user) {
        if(friends.contains(user)){
            friends.remove(user);
        } else {
            Log.e("Warning", "Removing user not found");
        }
    }


    public double distance(User myFriend){
        double lat1 = this.latitude;
        double lon1 = this.longitude;

        double lat2 = myFriend.getLatitude();
        double lon2 = myFriend.getLongitude();

        double R = 6378.137; // Radius of earth in KM
        double dLat = (lat2 - lat1) * Math.PI / 180;
        double dLon = (lon2 - lon1) * Math.PI / 180;
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c;
        return d * 1000; // meters
    }

    public boolean isInRange(User myFriend){
        return distance(myFriend) < 100;
    }
    public boolean isInRange(User myFriend, double range){
        return distance(myFriend) < range;
    }



}
