package com.tju.twist.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.tju.twist.atinote.MainActivity;
import com.tju.twist.atinote.R;
import com.tju.twist.atinote.WeekViewActivity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Twist on 2015/9/16.
 */
public class OnMonthChangeListener implements WeekView.MonthChangeListener {
    private List<WeekViewEvent> events = new ArrayList<WeekViewEvent>();
    private WeekViewActivity wAct;
    private WeekView weekView;

    public OnMonthChangeListener(WeekViewActivity wAct, WeekView weekView){
        this.wAct = wAct;
        this.weekView = weekView;
    }
    @Override
    public List<WeekViewEvent> onMonthChange(int i, int i1) {
        SQLiteDatabase database = wAct.getDatabaseHelper().getReadableDatabase();
        Cursor c = database.rawQuery("select * from event", null);
        while (c.moveToNext()){
            String time = c.getString(c.getColumnIndex("time"));
            int colorPos = c.getInt(c.getColumnIndex("color"));
            int color = getTheColor(colorPos);

            Calendar startTime = Calendar.getInstance();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date d = new Date();
            try {
                d = (Date)dateFormat.parse(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            startTime.setTime(d);
            addNewEvent(startTime,"", color);
        }
        c.close();
        return events;
    }

    private int getTheColor(int cPos){
        return wAct.getResources().getColor(wAct.getColorPicker().getColor(ColorPicker.colorStringPos, cPos));
    }

    public void addNewEvent(Calendar startTime, String name, int color){
        Date startDate = startTime.getTime();
        int minutes = startDate.getMinutes();
        if (minutes < 30){
            minutes = 0;
        }else {
            minutes = 30;
        }
        startDate.setMinutes(minutes);
        startDate.setSeconds(0);
        startTime.setTime(startDate);

        Calendar endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.MINUTE, 30);
        WeekViewEvent event = new WeekViewEvent(1, name, startTime, endTime);
        event.setColor(color);

        events.add(event);
    }

}
