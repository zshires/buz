package zshires.com.buz;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.gsm.SmsMessage;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.GregorianCalendar;


public class MessagesActivity extends ActionBarActivity {

    EditText txtMsgEditText, pNumEditText, messagesEditText;
    Button sendButton;
    static String messages = "";
    // Allows use to update the UI with new messages by telling the Activity
    // to update the UI every 5 seconds
    // A handler can schedule for code to execute at a set time in this Activities
    // thread
    Handler mHandler = new Handler();

    Button showNotificationBut, stopNotificationBut, alertButton;

    // Allows us to notify the user that something happened in the background
    NotificationManager notificationManager;

    // Used to track notifications
    int notifID = 33;

    // Used to track if notification is active in the task bar
    boolean isNotificActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        txtMsgEditText = (EditText) findViewById(R.id.txtMsgEditText);
        pNumEditText = (EditText) findViewById(R.id.pNumEditText);
        messagesEditText = (EditText) findViewById(R.id.messagesEditText);
        sendButton = (Button) findViewById(R.id.sendButton);

        // Initialize notificaiton buttons
        showNotificationBut = (Button) findViewById(R.id.showNotificationBut);
        stopNotificationBut = (Button) findViewById(R.id.stopNotificationBut);
        alertButton = (Button) findViewById(R.id.alertButton);

        // Thread updates the messages EditText every 10 seconds
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (true) {
                    try {
                        // Wait 5 seconds and then execute the code in run()
                        Thread.sleep(5000);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                // Update the messagesEditText
                                messagesEditText.setText(messages);
                            }
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void sendMessage(View view) {

        // Get the phone number and message to send
        String phoneNum = pNumEditText.getText().toString();
        String message = txtMsgEditText.getText().toString();

        try{

            // Handles sending and receiving data and text
            SmsManager smsManager = SmsManager.getDefault();

            // Sends the text message
            // 2nd is for the service center address or null
            // 4th if not null broadcasts with a successful send
            // 5th if not null broadcasts with a successful delivery
            smsManager.sendTextMessage(phoneNum, null, message, null, null);

            Toast.makeText(this, "Message Sent", Toast.LENGTH_SHORT).show();

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

        // Update the message EditText
        messages = messages + "You : " + message + "\n";

    }

    // Receives texts
    public static class SmsReceiver extends BroadcastReceiver {

        // Handles sending and receiving data and text
        final SmsManager smsManager = SmsManager.getDefault();

        public SmsReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            final Bundle bundle = intent.getExtras();

            try{

                // Check if we received data
                if (bundle != null){

                    // Store data sent as a PDU (Protocal Data Unit) which includes the
                    // number and text
                    final Object[] pdusObj = (Object[]) bundle.get("pdus");

                    // Cycle through the data received
                    for (int i = 0; i < pdusObj.length; i++) {

                        // Create a SmsMessage from the raw PDU data
                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);

                        // Get sending phone number
                        String phoneNumber = smsMessage.getDisplayOriginatingAddress();

                        // Get the message sent
                        String message = smsMessage.getDisplayMessageBody();

                        // Update the messages EditText
                        // messages = messages + phoneNumber + " : " + message + "\n";

                        // I use this to block the receiving number
                        messages = messages + "Sender : " + message + "\n";

                    } // end for loop
                } // bundle is null

            } catch (Exception ex) {
                Log.e("SmsReceiver", "Exception smsReceiver" +ex);

            }

        }

    }

    // Handles receiving MMS
    public class MMSReceiver extends BroadcastReceiver {
        public MMSReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            throw new UnsupportedOperationException("Not Implemented Yet");

        }

    }

    // Handles when you want to send a pre-written message when a call is rejected
    public class HeadlessSmsSendService extends BroadcastReceiver {
        public HeadlessSmsSendService() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            throw new UnsupportedOperationException("Not Implemented Yet");

        }

    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_messages, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_home:
                openHome();
                return true;
            case R.id.action_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openHome() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void showNotification(View view) {

        // Builds a notification
        NotificationCompat.Builder notificBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Message")
                .setContentText("New Message")
                .setTicker("Alert New Message")
                .setSmallIcon(R.drawable.bee);

        // Define that we have the intention of opening MoreInfoNotification
        Intent moreInfoIntent = new Intent(this, MoreInfoNotification.class);

        // Used to stack tasks across activites so we go to the proper place when back is clicked
        TaskStackBuilder tStackBuilder = TaskStackBuilder.create(this);

        // Add all parents of this activity to the stack
        tStackBuilder.addParentStack(MoreInfoNotification.class);

        // Add our new Intent to the stack
        tStackBuilder.addNextIntent(moreInfoIntent);

        // Define an Intent and an action to perform with it by another application
        // FLAG_UPDATE_CURRENT : If the intent exists keep it but update it if needed
        PendingIntent pendingIntent = tStackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Defines the Intent to fire when the notification is clicked
        notificBuilder.setContentIntent(pendingIntent);

        // Gets a NotificationManager which is used to notify the user of the background event
        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Post the notification
        notificationManager.notify(notifID, notificBuilder.build());

        // Used so that we can't stop a notification that has already been stopped
        isNotificActive = true;


    }

    public void stopNotification(View view) {

        // If the notification is still active close it
        if(isNotificActive) {
            notificationManager.cancel(notifID);
        }

    }

    public void setAlarm(View view) {

        // Define a time value of 5 seconds
        Long alertTime = new GregorianCalendar().getTimeInMillis()+5*1000;

        // Define our intention of executing AlertReceiver
        Intent alertIntent = new Intent(this, AlertReceiver.class);

        // Allows you to schedule for your application to do something at a later date
        // even if it is in he background or isn't active
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // set() schedules an alarm to trigger
        // Trigger for alertIntent to fire in 5 seconds
        // FLAG_UPDATE_CURRENT : Update the Intent if active
        alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime,
                PendingIntent.getBroadcast(this, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));

    }
}
