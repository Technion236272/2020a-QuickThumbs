package androidCourse.technion.quickthumbs.Utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class HandleBroadcastReceiver extends BroadcastReceiver {
    private String CHANNEL_ID = "F_REQ";
    @Override
    public void onReceive(Context context, Intent intent) {
//        Toast.makeText(context, "Notification Dialog Closed", Toast.LENGTH_LONG).show();
//        Log.d("Notification:", "Notification Dialog Closed");
//        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        manager.cancel(0);
//
//        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
//        NotificationCompat.Builder mb = new NotificationCompat.Builder(context, CHANNEL_ID);
//        mb.setContentIntent(resultPendingIntent);

        int notificationId = intent.getIntExtra("notificationId", 1);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);
    }
}