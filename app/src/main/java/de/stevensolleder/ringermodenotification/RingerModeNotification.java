package de.stevensolleder.ringermodenotification;



import android.app.Application;
import android.content.Context;



public class RingerModeNotification extends Application
{
    private static Context applicationContext;



    public void onCreate()
    {
        super.onCreate();
        RingerModeNotification.applicationContext = getApplicationContext();
    }


    public static Context getAppContext()
    {
        return RingerModeNotification.applicationContext;
    }
}
