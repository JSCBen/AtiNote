package com.tju.twist.utils;

import android.app.Dialog;
import android.graphics.RectF;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.tju.twist.atinote.MainActivity;
import com.tju.twist.atinote.R;

/**
 * Created by Twist on 2015/9/17.
 */
public class AntiEventLongPressListener implements WeekView.EventLongPressListener {
    private MainActivity mAct;

    public AntiEventLongPressListener(MainActivity mAct){
        this.mAct = mAct;
    }

    @Override
    public void onEventLongPress(WeekViewEvent weekViewEvent, RectF rectF) {
//        Toast.makeText(mAct, "hello", Toast.LENGTH_SHORT).show();

        final WeekViewEvent event = weekViewEvent;
        final Dialog dialog = new Dialog(mAct);
        dialog.setContentView(R.layout.layout_make_sure);
        dialog.setTitle("Ati提醒");

        Button yesButton = (Button) dialog.findViewById(R.id.makeSure_yes);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AntiMonthChangeListener)mAct.getWeekView().getMonthChangeListener()).removeEvent(event);
                dialog.dismiss();
            }
        });

        Button noButton = (Button) dialog.findViewById(R.id.makeSure_no);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
