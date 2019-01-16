package com.tju.twist.utils;

import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.RectF;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.tju.twist.atinote.MainActivity;
import com.tju.twist.atinote.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Twist on 2015/9/9.
 */
public class AntiEventClickListener implements WeekView.EventClickListener {
    private MainActivity mAct;
    public AntiEventClickListener(MainActivity mAct){
        this.mAct = mAct;
    }
    @Override
    public void onEventClick(final WeekViewEvent weekViewEvent, RectF rectF) {
        int preColor = weekViewEvent.getColor();
        int nowColor = mAct.getCurrentColor();
        int colorPos = mAct.getCurrentColorPos();
        final SQLiteDatabase antidb = mAct.getDatabaseHelper().getWritableDatabase();

        Calendar startTime = weekViewEvent.getStartTime();
        Date startDate = startTime.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        final String time = dateFormat.format(startDate);


        if(nowColor != preColor && mAct.getWriteSituation() == 0){
            if(nowColor == -1){
                Toast.makeText(mAct, "阿嚏！亲，请先选择颜色！", Toast.LENGTH_SHORT).show();
            }
            else{
                weekViewEvent.setColor(nowColor);
                String sql = "update [event] set color = " + colorPos +
                        " where time = " + "\'" + time + "\'" ;
                antidb.execSQL(sql);
                mAct.getWeekView().notifyDatasetChanged();
            }
        }
        else {
            final Dialog dialog = new Dialog(mAct);
            dialog.setContentView(R.layout.layout_add_details);

            dialog.setTitle("Anti简记" + "(" + getEventTitle(weekViewEvent.getStartTime()) + ")");
            String name = "";
            String content = "";

            Cursor c = antidb.rawQuery("select * from event where time = ?",new String[]{time});
            if(c.moveToFirst()) {
                c.move(0);
                name = c.getString(c.getColumnIndex("name"));
                content = c.getString(c.getColumnIndex("content"));
            }

            final EditText editTextName = (EditText) dialog.findViewById(R.id.edit_text_name);
            editTextName.setText(name);
            final EditText editTextContent = (EditText) dialog.findViewById(R.id.edit_text_content);
            editTextContent.setText(content);

            Button yesButton = (Button) dialog.findViewById(R.id.yesButton);
            yesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContentValues updatedValues = new ContentValues();
                    String where = "time = \'" + time + "\'";
                    updatedValues.put("name", editTextName.getText().toString());
                    antidb.update("event", updatedValues, where, null);
                    updatedValues.put("content", editTextContent.getText().toString());
                    antidb.update("event", updatedValues, where, null);
                    weekViewEvent.setName(editTextName.getText().toString());

                    dialog.dismiss();
                }
            });

            Button noButton = (Button) dialog.findViewById(R.id.noButton);
            noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();

            if(mAct.getWriteSituation() == 1){
                mAct.setWriteSituation(0);
            }
        }
    }

    private String getEventTitle(Calendar time) {
        return String.format("%02d:%02d %s/%d", time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), time.get(Calendar.MONTH)+1, time.get(Calendar.DAY_OF_MONTH));
    }

}
