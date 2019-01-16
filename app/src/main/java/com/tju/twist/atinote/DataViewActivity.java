package com.tju.twist.atinote;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.SensorEvent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.tju.twist.database.EventDatabase;
import com.tju.twist.utils.ColorPicker;
import com.tju.twist.utils.ShareMesListener;
import com.umeng.scrshot.adapter.UMAppAdapter;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sensor.UMSensor;
import com.umeng.socialize.sensor.beans.ShakeMsgType;
import com.umeng.socialize.sensor.controller.UMShakeService;
import com.umeng.socialize.sensor.controller.impl.UMShakeServiceFactory;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.BarModel;
import org.eazegraph.lib.models.PieModel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import at.markushi.ui.CircleButton;

public class DataViewActivity extends AppCompatActivity {
    private Button btnDataView0, btnDataView1;
    private ImageView btnBack, btnShake;
    private TextView textViewBar;
    private TextView textColorView;
    private ViewSwitcher viewSwitcher;
    private PieChart pieChart;
    private BarChart barChart;
    private EventDatabase antidb;

    private ColorPicker colorPicker;

    private static int pie_pos = 0;
    private int bar_pos = 0;

    //------------------------------------------------------------------------------
    //Shake to share
    private UMShakeService mShakeController = UMShakeServiceFactory.getShakeService("write.your.content");
    private UMSensor.OnSensorListener mSensorListener = new UMSensor.OnSensorListener() {
        @Override
        public void onActionComplete(SensorEvent sensorEvent) {
            Toast.makeText(DataViewActivity.this, "摇一摇分享记录", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onButtonClick(UMSensor.WhitchButton button) {
            if (button == UMSensor.WhitchButton.BUTTON_CANCEL) {
                Toast.makeText(DataViewActivity.this, "取消分享", Toast.LENGTH_SHORT).show();
            } else {
                // 分享中, ( 用户点击了分享按钮 )
            }
        }

        @Override
        public void onStart() {}

        @Override
        public void onComplete(SHARE_MEDIA share_media, int i, SocializeEntity socializeEntity) {
            Toast.makeText(DataViewActivity.this, "分享进行中", Toast.LENGTH_SHORT).show();
        }
    };
    //------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_view);
        antidb = new EventDatabase(this);
        colorPicker = new ColorPicker();

        setUpViewSwitcher();
        setUpCharts();
        setUpBtns(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        addQQ(DataViewActivity.this);
        addWeiXin(DataViewActivity.this);

        UMAppAdapter appAdapter = new UMAppAdapter(DataViewActivity.this);
        List<SHARE_MEDIA> platforms = new ArrayList<SHARE_MEDIA>();
        platforms.add(SHARE_MEDIA.WEIXIN);
        platforms.add(SHARE_MEDIA.WEIXIN_CIRCLE);
        platforms.add(SHARE_MEDIA.QQ);
        platforms.add(SHARE_MEDIA.QZONE);
        platforms.add(SHARE_MEDIA.SINA);

        mShakeController.setShakeMsgType(ShakeMsgType.SCRSHOT);
        mShakeController.setAsyncTakeScrShot(true);
        mShakeController.setShareContent("AtiNote-记录-积累-改变");
        mShakeController.registerShakeListender(DataViewActivity.this, appAdapter, platforms, mSensorListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mShakeController.unregisterShakeListener(DataViewActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_data_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpViewSwitcher(){
        viewSwitcher = (ViewSwitcher) findViewById(R.id.view_switcher);
    }

    private void setUpBtns(final Activity act){
        btnDataView0 = (Button) findViewById(R.id.btn_dataView0);
        btnDataView1 = (Button) findViewById(R.id.btn_dataView1);
        btnBack = (ImageView) findViewById(R.id.btn_dataView_back);

        View.OnClickListener topButtonsListener  = new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.btn_dataView0){
                    btnDataView0.setSelected(true);
                    btnDataView1.setSelected(false);
                    viewSwitcher.setDisplayedChild(0);
                    setPieText(getStartTime(0), getToday(0));
                    drawPieChart();
                    setPieBtns();
                }
                else{
                    btnDataView0.setSelected(false);
                    btnDataView1.setSelected(true);
                    viewSwitcher.setDisplayedChild(1);
                    setBarText(getStartTime(0), getToday(0));
                    setBarBtns();
                    drawBarChart();
                }
            }
        };

        btnDataView0.setOnClickListener(topButtonsListener);
        btnDataView1.setOnClickListener(topButtonsListener);
        btnDataView0.performClick();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                act.finish();
            }
        });

        btnShake = (ImageView) findViewById(R.id.btn_dataView_shake);
        btnShake.setOnClickListener(new ShareMesListener(act));
    }

    private void setUpCharts(){
        pieChart = (PieChart) findViewById(R.id.piechart);
        barChart = (BarChart) findViewById(R.id.barchart);
    }

    private void drawPieChart(){
        pieChart.clearChart();
        pieChart.addPieSlice(new PieModel("高效工作", getWeekEventCount(0, getStartTime(0)), this.getResources().getColor(colorPicker.getColor(ColorPicker.colorStringPos,0))));
        pieChart.addPieSlice(new PieModel("畅快地玩", getWeekEventCount(1, getStartTime(0)), this.getResources().getColor(colorPicker.getColor(ColorPicker.colorStringPos,1))));
        pieChart.addPieSlice(new PieModel("放松休息", getWeekEventCount(2, getStartTime(0)), this.getResources().getColor(colorPicker.getColor(ColorPicker.colorStringPos,2))));
        pieChart.addPieSlice(new PieModel("强制工作", getWeekEventCount(3, getStartTime(0)), this.getResources().getColor(colorPicker.getColor(ColorPicker.colorStringPos,3))));
        pieChart.addPieSlice(new PieModel("浪费拖延", getWeekEventCount(4, getStartTime(0)), this.getResources().getColor(colorPicker.getColor(ColorPicker.colorStringPos,4))));

        pieChart.startAnimation();
    }

    private void drawBarChart(){
        barChart.clearChart();
        for(int i = 0; i < 7; i++){
            int count = getDayEventCount(bar_pos, getNextDay(i, 0));
            barChart.addBar(new BarModel(getWeekDate(i), count, getTheColor(bar_pos)));
        }
        barChart.startAnimation();
    }

    private int getCountEvent(int colorPos){
        SQLiteDatabase database = antidb.getReadableDatabase();
        Cursor c = database.rawQuery("select * from event where color = ?", new String[]{String.valueOf(colorPos)});
//        database.close();
        return c.getCount();
    }

    private int getDayEventCount(int colorPos, String day){
        SQLiteDatabase database = antidb.getReadableDatabase();
        Cursor c = database.rawQuery("select * from event where time like ? and color = ?", new String[]{day + '%', String.valueOf(colorPos)});
//        database.close();
        return c.getCount();
    }

    private int getWeekEventCount(int colorPos, String startDay){
        SQLiteDatabase database = antidb.getReadableDatabase();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String day = startDay;
        Date d = new Date();
        Calendar calendar = Calendar.getInstance();
        try {
            d = (Date)dateFormat.parse(day);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int count = 0;
        for (int i = 0; i < 7; i++){
            Cursor c = database.rawQuery("select * from event where time like ? and color = ?", new String[]{day + "%",String.valueOf(colorPos)});
            count += c.getCount();
            calendar.setTime(d);
            calendar.add(Calendar.DATE, 1);
            d = calendar.getTime();
            day = dateFormat.format(d);
        }
        return count;
    }
    //-------------------------------------------------------------------------
    //helper functions
    private String getStartTime(int format){
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.DAY_OF_WEEK;
        calendar.add(calendar.DAY_OF_WEEK, 1 - (dayOfWeek));

        Date firstDay = calendar.getTime();
        DateFormat dateFormat;
        if (format == 0){
            dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        }else {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        }

        return dateFormat.format(firstDay);
    }

    private String getToday(int format){
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();

        DateFormat dateFormat;
        if (format == 0){
            dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        }else {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        }
        return dateFormat.format(today);
    }

    private String getNextDay(int n, int format){
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.DAY_OF_WEEK;
        calendar.add(calendar.DAY_OF_WEEK, 1 + n - (dayOfWeek));

        Date nextDay = calendar.getTime();
        DateFormat dateFormat;
        if (format == 0){
            dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        }else {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        }

        return dateFormat.format(nextDay);
    }

    private String getWeekDate(int n){
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.DAY_OF_WEEK;
        calendar.add(calendar.DAY_OF_WEEK, 1 + n - (dayOfWeek));
        int date = calendar.getTime().getDate();
        return String.valueOf(date);
    }
    //------------------------------------------------------------------------

    //------------------------------------------------------------------------
    //initializers
    private void setBarText(String startTime, String endTime){
        textViewBar = (TextView) findViewById(R.id.textView_bar);
        textViewBar.setText(startTime + " —— " + endTime);
    }

    private void setPieText(String startTime, String endTime){
        textViewBar = (TextView) findViewById(R.id.textView_pie);
        textViewBar.setText(startTime + " —— " + endTime);
    }

    private void setBarBtns(){
        CircleButton btn0, btn1, btn2, btn3, btn4;
        textColorView = (TextView) findViewById(R.id.textView_bar_color);
        textColorView.setText("高效工作");
        btn0 = (CircleButton) findViewById(R.id.btn_bar0);
        btn1 = (CircleButton) findViewById(R.id.btn_bar1);
        btn2 = (CircleButton) findViewById(R.id.btn_bar2);
        btn3 = (CircleButton) findViewById(R.id.btn_bar3);
        btn4 = (CircleButton) findViewById(R.id.btn_bar4);

        CircleButton[] btnsGroup = new CircleButton[]{btn0, btn1, btn2, btn3, btn4};
        int i = 0;
        for (CircleButton btn:btnsGroup
             ) {
            btn.setColor(this.getResources().getColor(colorPicker.getColor(ColorPicker.colorStringPos, i)));
            i++;
            btn.setOnClickListener(new ColorButtonListener());
        }
    }

    private void setPieBtns(){
        CircleButton btn0, btn1, btn2, btn3, btn4;
        btn0 = (CircleButton) findViewById(R.id.btn_pie0);
        btn1 = (CircleButton) findViewById(R.id.btn_pie1);
        btn2 = (CircleButton) findViewById(R.id.btn_pie2);
        btn3 = (CircleButton) findViewById(R.id.btn_pie3);
        btn4 = (CircleButton) findViewById(R.id.btn_pie4);

        CircleButton[] btnGroup = new CircleButton[]{btn0, btn1, btn2, btn3, btn4};
        int i = 0;
        for (CircleButton btn: btnGroup
             ) {
            btn.setColor(this.getResources().getColor(colorPicker.getColor(ColorPicker.colorStringPos, i)));
            btn.setOnClickListener(new PieButtonListener(i));
            i++;
        }
    }
    //---------------------------------------------------------------------------

    private class ColorButtonListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.btn_bar0:
                    bar_pos = 0;
                    textColorView.setText("高效工作");
                    break;
                case R.id.btn_bar1:
                    bar_pos = 1;
                    textColorView.setText("畅快地玩");
                    break;
                case R.id.btn_bar2:
                    bar_pos = 2;
                    textColorView.setText("放松休息");
                    break;
                case R.id.btn_bar3:
                    bar_pos = 3;
                    textColorView.setText("强制工作");
                    break;
                case R.id.btn_bar4:
                    bar_pos = 4;
                    textColorView.setText("浪费拖延");
                    break;
                default:
                    bar_pos = 0;
                    textColorView.setText("高效工作");
            }

            drawBarChart();
        }
    }

    private class PieButtonListener implements View.OnClickListener{
        int i = 0;
        public PieButtonListener(int i){
            super();
            this.i = i;
        }
        @Override
        public void onClick(View v) {
            pieChart.setCurrentItem(i);
        }
    }

    private int getTheColor(int cPos){
        return this.getResources().getColor(colorPicker.getColor(ColorPicker.colorStringPos, cPos));
    }

    //----------------------------------------------------------------------------
    //Share Module
    private void addWeiXin(Activity act){
        //register information
        String appID = "wx6f6881f90c8c0e68";
        String appSecret = "c198d54b4ed849bff2051a32440603f4";
        //add WeiXin platform
        UMWXHandler wxHandler = new UMWXHandler(act, appID, appSecret);
        wxHandler.addToSocialSDK();

        // 支持微信朋友圈
        UMWXHandler wxCircleHandler = new UMWXHandler(act, appID,appSecret);
        wxCircleHandler.setToCircle(true);
        wxCircleHandler.addToSocialSDK();
    }

    private void addQQ(Activity act){
        UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(act, "1104803897", "2xog4814oI5TGYb3");
        qqSsoHandler.addToSocialSDK();

        QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(act, "1104803897", "2xog4814oI5TGYb3");
        qZoneSsoHandler.addToSocialSDK();

    }
    //----------------------------------------------------------------------------
}
