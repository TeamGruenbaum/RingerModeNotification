package de.stevensolleder.ringermodenotification;



import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;



public class RingerModeService extends Service
{
    SharedPreferences settingsSharedPreferences;

    private int currentRingerModeIconId;
    private String currentRingerMode;
    private final int persistendNotificationId=1210;
    private boolean lastCallSpecialCase;

    private final BroadcastReceiver ringerModerChangedBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            checkRingerMode();
            createAndSendNotification();

            SharedPreferences.Editor settingsSharedPreferencesEditor=settingsSharedPreferences.edit();
            if(intent.getAction().contentEquals("android.media.RINGER_MODE_CHANGED"))
            {
                settingsSharedPreferencesEditor.putInt("previousRingerMode", ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).getRingerMode());

            }
            else
            {
                settingsSharedPreferencesEditor.putBoolean("previousDoNotDisturbActivationState", MainAppCompatActivity.isDoNotDisturbActivated());
            }
            settingsSharedPreferencesEditor.apply();
        }
    };



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        settingsSharedPreferences=this.getSharedPreferences("settings", Context.MODE_PRIVATE);

        currentRingerModeIconId=R.drawable.ic_mobile_off;
        currentRingerMode=this.getString(R.string.normal_mode);

        lastCallSpecialCase=false;


        IntentFilter ringerModerChangedIntentFilter = new IntentFilter();
        ringerModerChangedIntentFilter.addAction("android.app.action.INTERRUPTION_FILTER_CHANGED");
        ringerModerChangedIntentFilter.addAction("android.media.RINGER_MODE_CHANGED");
        registerReceiver(ringerModerChangedBroadcastReceiver, ringerModerChangedIntentFilter);


        checkRingerMode();
        startForeground(persistendNotificationId, createNotification());

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy()
    {
        unregisterReceiver(ringerModerChangedBroadcastReceiver);

        super.onDestroy();
    }


    private void checkRingerMode()
    {
        String doNotDisturb="";

        if (lastCallSpecialCase)
        {
            lastCallSpecialCase=false;

            return;
        }

        if(settingsSharedPreferences.getBoolean("previousDoNotDisturbActivationState", true)==false && MainAppCompatActivity.isDoNotDisturbActivated()==true)
        {
            lastCallSpecialCase=true;
        }
        else
        {
            switch(((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)).getCurrentInterruptionFilter())
            {
                case NotificationManager.INTERRUPTION_FILTER_ALL: break;
                case NotificationManager.INTERRUPTION_FILTER_NONE:
                case NotificationManager.INTERRUPTION_FILTER_PRIORITY:
                case NotificationManager.INTERRUPTION_FILTER_ALARMS:
                case NotificationManager.INTERRUPTION_FILTER_UNKNOWN: doNotDisturb=" ("+this.getString(R.string.do_not_disturb_mode)+")"; break;
            }

            switch(((AudioManager) getSystemService(Context.AUDIO_SERVICE)).getRingerMode())
            {
                case AudioManager.RINGER_MODE_NORMAL: currentRingerModeIconId=R.drawable.ic_music_note; currentRingerMode=writeInUppercase(this.getString(R.string.normal_mode)); break;
                case AudioManager.RINGER_MODE_VIBRATE: currentRingerModeIconId=R.drawable.ic_vibration; currentRingerMode=writeInUppercase(this.getString(R.string.vibration_mode)+doNotDisturb); break;
                case AudioManager.RINGER_MODE_SILENT: currentRingerModeIconId=R.drawable.ic_mobile_off; currentRingerMode=writeInUppercase(this.getString(R.string.silent_mode)+doNotDisturb); break;
            }
        }
    }

    private Notification createNotification()
    {
        return new NotificationCompat.Builder(this, "1210")
                .setSmallIcon(currentRingerModeIconId)
                .setContentTitle(currentRingerMode)
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                .setAutoCancel(false)
                .setOngoing(true)
                .setShowWhen(false)
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainAppCompatActivity.class), 0))
                .build();
    }

    private void createAndSendNotification()
    {
        NotificationManagerCompat.from(this).notify(persistendNotificationId, createNotification());
    }

   private String writeInUppercase(String text)
   {
       return settingsSharedPreferences.getBoolean("uppercased", false)?text.toUpperCase():text;
   }
}