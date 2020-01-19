package androidCourse.technion.quickthumbs.Utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Random;

import androidCourse.technion.quickthumbs.MainPager;
import androidCourse.technion.quickthumbs.MainUserActivity;
import androidCourse.technion.quickthumbs.NotificationActivity;
import androidCourse.technion.quickthumbs.R;

import static android.graphics.drawable.Icon.createWithResource;


public class FriendRequestMessageService extends FirebaseMessagingService {
    private NotificationManager notificationManager;
    private String CHANNEL_ID = "F_REQ";
    String TAG = FriendRequestMessageService.class.getSimpleName();

    public FriendRequestMessageService() {
        super();
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        if (message == null || message.getFrom() == null) {
            return;
        }

        //stuff we get from the cloud
        String title = message.getData().get("title");
        String body = message.getData().get("body");
        String from = message.getData().get("sender");// from the 25 index is the uid of the sender
        String fromId = message.getData().get("senderId");// from the 25 index is the uid of the sender
        String roomKey = message.getData().get("room");
        NotificationCompat.Builder builder;
        if (fromId == getUid())//prevent self sending notification
            return;
        //choose the appropriate notification
        int oneTimeID = (int) SystemClock.uptimeMillis();
        switch (title) {
            case "You are Friends!":
                builder = setFriendAcceptNotification(title, body, from, fromId, oneTimeID);

                break;
            case "New Friend Request":
                builder = setFriendRequestNotification(title, body, from, fromId, oneTimeID);

                break;
            case "Game invite":
                MainUserActivity.acceptedInvitationRoomKey = roomKey;
                MainUserActivity.invitationSender = from;
                builder = setFriendGameInviteNotification(title, body, from, fromId, roomKey, oneTimeID);
                break;
            default:

                return;
        }

        builder.setOngoing(false);
        builder.setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(oneTimeID, builder.build());
    }

    private NotificationCompat.Builder setFriendGameInviteNotification(String title, String body, String from, String fromId, String roomKey,
                                                                       int notification_id) {
        //the press on notification answer
        PendingIntent regularPendingIntent = setPressOnNotificationIntent(notification_id);
        //the yes answer
        PendingIntent acceptPendingIntent = setGameAnswerPendingIntent(title, body, from, fromId, roomKey, true, notification_id);
        //the no answer
        PendingIntent rejectPendingIntent = setGameAnswerPendingIntent(title, body, from, fromId, roomKey, false, notification_id);

        //This is the proper solution for dismiss the dialog aka the dismiss a

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                // Set the intent that will fire when the user taps the notification
                .addAction(R.drawable.account_plus, "Accept", acceptPendingIntent) // #0
                .addAction(R.drawable.account_remove, "Reject", rejectPendingIntent)  // #1
                .setWhen(0) //important so the whole message will be shown
                .setAutoCancel(true);

        setNotificationSenderImage(fromId, builder);
        return builder;
    }

    private NotificationCompat.Builder setFriendAcceptNotification(String title, String body, String from, String fromId,
                                                                   int notification_id) {
        //the press on notification answer
        PendingIntent regularPendingIntent = setPressOnNotificationIntent(notification_id);
        //the yes answer
        PendingIntent acceptPendingIntent = setPendingIntent(title, body, fromId, true, notification_id);
        //the no answer
        PendingIntent rejectPendingIntent = setPendingIntent(title, body, fromId, false, notification_id);

        //This is the proper solution for dismiss the dialog aka the dismiss a

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                // Set the intent that will fire when the user taps the notification
//                .setContentIntent(regularPendingIntent)
                .setWhen(0) //important so the whole message will be shown
                .setAutoCancel(true);

        setNotificationSenderImage(fromId, builder);
        return builder;
    }


    private NotificationCompat.Builder setFriendRequestNotification(String title, String body, String from, String fromId,
                                                                    int notification_id) {
        //the press on notification answer
        PendingIntent regularPendingIntent = setPressOnNotificationIntent(notification_id);
        //the yes answer
        PendingIntent acceptPendingIntent = setPendingIntent(title, body, fromId, true, notification_id);
        //the no answer
        PendingIntent rejectPendingIntent = setPendingIntent(title, body, fromId, false, notification_id);

        //This is the proper solution for dismiss the dialog aka the dismiss a

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .addAction(R.drawable.account_plus, "Accept", acceptPendingIntent)
                .addAction(R.drawable.account_remove, "Reject", rejectPendingIntent)
                // Set the intent that will fire when the user taps the notification
                //.setContentIntent(regularPendingIntent)
                .setWhen(0) //important so the whole message will be shown
                .setAutoCancel(true);


        setNotificationSenderImage(fromId, builder);
        return builder;
    }

    private PendingIntent setGameAnswerPendingIntent(String title, String body, String from, String fromId, String roomKey, boolean b
            , int notification_id) {
        //the yes answer
        Intent acceptIntent = new Intent(getApplicationContext(), NotificationActivity.class);
        acceptIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        acceptIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        acceptIntent.putExtra("gameInvite", true);
        acceptIntent.putExtra("roomKey", roomKey);
        acceptIntent.putExtra("answer", b);
        acceptIntent.putExtra("title", title);
        acceptIntent.putExtra("body", body);
        acceptIntent.putExtra("from", from);
        acceptIntent.putExtra("notification_id", notification_id);
        int rand = new Random().nextInt('Z' - 'A');
        String xId = String.valueOf(rand);
        acceptIntent.putExtra("x_id", xId);
        acceptIntent.setAction(xId);
        return PendingIntent.getActivity(getApplicationContext(), 1, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void setNotificationSenderImage(String from, NotificationCompat.Builder builder) {
        CacheHandler cacheHandler = new CacheHandler(getApplicationContext());
        if (cacheHandler.isUsingProfilePicture()) {
            cacheHandler.getProfilePicture(from, builder);
//            Bitmap icon=cacheHandler.ShrinkBitmap(profilePicture,64,64);
//            builder.setLargeIcon(icon);
        }
    }

    private PendingIntent setPendingIntent(String title, String body, String from, boolean b, int notification_id) {
        //the yes answer
        Intent acceptIntent = new Intent(getApplicationContext(), NotificationActivity.class);
        acceptIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        acceptIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        acceptIntent.putExtra("answer", b);
        acceptIntent.putExtra("title", title);
        acceptIntent.putExtra("body", body);
        acceptIntent.putExtra("from", from);
        int rand = new Random().nextInt('Z' - 'A');
        String xId = String.valueOf(rand);
        acceptIntent.putExtra("x_id", xId);
        acceptIntent.putExtra("notification_id", notification_id);
        acceptIntent.setAction(xId);
        return PendingIntent.getActivity(getApplicationContext(), 1, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent setPressOnNotificationIntent(int notification_id) {
        //the click on the notification
        Intent regularIntent = new Intent(getApplicationContext(), NotificationActivity.class);
        regularIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int rand = new Random().nextInt('Z' - 'A');
        String xId = String.valueOf(rand);
        regularIntent.putExtra("x_id", xId);
        regularIntent.putExtra("notification_id", notification_id);
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

    private String getUid() {
        FirebaseAuth fireBaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = fireBaseAuth.getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (account != null && currentUser == null) {
            return account.getId();
        } else if (currentUser != null) {
            return fireBaseAuth.getUid();
        } else {
            return accessToken.getUserId();
        }
    }

}
