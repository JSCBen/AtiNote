package com.tju.twist.atinote;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.alamkanak.weekview.WeekView;
import com.andexert.library.RippleView;
import com.tju.twist.database.EventDatabase;
import com.tju.twist.database.SummaryDatabase;
import com.tju.twist.utils.AntiEmptyViewClickListener;
import com.tju.twist.utils.AntiEventClickListener;
import com.tju.twist.utils.AntiEventLongPressListener;
import com.tju.twist.utils.AntiMonthChangeListener;
import com.tju.twist.utils.AtiScrollListener;
import com.tju.twist.utils.ColorPicker;
import com.umeng.scrshot.adapter.UMAppAdapter;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.sensor.UMSensor;
import com.umeng.socialize.sensor.beans.ShakeMsgType;
import com.umeng.socialize.sensor.controller.UMShakeService;
import com.umeng.socialize.sensor.controller.impl.UMShakeServiceFactory;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.update.UmengUpdateAgent;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import at.markushi.ui.CircleButton;

public class MainActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener{
//    private ScreenListener screenListener;

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    private Button datePicker, btnWrite, btnPin;
    private TextView textLogInfo;
    private Calendar currentCalendar = Calendar.getInstance();
    //initialize navigation header btns
    private ImageView btnSetting, btnShake;
//    private RadioGroup radioGroup;
    private int currentColor = 0;
    private static int write = 0;
//    private CoordinatorLayout layoutMain;
    private CircleButton btnColor0, btnColor1, btnColor2, btnColor3, btnColor4;
    private CircleButton[] btnGroup;
    private static int circleButtonSize;

    private int mCurrentSelectedPosition = 0;

    private static final String PREFERENCES_FILE = "yourappname_settings";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";


    private boolean mUserLearnedDrawer;
    private WeekView weekView;

    //Database
    private EventDatabase antidb;
    private SummaryDatabase sumdb;

    //colorPicker
    private ColorPicker colorPicker;
    private static int colorStringPos = 0;

    //------------------------------------------------------------------------------
    private static boolean isLogIn = false;
    private static int logPos = -1;
    private final int SINA = 0, QQ = 1;

    //notify the exist information
    private long mExitTime;

    //Shake to share

    private UMShakeService mShakeController = UMShakeServiceFactory.getShakeService("write.your.content");
    private UMSensor.OnSensorListener mSensorListener = new UMSensor.OnSensorListener() {
        @Override
        public void onActionComplete(SensorEvent sensorEvent) {
            Toast.makeText(MainActivity.this, "摇一摇分享记录", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onButtonClick(UMSensor.WhitchButton button) {
            if (button == UMSensor.WhitchButton.BUTTON_CANCEL) {
                Toast.makeText(MainActivity.this, "取消分享", Toast.LENGTH_SHORT).show();
            } else {
                // 分享中, ( 用户点击了分享按钮 )
            }
        }

        @Override
        public void onStart() {}

        @Override
        public void onComplete(SHARE_MEDIA share_media, int i, SocializeEntity socializeEntity) {
            Toast.makeText(MainActivity.this, "分享进行中", Toast.LENGTH_SHORT).show();
        }
    };
    //------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpToolbar();
        setUpSetting(this);
        setUpColorPicker();

        //initiate mDrawerLayout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.nav_drawer);
        mUserLearnedDrawer = Boolean.valueOf(readSharedSetting(this, PREF_USER_LEARNED_DRAWER, "false"));
        setUpNavDrawer();

        //initiate mNavigationView
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                switch (menuItem.getItemId()) {
                    case R.id.navigation_item_1:
                        mCurrentSelectedPosition = 0;
                        mDrawerLayout.closeDrawers();
                        return true;
                    case R.id.navigation_item_2:
                        startActivity(new Intent(MainActivity.this, WeekViewActivity.class));
                        mCurrentSelectedPosition = 1;
                        return true;
                    case R.id.navigation_item_3:
                        startActivity(new Intent(MainActivity.this, DataViewActivity.class));
                        mCurrentSelectedPosition = 2;

                    default:
                        return true;
                }
            }
        });


        //initialize the database
        setUpDatabase(this);
        //initialize the weekView
        setUpWeekView();
        //refresh the weekView and load data
        //initialize the datePicker
        setupDatePicker();
        //initialize buttons
        setupBtns(this);
        //register to WeiXin
//        regToWx();
        UmengUpdateAgent.update(this);
        setUpNotification();
    }

    @Override
    protected void onResume() {
        super.onResume();
        addQQ(MainActivity.this);
        addWeiXin(MainActivity.this);

        UMAppAdapter appAdapter = new UMAppAdapter(MainActivity.this);
        List<SHARE_MEDIA> platforms = new ArrayList<SHARE_MEDIA>();
        platforms.add(SHARE_MEDIA.WEIXIN);
        platforms.add(SHARE_MEDIA.WEIXIN_CIRCLE);
        platforms.add(SHARE_MEDIA.QQ);
        platforms.add(SHARE_MEDIA.QZONE);
        platforms.add(SHARE_MEDIA.SINA);

        mShakeController.setShareContent("AtiNote-记录-积累-改变");
        mShakeController.setShakeMsgType(ShakeMsgType.SCRSHOT);
        mShakeController.registerShakeListender(MainActivity.this, appAdapter, platforms, mSensorListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mShakeController.unregisterShakeListener(MainActivity.this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        antidb.close();
        sumdb.close();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Object mHelperUtils;
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();

            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle bundle = data.getExtras();
        isLogIn = bundle.getBoolean("isLogIn", false);
        logPos = bundle.getInt("logPos", -1);
        int checkPos = bundle.getInt("checkPos", 0);
        if(checkPos != colorStringPos){
            colorStringPos = checkPos;
            colorPicker.setColorStringPos(colorStringPos);
            weekView.notifyDatasetChanged();
            refreshColorButtons();
        }
        setLogText();
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            String msg = "";
            switch (menuItem.getItemId()) {
                case R.id.action_shake:
                    msg += "摇一摇分享记录";
                    break;
                case R.id.action_settings:
                    break;
            }

            if(!msg.equals("")) {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    };

    private void setUpToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        mToolbar.setOnMenuItemClickListener(onMenuItemClick);
    }

    private void setLogText(){
        if(isLogIn){
            switch (logPos){
                case SINA:
                    textLogInfo.setText(this.getResources().getString(R.string.log_pos) + "(新浪微博)");
                    break;
                case QQ:
                    textLogInfo.setText(this.getResources().getString(R.string.log_pos) + "(QQ)");
                    break;
            }
        }else {
            textLogInfo.setText(this.getResources().getString(R.string.log_on));
        }
    }

    private void setUpNavDrawer() {
        textLogInfo = (TextView) findViewById(R.id.text_drawer_log);
        setLogText();

        if (mToolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mToolbar.setNavigationIcon(R.drawable.ic_main);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        if (!mUserLearnedDrawer) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            mUserLearnedDrawer = true;
            saveSharedSetting(this, PREF_USER_LEARNED_DRAWER, "true");
        }
    }

    public static void saveSharedSetting(Context ctx, String settingName, String settingValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(settingName, settingValue);
        editor.apply();
    }
    public static String readSharedSetting(Context ctx, String settingName, String defaultValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return sharedPref.getString(settingName, defaultValue);
    }

    private  void setupBtns(final Activity act){
        btnSetting = (ImageView) findViewById(R.id.btn_setting);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(act, SettingActivity.class);

                Bundle bundle = new Bundle();
                bundle.putBoolean("isLogIn", isLogIn);
                bundle.putInt("logPos", logPos);
                bundle.putInt("checkPos", colorStringPos);
                intent.putExtras(bundle);

                startActivityForResult(intent, 0);
            }
        });

        btnColor0 = (CircleButton) findViewById(R.id.btn_color0);
        btnColor1 = (CircleButton) findViewById(R.id.btn_color1);
        btnColor2 = (CircleButton) findViewById(R.id.btn_color2);
        btnColor3 = (CircleButton) findViewById(R.id.btn_color3);
        btnColor4 = (CircleButton) findViewById(R.id.btn_color4);

        int i = 0;
        CircleButton[] btnColors = new CircleButton[]{btnColor0, btnColor1, btnColor2, btnColor3, btnColor4};
        for (CircleButton btn: btnColors
             ) {
            btn.setColor(this.getResources().getColor(colorPicker.getColor(colorStringPos, i)));
            i++;
        }

        circleButtonSize = btnColor0.getLayoutParams().height;
        btnGroup = new CircleButton[]{btnColor0, btnColor1, btnColor2, btnColor3, btnColor4};
        setBtnsSize();
        setButtonSmaller(btnColor0);

        for (CircleButton btn: btnGroup
             ) {
            btn.setOnClickListener(new ColorButtonListenter());
            btn.setOnLongClickListener(new ColorButtonLongClickListener(act));
        }

        btnWrite = (Button) findViewById(R.id.btn_write);
        RippleView ripple = (RippleView) findViewById(R.id.btn_write_ripple);
        ripple.setRippleColor(colorPicker.getColor(colorStringPos, 0));
        btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write = 1;
            }
        });

        btnPin = (Button) findViewById(R.id.btn_pin);
        btnPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.layout_summary);
                dialog.setTitle("一日小结");

                final EditText editSum, editGet, editSpecial;
                editSum = (EditText) dialog.findViewById(R.id.edit_sum);
                editGet = (EditText) dialog.findViewById(R.id.edit_get);
                editSpecial = (EditText) dialog.findViewById(R.id.edit_special);

                final SQLiteDatabase sumDatabase = sumdb.getWritableDatabase();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                final String time = format.format(currentCalendar.getTime());

                Cursor c = sumDatabase.rawQuery("select * from summary where time = ?", new String[]{time});
                final boolean has = c.moveToFirst();
                if(has) {
//                    c.move(0);
                    editSum.setText(c.getString(c.getColumnIndex("sum")));
                    editGet.setText(c.getString(c.getColumnIndex("get")));
                    editSpecial.setText(c.getString(c.getColumnIndex("special")));
                }

                Button btnYes = (Button) dialog.findViewById(R.id.btn_summary_yes);
                btnYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ContentValues cv = new ContentValues();
                        String where = "time = \'" + time + "\'";
                        cv.put("sum", editSum.getText().toString());
                        cv.put("get", editGet.getText().toString());
                        cv.put("special", editSpecial.getText().toString());
                        if(has){
                            sumDatabase.update("summary", cv, where, null);
                        }else {
                            cv.put("time", time);
                            sumDatabase.insert("summary",null, cv);
                        }
                        dialog.dismiss();
                    }
                });

                Button btnNo = (Button) dialog.findViewById(R.id.btn_summary_no);
                btnNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        btnShake = (ImageView) findViewById(R.id.btn_main_shake);
        btnShake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(act, act.getResources().getString(R.string.shake_to_share), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshColorButtons(){
        int i = 0;
        for (CircleButton btn: btnGroup
             ) {
            btn.setColor(this.getResources().getColor(colorPicker.getColor(colorStringPos, i)));
            i++;
        }
    }

    private void setupDatePicker(){
        datePicker = (Button) findViewById(R.id.date_picker);
        Calendar now = Calendar.getInstance();
        final int year = now.get(Calendar.YEAR);
        final int month = now.get(Calendar.MONTH);
        final int day_month = now.get(Calendar.DAY_OF_MONTH);

        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        MainActivity.this,
                        year,
                        month,
                        day_month
                );
                dpd.show(getFragmentManager(), "DatePickerDialog");
            }
        });
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int monthOfYear, int dayOfMonth) {
        Calendar newCal = Calendar.getInstance();
        newCal.set(year, monthOfYear, dayOfMonth);
        currentCalendar = newCal;
        weekView.goToDate(newCal);
    }

    //initialize the weekView and add listeners to it
    private void setUpWeekView(){
        weekView = (WeekView) findViewById(R.id.weekView);

        AntiMonthChangeListener amcl = new AntiMonthChangeListener(this);
        weekView.setMonthChangeListener(amcl);
        weekView.setOnEventClickListener(new AntiEventClickListener(this));

        AntiEmptyViewClickListener aevcl = new AntiEmptyViewClickListener(amcl, this);
        weekView.setEmptyViewClickListener(aevcl);

        AntiEventLongPressListener aelpl = new AntiEventLongPressListener(this);
        weekView.setEventLongPressListener(aelpl);

        AtiScrollListener asl = new AtiScrollListener(this);
        weekView.setScrollListener(asl);

        weekView.setNumberOfVisibleDays(1);
        weekView.goToHour(getRightHour());

//         Lets change some dimensions to best fit the view.
        weekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        weekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        weekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, getResources().getDisplayMetrics()));
    }

    private class ColorButtonListenter implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.btn_color0:
                    currentColor = 0;
                    setButtonSmaller(btnColor0);
                    break;
                case R.id.btn_color1:
                    currentColor = 1;
                    setButtonSmaller(btnColor1);
                    break;
                case R.id.btn_color2:
                    currentColor = 2;
                    setButtonSmaller(btnColor2);
                    break;
                case R.id.btn_color3:
                    currentColor = 3;
                    setButtonSmaller(btnColor3);
                    break;
                case R.id.btn_color4:
                    currentColor = 4;
                    setButtonSmaller(btnColor4);
                    break;
                default:
                    currentColor = -1;
            }
        }
    }

    private class ColorButtonLongClickListener implements View.OnLongClickListener{
        private Activity act;

        public ColorButtonLongClickListener(Activity act){
            this.act = act;
        }

        @Override
        public boolean onLongClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.btn_color0:
                    Toast.makeText(act, act.getResources().getString(R.string.text_item0), Toast.LENGTH_SHORT).show();
                    break;
                case R.id.btn_color1:
                    Toast.makeText(act, act.getResources().getString(R.string.text_item1), Toast.LENGTH_SHORT).show();
                    break;
                case R.id.btn_color2:
                    Toast.makeText(act, act.getResources().getString(R.string.text_item2), Toast.LENGTH_SHORT).show();
                    break;
                case R.id.btn_color3:
                    Toast.makeText(act, act.getResources().getString(R.string.text_item3), Toast.LENGTH_SHORT).show();
                    break;
                case R.id.btn_color4:
                    Toast.makeText(act, act.getResources().getString(R.string.text_item4), Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    }

    //---------------------------------------------------------------------------
    //get the situation of buttons

    public int getCurrentColorPos(){
        return currentColor;
    }

    public int getCurrentColor(){
        if(currentColor != -1){
            return this.getResources().getColor(colorPicker.getColor(colorStringPos, currentColor));
        }
        else {
            return -1;
        }

    }

    public int getTheColor(int cPos){
        return this.getResources().getColor(colorPicker.getColor(colorStringPos, cPos));
    }

    public int getWriteSituation(){
        return write;
    }

    public WeekView getWeekView(){
        return weekView;
    }

    private void setUpDatabase(Activity act){
        if(antidb == null){
            antidb = new EventDatabase(act);
        }
        if(sumdb == null){
            sumdb = new SummaryDatabase(act);
        }
    }

    public void setWriteSituation(int s){
        write = s;
    }

    //---------------------------------------------------------------------------


    public EventDatabase getDatabaseHelper(){
        return antidb;
    }

    private void setBtnsSize(){
        for (CircleButton btn: btnGroup
             ) {
            btn.setLayoutParams(new FrameLayout.LayoutParams(circleButtonSize, circleButtonSize));
        }
    }

    private void setButtonSmaller(CircleButton btn){

        int smallerSize = (int)(circleButtonSize * 0.8);
        for (CircleButton button: btnGroup
             ) {
            if(btn == button){
                button.setLayoutParams(new FrameLayout.LayoutParams(smallerSize, smallerSize));
            }
            else {
                button.setLayoutParams(new FrameLayout.LayoutParams(circleButtonSize, circleButtonSize));
            }
        }
    }

    public void setCurrentCalendar(Calendar c){
        currentCalendar = c;
    }

    public int getRightHour(){
        Calendar now = Calendar.getInstance();
        int hour = now.getTime().getHours();
        if(hour < 12){
            if(hour >= 6)
                return 6;
            else
                return 0;
        }else {
            return 12;
        }
    }

    //colorPicker function
    //----------------------------------------------------------------------------------
    private void setUpColorPicker(){
        colorPicker = new ColorPicker();
//        colorStringPos = ColorSettingActivity.checkPos;
        colorPicker.setColorStringPos(colorStringPos);
    }

    public ColorPicker getColorPicker(){
        return colorPicker;
    }
    //----------------------------------------------------------------------------------

    private void setUpSetting(Activity act){
        SharedPreferences sp = act.getSharedPreferences("setting", MODE_PRIVATE);
        colorStringPos = sp.getInt("colorPos", 0);
    }

    //--------------------------------------------------------------------------------
    //Add Share Platforms

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

    //-------------------------------------------------------------------------------

    private void setUpNotification(){
        int icon = R.drawable.icon;
        long when = System.currentTimeMillis();
        Notification notification = new Notification(icon, "AtiNote", when);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.layout_notification);
        contentView.setImageViewResource(R.id.notification_icon, icon);
        contentView.setViewVisibility(R.id.layout_notification, View.VISIBLE);
        notification.contentView = contentView;

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.contentIntent = contentIntent;

        notification.flags = Notification.FLAG_NO_CLEAR; //Do not clear the notification;
        notification.defaults = Notification.DEFAULT_LIGHTS; //LED
        notification.defaults = Notification.DEFAULT_VIBRATE; //Vibration
        notification.defaults = Notification.DEFAULT_SOUND; //Sound

        notificationManager.notify(1, notification);
    }

}













