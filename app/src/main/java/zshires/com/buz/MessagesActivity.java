package zshires.com.buz;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
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


public class MessagesActivity extends ActionBarActivity {

    EditText txtMsgEditText, pNumEditText, messagesEditText;
    Button sendButton;
    static String messages = "";
    // Allows use to update the UI with new messages by telling the Activity
    // to update the UI every 5 seconds
    // A handler can schedule for code to execute at a set time in this Activities
    // thread
    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        txtMsgEditText = (EditText) findViewById(R.id.txtMsgEditText);
        pNumEditText = (EditText) findViewById(R.id.pNumEditText);
        messagesEditText = (EditText) findViewById(R.id.messagesEditText);
        sendButton = (Button) findViewById(R.id.sendButton);

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
}
