package com.tju.twist.utils;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.alamkanak.weekview.WeekView;
import com.tju.twist.atinote.MainActivity;
import com.tju.twist.atinote.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Twist on 2015/9/10.
 */
public class AntiEmptyViewClickListener implements WeekView.EmptyViewClickListener {

    private AntiMonthChangeListener antiMonthChangeListener;
    private MainActivity mAct;
    private SQLiteDatabase antidb;
    public AntiEmptyViewClickListener(AntiMonthChangeListener amcl, MainActivity mAct){
        this.antiMonthChangeListener = amcl;
        this.mAct = mAct;
    }
    @Override
    public void onEmptyViewClicked(Calendar calendar) {
        antidb = mAct.getDatabaseHelper().getWritableDatabase();

        int colorPos = mAct.getCurrentColorPos();
        if(colorPos == -1){
            Toast.makeText(mAct, "阿嚏！亲，请先选择颜色！", Toast.LENGTH_SHORT).show();
        }
        else {
//            antiMonthChangeListener.addNewEvent(calendar, "", mAct.getResources().getColor(mAct.getColorPicker().getColor(ColorPicker.colorStringPos, colorPos)));
            switchSelection(calendar, colorPos);
            mAct.getWeekView().notifyDatasetChanged();
        }
    }

    private void insertIntoDB(String time, int color){
        ContentValues cv = new ContentValues();
        cv.put("time", time);
        cv.put("color", color);
        antidb.insert("event", null, cv);
        antidb.close();
    }

    private void switchSelection(Calendar calendar, int c){
        Date startDate = calendar.getTime();
        startDate.setMinutes(startDate.getMinutes() < 30 ? 0 : 30);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String time = dateFormat.format(startDate);

        insertIntoDB(time, c);
    }

}











