package com.tju.twist.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tju.twist.atinote.MainActivity;
import com.tju.twist.atinote.ScreenLockActivity;

/**
 * Created by Twist on 2015/9/22.
 */
public class ScreenListener extends BroadcastReceiver{
    private MainActivity mAct;
    private final String TAG = "AtiLog";

    public ScreenListener(MainActivity mAct){
        this.mAct = mAct;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent();
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setClass(context, ScreenLockActivity.class);
        context.startActivity(i);
    }
}



















