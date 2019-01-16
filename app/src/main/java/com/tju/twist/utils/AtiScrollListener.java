package com.tju.twist.utils;

import com.alamkanak.weekview.WeekView;
import com.tju.twist.atinote.MainActivity;

import java.util.Calendar;

/**
 * Created by Twist on 2015/9/19.
 */
public class AtiScrollListener implements WeekView.ScrollListener {
    MainActivity mAct;

    public AtiScrollListener(MainActivity mAct){
        this.mAct = mAct;
    }

    @Override
    public void onFirstVisibleDayChanged(Calendar calendar, Calendar calendar1) {
        mAct.setCurrentCalendar(calendar);
    }
}
