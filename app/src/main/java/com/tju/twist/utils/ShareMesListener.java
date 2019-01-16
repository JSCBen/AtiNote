package com.tju.twist.utils;

import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import com.tju.twist.atinote.R;

/**
 * Created by Twist on 2015/9/24.
 */
public class ShareMesListener implements View.OnClickListener {
    private Activity act;
    public ShareMesListener(Activity act){
        this.act = act;
    }
    @Override
    public void onClick(View v) {
        Toast.makeText(act, act.getResources().getString(R.string.shake_to_share), Toast.LENGTH_SHORT).show();
    }
}
