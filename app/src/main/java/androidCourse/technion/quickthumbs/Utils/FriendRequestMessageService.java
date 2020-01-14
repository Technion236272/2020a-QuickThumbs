package androidCourse.technion.quickthumbs.Utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

import androidCourse.technion.quickthumbs.MainPager;
import androidCourse.technion.quickthumbs.NotificationActivity;
import androidCourse.technion.quickthumbs.R;


public class FriendRequestMessageService extends FirebaseMessagingService {
    private NotificationManager notificationManager;
    private String CHANNEL_ID = "F_REQ";
    int notificationId = 1;

    public FriendRequestMessageService() {
        super();
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        if (message == null || message.getNotification() == null) {
            return;
        }

        //stuff we get from the cloud
        String title = message.getNotification().getTitle();
        String body = message.getNotification().getBody();
        String from = message.getFrom().substring(25);// from the 25 index is the uid of the sender

        NotificationCompat.Builder builder;

        if (title.equals("Request accepted") ){
            builder = setFriendAcceptNotification(title, body, from);
        }else{
            builder = setFriendRequestNotification(title, body, from);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

        // notificationId is a unique int for each notification that you must define

        notificationManager.notify(notificationId, builder.build());

    }

    private NotificationCompat.Builder setFriendAcceptNotification(String title, String body, String from) {
        notificationId = 3;
        //the press on notification answer
        PendingIntent regularPendingIntent = setPressOnNotificationIntent();
        //the yes answer
        PendingIntent acceptPendingIntent = setPendingIntent(title, body, from, true);
        //the no answer
        PendingIntent rejectPendingIntent = setPendingIntent(title, body, from, false);

        //This is the proper solution for dismiss the dialog aka the dismiss a
        PendingIntent pIntentDismiss = setDismissNotificationIntent();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                // Set the intent that will fire when the user taps the notification
                .addAction(R.drawable.eye_off, "Dismiss", pIntentDismiss)
//                .setContentIntent(regularPendingIntent)
                .setWhen(0) //important so the whole message will be shown
                .setAutoCancel(true);


        setNotificationSenderImage(from, builder);
        return builder;
    }


    private NotificationCompat.Builder setFriendRequestNotification(String title, String body, String from) {
        notificationId = 2;
        //the press on notification answer
        PendingIntent regularPendingIntent = setPressOnNotificationIntent();
        //the yes answer
        PendingIntent acceptPendingIntent = setPendingIntent(title, body, from, true);
        //the no answer
        PendingIntent rejectPendingIntent = setPendingIntent(title, body, from, false);

        //This is the proper solution for dismiss the dialog aka the dismiss a
        PendingIntent pIntentDismiss = setDismissNotificationIntent();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                // Set the intent that will fire when the user taps the notification
                .addAction(R.drawable.account_plus, "Accept", acceptPendingIntent) // #0
                .addAction(R.drawable.account_remove, "Reject", rejectPendingIntent)  // #1
                .addAction(R.drawable.eye_off, "Dismiss", pIntentDismiss)
//                .setContentIntent(regularPendingIntent)
                .setWhen(0) //important so the whole message will be shown
                .setAutoCancel(true);


        setNotificationSenderImage(from, builder);
        return builder;
    }

    private void setNotificationSenderImage(String from, NotificationCompat.Builder builder) {
        CacheHandler cacheHandler = new CacheHandler(getApplicationContext());
        if (cacheHandler.isUsingProfilePicture()) {
            cacheHandler.getProfilePicture(from, builder);
//            Bitmap icon=cacheHandler.ShrinkBitmap(profilePicture,64,64);
//            builder.setLargeIcon(icon);
        }
    }

    private PendingIntent setDismissNotificationIntent() {
        //Create an Intent for the BroadcastReceiver
        Intent intentDismiss = new Intent(getApplicationContext(), NotificationActivity.class);
        intentDismiss.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intentDismiss.putExtra("dismiss", true);
        //Create the PendingIntent
        int rand = new Random().nextInt('Z'-'A');
        String xId = String.valueOf(rand);
        intentDismiss.putExtra("x_id", xId);
        intentDismiss.setAction(xId);
        return PendingIntent.getActivity(getApplicationContext(), 1, intentDismiss, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent setPendingIntent(String title, String body, String from, boolean b) {
        //the yes answer
        Intent acceptIntent = new Intent(getApplicationContext(), NotificationActivity.class);
        acceptIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        acceptIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        acceptIntent.putExtra("answer", b);
        acceptIntent.putExtra("title", title);
        acceptIntent.putExtra("body", body);
        acceptIntent.putExtra("from", from);
        int rand = new Random().nextInt('Z'-'A');
        String xId = String.valueOf(rand);
        acceptIntent.putExtra("x_id", xId);
        acceptIntent.setAction(xId);
        return PendingIntent.getActivity(getApplicationContext(), 1, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent setPressOnNotificationIntent() {
        //the click on the notification
        Intent regularIntent = new Intent(getApplicationContext(), NotificationActivity.class);
        regularIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int rand = new Random().nextInt('Z'-'A');
        String xId = String.valueOf(rand);
        regularIntent.putExtra("x_id", xId);
        regularIntent.setAction(xId);
        return PendingIntent.getActivity(getApplicationContext(), 1, regularIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private CharSequence getMessageText(Intent intent) {
        Bundle remoteInput = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT_WATCH) {
            remoteInput = RemoteInput.getResultsFromIntent(intent);
        }
        if (remoteInput != null) {
            return remoteInput.getCharSequence("got message");
        }
        return null;
    }


    public void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "friendRequest";
            String description = "friendRequest";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

        } else {
            notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }


}
