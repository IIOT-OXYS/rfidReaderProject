package com.example.nzar.toyotarfid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by uberm on 9/16/2017.
 */

public class MainActivityStartupReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent startupIntent = new Intent(context, MainActivity.class);
            startupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(startupIntent);
        }
    }
}
