package zshires.com.buz;

import android.util.Log;

/**
 * Created by Michael on 4/18/2015.
 */
public class User {
    private double latitude;
    private double longitude;
    private int id;
    private String name;

    public User(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public User(double latitude, double longitude, int id){
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = id;
    }
    public User(double latitude, double longitude, int id, String name){
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = id;
        this.name = name;
    }
    public double getLongitude(){
        return this.longitude;
    }
    public double getLatitude(){
        return this.latitude;
    }
    public String getName(){
        return this.name;
    }
    public String toString(){
        String myString = "" + name + " lat: " + latitude + " long: " + longitude;
        return myString;
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
        Log.d("myTag", Double.toString(d*1000));
        return d * 1000; // meters
    }

    public boolean isInRange(User myFriend){
        return distance(myFriend) < 100;
    }
    public boolean isInRange(User myFriend, double range){
        return distance(myFriend) < range;
    }



}
