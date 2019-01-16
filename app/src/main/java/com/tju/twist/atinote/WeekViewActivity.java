package com.tju.twist.atinote;

import android.app.Activity;
import android.hardware.SensorEvent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.alamkanak.weekview.WeekView;
import com.tju.twist.database.EventDatabase;
import com.tju.twist.utils.ColorPicker;
import com.tju.twist.utils.OnMonthChangeListener;

import com.tju.twist.utils.ShareMesListener;
import com.umeng.scrshot.adapter.UMAppAdapter;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.sensor.UMSensor;
import com.umeng.socialize.sensor.beans.ShakeMsgType;
import com.umeng.socialize.sensor.controller.UMShakeService;
import com.umeng.socialize.sensor.controller.impl.UMShakeServiceFactory;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;

import java.util.ArrayList;
import java.util.List;

public class WeekViewActivity extends AppCompatActivity {
    private WeekView weekView;
    private ImageView backBtn, btnShare;
    private EventDatabase antidb;
    private ColorPicker colorPicker;
    private boolean checkPosChanged = false;

    //------------------------------------------------------------------------------
    //Shake to share
    private UMShakeService mShakeController = UMShakeServiceFactory.getShakeService("write.your.content");
    private UMSensor.OnSensorListener mSensorListener = new UMSensor.OnSensorListener() {
        @Override
        public void onActionComplete(SensorEvent sensorEvent) {
            Toast.makeText(WeekViewActivity.this, "摇一摇分享记录", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onButtonClick(UMSensor.WhitchButton button) {
            if (button == UMSensor.WhitchButton.BUTTON_CANCEL) {
                Toast.makeText(WeekViewActivity.this, "取消分享", Toast.LENGTH_SHORT).show();
            } else {
                // 分享中, ( 用户点击了分享按钮 )
            }
        }

        @Override
        public void onStart() {}

        @Override
        public void onComplete(SHARE_MEDIA share_media, int i, SocializeEntity socializeEntity) {
            Toast.makeText(WeekViewActivity.this, "分享进行中", Toast.LENGTH_SHORT).show();
        }
    };
    //------------------------------------------------------------------------------


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_view);

//        checkPosChanged = this.getIntent().getBooleanExtra("checkPosChanged", false);
//        if()

        antidb = new EventDatabase(this);
        setUpColorPicker();
        setUpWeekView();
        setUpBtns(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addQQ(WeekViewActivity.this);
        addWeiXin(WeekViewActivity.this);

        UMAppAdapter appAdapter = new UMAppAdapter(WeekViewActivity.this);
        List<SHARE_MEDIA> platforms = new ArrayList<SHARE_MEDIA>();
        platforms.add(SHARE_MEDIA.WEIXIN);
        platforms.add(SHARE_MEDIA.WEIXIN_CIRCLE);
        platforms.add(SHARE_MEDIA.QQ);
        platforms.add(SHARE_MEDIA.QZONE);
        platforms.add(SHARE_MEDIA.SINA);

        mShakeController.setShareContent("AtiNote-记录-积累-改变");
        mShakeController.setShakeMsgType(ShakeMsgType.SCRSHOT);
        mShakeController.registerShakeListender(WeekViewActivity.this, appAdapter, platforms, mSensorListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        weekView.notifyDatasetChanged();
        mShakeController.unregisterShakeListener(WeekViewActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_week_view, menu);
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

    private void setUpWeekView(){

        weekView = (WeekView) findViewById(R.id.weekView0);
        weekView.setNumberOfVisibleDays(7);
        int h = (int)(weekView.getHourHeight() * 0.5);
        weekView.setHourHeight(h);
        weekView.goToToday();

        OnMonthChangeListener mcl = new OnMonthChangeListener(this, weekView);
        weekView.setMonthChangeListener(mcl);

        // Lets change some dimensions to best fit the view.
        weekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
        weekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 8, getResources().getDisplayMetrics()));
        weekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
    }

    private void setUpBtns(final Activity act){
        backBtn = (ImageView) findViewById(R.id.btn_weekView_back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                act.finish();
            }
        });

        btnShare = (ImageView) findViewById(R.id.btn_weekView_share);
        btnShare.setOnClickListener(new ShareMesListener(act));
    }

    private void setUpColorPicker(){
        colorPicker = new ColorPicker();
    }

    public EventDatabase getDatabaseHelper(){
        return antidb;
    }

    public ColorPicker getColorPicker(){
        return colorPicker;
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













