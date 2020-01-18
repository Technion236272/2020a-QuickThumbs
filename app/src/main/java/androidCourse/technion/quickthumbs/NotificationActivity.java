package androidCourse.technion.quickthumbs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;

import androidCourse.technion.quickthumbs.database.FriendsDatabaseHandler;


public class NotificationActivity extends AppCompatActivity {
    private static final String TAG = NotificationActivity.class.getSimpleName();
    private Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        Bundle extras = getIntent().getExtras();

        Log.d(TAG, "NotificationActivity - onCreate - extras: " + extras);

        if (extras == null) {
            context.finish();
            return;
        }
        HashMap<String, Object> messageValues = new HashMap<>();
        for (String key : extras.keySet()) {
            Object value = extras.get(key);
            messageValues.put(key, value);
            Log.d(TAG, "Extras received at onNewIntent:  Key: " + key + " Value: " + value);
        }
        if(messageValues.get("notification_id")!= null){
            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel((Integer)messageValues.get("notification_id"));
        }
        if (messageValues.get("gameInvite") != null) {
            boolean answer = (boolean) extras.get("answer");
            if (answer) {
                Intent goToMainScreen = new Intent(context, MainPager.class);
                goToMainScreen.putExtra("roomKey",messageValues.get("roomKey").toString());
                goToMainScreen.putExtra("answer", (Boolean) messageValues.get("answer"));
                goToMainScreen.putExtra("from", messageValues.get("from").toString());
                startActivity(goToMainScreen);
                return;
            } else {
                //reject game
                finish();
                return;
            }
        }

        FriendsDatabaseHandler friendsDatabaseHandler = new FriendsDatabaseHandler();
        if (messageValues.get("answer") != null && messageValues.get("gameInvite") == null) {
            boolean answer = (boolean) extras.get("answer");
            if (answer) {
                friendsDatabaseHandler.addFriend((String) extras.getString("from"), getApplicationContext());
                Log.d(TAG, "friend is " + extras.getString("from"));
                Intent intent = new Intent(this, MainPager.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                friendsDatabaseHandler.removeRequest(extras.getString("from"), getApplicationContext());
                Intent intent = new Intent(this, MainPager.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
        //if dismiss button was pressed
        if (messageValues.get("dismiss") != null && ((Boolean) messageValues.get("dismiss") ) ) {
            Intent intent = new Intent(this, MainPager.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
//        if (messageValues.get("from") != null ) {
//            showNotificationInADialog((String) messageValues.get("from"));
//        }
        // if the message body was pressed

//        RemoteMessage msg = (RemoteMessage) extras.get("msg");
//        if (msg == null) {
//            context.finish();
//            return;
//        }
//
//        RemoteMessage.Notification notification = msg.getNotification();
//
//        if (notification == null) {
//            context.finish();
//            return;
//        }
//
//        String dialogMessage;
//        try {
//            dialogMessage = notification.getBody();
//        } catch (Exception e) {
//            context.finish();
//            return;
//        }
//
//        String dialogTitle = notification.getTitle();
//        if (dialogTitle == null || dialogTitle.length() == 0) {
//            dialogTitle = "";
//        }
//
//
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper());
//        builder.setTitle(dialogTitle);
//        builder.setMessage(dialogMessage);
//        builder.setPositiveButton("accept", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                dialog.cancel();
//            }
//        });
//        AlertDialog alert = builder.create();
//        alert.show();
//
    }


    @Override
    public void onNewIntent(Intent intent) {
        //called when a new intent for this class is created.
        // The main case is when the app was in background, a notification arrives to the tray, and the user touches the notification

        super.onNewIntent(intent);
        setIntent(intent);

//        Log.d(TAG, "onNewIntent - starting");
//        Bundle extras = intent.getExtras();
//        if (extras != null) {
//            for (String key : extras.keySet()) {
//                Object value = extras.get(key);
//                Log.d(TAG, "Extras received at onNewIntent:  Key: " + key + " Value: " + value);
//            }
//            String title = extras.getString("title");
//            String message = extras.getString("body");
//            String from = extras.getString("from");
//            if (message != null && message.length() > 0) {
//                getIntent().removeExtra("body");
//                showNotificationInADialog(from);
//            }
//        }
    }


    private void showNotificationInADialog(final String from) {

        // show a dialog with the provided title and message
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Friend Request");
//        builder.setMessage(from+ " has asked to be your friend ");
//        builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                dialog.cancel();
//                FriendsDatabaseHandler friendsDatabaseHandler = new FriendsDatabaseHandler();
//                friendsDatabaseHandler.addFriend(from, getApplicationContext());
//            }
//        });
//        builder.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                dialog.cancel();
//                FriendsDatabaseHandler friendsDatabaseHandler = new FriendsDatabaseHandler();
//                friendsDatabaseHandler.removeRequest(from, getApplicationContext());
//            }
//        });
//        builder.setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                dialog.cancel();
//            }
//        });
//        AlertDialog alert = builder.create();
//        alert.show();
    }


}