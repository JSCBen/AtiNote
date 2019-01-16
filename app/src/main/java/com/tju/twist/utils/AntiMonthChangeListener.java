package com.tju.twist.utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.tju.twist.atinote.MainActivity;
import com.tju.twist.atinote.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Twist on 2015/9/9.
 */
public class AntiMonthChangeListener implements WeekView.MonthChangeListener{
    private MainActivity mAct;
    private WeekView weekView;
    private int currentYear = 0, currentMonth = 0;
    public AntiMonthChangeListener(MainActivity mAct){
        this.mAct = mAct;
        this.weekView = mAct.getWeekView();
    }
    private List<WeekViewEvent> events = new ArrayList<WeekViewEvent>();
    @Override
    public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        currentYear = newYear;
        currentMonth = newMonth - 1;

        events = refreshWeekView();
        return events;
    }

    public void addNewEvent(Calendar startTime, String name, int color) {
//        events.clear();
        Date startDate = startTime.getTime();
        int minutes = startDate.getMinutes();
        if (minutes < 30){
            minutes = 0;
        } else {
            minutes = 30;
        }
        startDate.setMinutes(minutes);
        startDate.setSeconds(0);
        startTime.setTime(startDate);

        Calendar endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.MINUTE, 29);
        WeekViewEvent event = new WeekViewEvent(1, name, startTime, endTime);
        event.setColor(color);

    }

    public void removeEvent(WeekViewEvent event){
        events.remove(event);
        weekView.notifyDatasetChanged();

        Calendar startTime = event.getStartTime();
        Date startDate = startTime.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        final String time = dateFormat.format(startDate);

        SQLiteDatabase antidb = mAct.getDatabaseHelper().getWritableDatabase();
        String sql = "delete from event where time = " + "\'" + time + "\'" ;
        antidb.execSQL(sql);
    }

    private List<WeekViewEvent> refreshWeekView(){
        List<WeekViewEvent> list = new ArrayList<WeekViewEvent>();
        SQLiteDatabase mydatabase = mAct.getDatabaseHelper().getReadableDatabase();
        Cursor c = mydatabase.rawQuery("select * from event", null);
        while (c.moveToNext()){
            String time = c.getString(c.getColumnIndex("time"));
            String name = c.getString(c.getColumnIndex("name"));
            int colorPos = c.getInt(c.getColumnIndex("color"));
            int color = mAct.getTheColor(colorPos);

            Calendar startTime = Calendar.getInstance();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date d = new Date();
            try {
                d = (Date)dateFormat.parse(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }

//            startTime.setTime(d);
            int minutes = d.getMinutes();
            if (minutes < 30){
                minutes = 0;
            } else {
                minutes = 30;
            }
            d.setMinutes(minutes);
            d.setSeconds(0);
            startTime.setTime(d);
            startTime.set(Calendar.YEAR, currentYear);
            startTime.set(Calendar.MONTH, currentMonth);

            Calendar endTime = (Calendar) startTime.clone();
            endTime.add(Calendar.MINUTE, 29);
            endTime.set(Calendar.MONTH, currentMonth);
            WeekViewEvent event = new WeekViewEvent(1, name, startTime, endTime);
            event.setColor(color);

            list.add(event);
        }
        c.close();
        return list;
    }

}