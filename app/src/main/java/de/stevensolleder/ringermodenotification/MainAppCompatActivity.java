package de.stevensolleder.ringermodenotification;



import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;


public class MainAppCompatActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel currentModeNotificationChannel=new NotificationChannel("1210", this.getString(R.string.notification_mode_channel), NotificationManager.IMPORTANCE_DEFAULT);
            currentModeNotificationChannel.setSound(null, null);
            currentModeNotificationChannel.setVibrationPattern(null);
            currentModeNotificationChannel.setShowBadge(false);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(currentModeNotificationChannel);
        }


        SharedPreferences settingsSharedPreferences=this.getSharedPreferences("settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor settingsSharedPreferencesEditor=settingsSharedPreferences.edit();

        settingsSharedPreferencesEditor.putBoolean("previousDoNotDisturbActivationState", isDoNotDisturbActivated());
        settingsSharedPreferencesEditor.putInt("previousRingerMode", ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).getRingerMode());
        settingsSharedPreferencesEditor.apply();


        setContentView(R.layout.main_activity);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        Switch activatedSwitch=findViewById(R.id.activated_switch);
        activatedSwitch.setChecked(settingsSharedPreferences.getBoolean("activated", false));
        activatedSwitch.setOnCheckedChangeListener(((buttonView, isChecked) ->
        {
            Intent serviceIntent=new Intent(this, RingerModeService.class);

            if(isChecked)
            {
                startService(serviceIntent);
                settingsSharedPreferencesEditor.putBoolean("activated", true);
            }
            else
            {
                stopService(serviceIntent);
                settingsSharedPreferencesEditor.putBoolean("activated", false);
            }
            settingsSharedPreferencesEditor.apply();
        }));

        Switch uppercasedSwitch=findViewById(R.id.uppercased_switch);
        uppercasedSwitch.setChecked(settingsSharedPreferences.getBoolean("uppercased", false));
        uppercasedSwitch.setOnCheckedChangeListener(((buttonView, isChecked) ->
        {
            if(isChecked)
            {
                settingsSharedPreferencesEditor.putBoolean("uppercased", true);
            }
            else
            {
                settingsSharedPreferencesEditor.putBoolean("uppercased", false);
            }
            settingsSharedPreferencesEditor.apply();
        }));

        findViewById(R.id.stevensolleder).setOnClickListener(view ->startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://stevensolleder.de"))));

        findViewById(R.id.isabellwaas).setOnClickListener(view->startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/isabellwaas"))));

        findViewById(R.id.contact).setOnClickListener(view->startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:kontakt@stevensolleder.de"))));

        findViewById(R.id.licences).setOnClickListener(view->
        {
            WebView licencesWebView=new WebView(MainAppCompatActivity.this);
            licencesWebView.setVerticalScrollBarEnabled(false);
            licencesWebView.setHorizontalScrollBarEnabled(false);
            licencesWebView.loadUrl("file:///android_asset/licences.html");

            RelativeLayout contentRelativeLayout=new RelativeLayout(MainAppCompatActivity.this);
            contentRelativeLayout.setGravity(RelativeLayout.CENTER_VERTICAL|RelativeLayout.CENTER_VERTICAL);
            contentRelativeLayout.addView(licencesWebView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            AlertDialog.Builder licencesAlertDialogBuilder=new AlertDialog.Builder(MainAppCompatActivity.this);
            licencesAlertDialogBuilder.setPositiveButton(R.string.ok, (dialogInterface, which)->{});
            AlertDialog licencesAlertDialog = licencesAlertDialogBuilder.create();
            licencesAlertDialog.setView(contentRelativeLayout);


            licencesWebView.setWebViewClient(new WebViewClient()
            {
                @Override
                public void onPageFinished (WebView view, String url)
                {
                    super.onPageFinished(view, url);
                    licencesAlertDialog.show();
                    licencesAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.alpha(Color.WHITE));
                    licencesAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(RingerModeNotification.getAppContext(), R.color.secondaryColor));
                }
            });
        });

        findViewById(R.id.donate).setOnClickListener(view->startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://paypal.stevensolleder.de"))));
    }

    public static boolean isDoNotDisturbActivated()
    {
        NotificationManager mNotificationManager = (NotificationManager) RingerModeNotification.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE);
        return mNotificationManager.getCurrentInterruptionFilter()!=NotificationManager.INTERRUPTION_FILTER_ALL;
    }
}