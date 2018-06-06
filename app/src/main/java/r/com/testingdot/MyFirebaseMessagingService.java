package r.com.testingdot;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by rufotamo on 13/05/2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...
        String sMensaje = null;
        String ID = null;
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            Map <String,String> msg = remoteMessage.getData();
            for (Map.Entry<String, String> entry : msg.entrySet()) {
                //Toast t = Toast.makeText(this,entry.getValue(),Toast.LENGTH_LONG);
                if(entry.getKey().equalsIgnoreCase("message"))
                    sMensaje=entry.getValue();
                else
                    ID=entry.getValue();
            }
        }
        //if (/* Check if data needs to be processed by long running job */ true) {
        // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
             /*   scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }*/

        // }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

            int id = 0;
            String image = remoteMessage.getNotification().getIcon();
            String title = remoteMessage.getNotification().getTitle();
            String text = remoteMessage.getNotification().getBody();
            Object obj = remoteMessage.getData().get("id");
            if (obj != null) {
                id = Integer.valueOf(obj.toString());
            }
            String sound = remoteMessage.getNotification().getSound();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("theMessage", sMensaje);
            intent.putExtra("UID",ID);
            startActivity(intent);
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }


}
