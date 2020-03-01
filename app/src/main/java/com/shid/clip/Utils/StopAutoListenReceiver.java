package com.shid.clip.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.shid.clip.Service.AutoListenService;

public class StopAutoListenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        context.getApplicationContext().stopService(new Intent(context, AutoListenService.class));
        SharedPref sharedPref = new SharedPref(context.getApplicationContext());
        sharedPref.setSwitch(false);
    }
}
