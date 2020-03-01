package com.shid.clip.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.shid.clip.Database.AppDatabase;
import com.shid.clip.Database.ClipEntry;
import com.shid.clip.R;
import com.shid.clip.Utils.StopAutoListenReceiver;
import com.shid.clip.UI.MainActivity;
import com.shid.clip.Utils.AppExecutor;
import com.shid.clip.Utils.Constant;

import java.util.Date;

public class AutoListenService extends Service {

    private static final String GENERAL_CHANNEL = "general channel";
    private Context context;
    private NotificationManager notificationManager;
    private Notification vNotification;
    private NotificationCompat.Builder vNotification1;
    private ClipboardManager mClipboard;
    private ClipboardManager.OnPrimaryClipChangedListener listener;
    // Member variable for the Database
    private AppDatabase mDb;
    public static boolean isServiceRunning = false;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent openActivityIntent = new Intent(context, MainActivity.class);
        openActivityIntent.putExtra("service_on", true);
        openActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        openActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent openActivityPIntent = PendingIntent.getActivity(context, Constant.AUTO_REQUEST_CODE, openActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // FLAG_UPDATE_CURRENT = if the described PendingIntent already exists,
        // then keep it but its replace its extra data with what is in this new Intent.

        Intent stopAutoIntent = new Intent(context, StopAutoListenReceiver.class);
        stopAutoIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        stopAutoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent stopAutoPIntent = PendingIntent.getBroadcast(context, Constant.REQUEST_CODE, stopAutoIntent,0);

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_service);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Icon icon = Icon.createWithResource(context, R.drawable.ic_stop_black);
            Notification.Action.Builder builder = new Notification.Action.Builder(icon, "STOP", stopAutoPIntent);


            vNotification = new Notification.Builder(context, GENERAL_CHANNEL)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(bitmap)
                    .setContentTitle("AutoListen now activated")
                    .setContentText("Press to disable Clipboard Manager")
                    .setContentIntent(openActivityPIntent)
                    .setAutoCancel(true)
                    .addAction(builder.build())
                    .build();

            vNotification.flags = Notification.FLAG_NO_CLEAR;

            notificationManager.notify(Constant.NOTI_IDENTIFIER, vNotification);
            startForeground(Constant.NOTI_IDENTIFIER, vNotification);
        } else{
            NotificationCompat.Action action =
                    new NotificationCompat.Action.Builder(R.drawable.ic_stop_black,
                            "STOP", stopAutoPIntent)
                            .build();


            NotificationCompat.Builder builder2 = new NotificationCompat.Builder(context, GENERAL_CHANNEL);
            Notification mNotification = builder2
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(bitmap)
                    .setContentTitle("AutoListen now activated")
                    .setContentText("Press to disable Clipboard Manager")
                    .setContentIntent(openActivityPIntent)
                    .setAutoCancel(true)
                    .addAction(action)
                    .build();

            mNotification.flags = Notification.FLAG_NO_CLEAR;

            notificationManager.notify(Constant.NOTI_IDENTIFIER, mNotification);
            startForeground(Constant.NOTI_IDENTIFIER, mNotification);
            /*else {//for old devices
            vNotification1 = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(bitmap)
                    .setContentTitle("AutoListen now activated")
                    .setContentText("Press to stop Clipboard Manager")
                    .setContentIntent(openActivityPIntent)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .addAction(R.drawable.ic_stop_black, "STOP", stopAutoPIntent);

            notificationManager.notify(Constant.NOTI_IDENTIFIER, vNotification1.build());


        }
        */
        }

        mClipboard.addPrimaryClipChangedListener(listener);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isServiceRunning = true;
        context = getApplicationContext();
        mClipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
        mDb = AppDatabase.getInstance(getApplicationContext());

        listener = new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                startPrimaryClipChangedListenerDelayThread();
                performClipboardCheck();
            }
        };
    }

    void startPrimaryClipChangedListenerDelayThread() {
        mClipboard.removePrimaryClipChangedListener(listener);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mClipboard.addPrimaryClipChangedListener(listener);
            }
        }, 1000);
    }


    @Override
    public void onDestroy() {
        isServiceRunning = false;
        if (notificationManager != null) {
            notificationManager.cancel(Constant.NOTI_IDENTIFIER);
        }


        if (mClipboard != null && listener != null) {
            this.mClipboard.removePrimaryClipChangedListener(listener);
            Toast.makeText(context, " AutoListen Disabled, reset to use again", Toast.LENGTH_SHORT).show();

        }
        super.onDestroy();
    }

    private void performClipboardCheck() {
        if (mClipboard.hasPrimaryClip()) {
            String copiedClip = mClipboard.getPrimaryClip().getItemAt(0).getText().toString();

            Date date = new Date();
            final ClipEntry clipEntry = new ClipEntry(copiedClip, date, 0);
            AppExecutor.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mDb.clipDao().insertClip(clipEntry);

                }
            });


            Toast.makeText(context, "Clip added", Toast.LENGTH_LONG).show();

        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(GENERAL_CHANNEL,
                    getResources().getString(R.string.general_channel),
                    NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.setShowBadge(true);
            notificationChannel.setLightColor(Color.MAGENTA);
            notificationChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public AutoListenService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
