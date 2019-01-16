package com.tju.twist.atinote;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.fb.FeedbackAgent;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners;
import com.umeng.socialize.exception.SocializeException;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.update.UmengUpdateAgent;

import java.util.Map;
import java.util.Set;

public class SettingActivity extends AppCompatActivity {

    private ImageView btnSettingBack, btnWX, btnQQ, btnSina, imgUser;
    private TextView textLog, textUser;
    private LinearLayout btnLogOn, btnSetColor, btnDataSync, btnFeedback, btnAboutUs, btnUpdate;
    private boolean isLogIn = false;
    private int logPos = -1;
    private static int checkPos = 0;
    private final int SINA = 0, QQ = 1;

    //------------------------------------------------------------------------------
    //Log Platform
    UMSocialService mController = UMServiceFactory.getUMSocialService("com.umeng.login");
    //------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Bundle bundle = this.getIntent().getExtras();
        isLogIn = bundle.getBoolean("isLogIn", false);
        logPos = bundle.getInt("logPos", -1);
        checkPos = bundle.getInt("checkPos", 0);

        setUpBtns(this);
        setUpTextViews();
        addQQ(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setting, menu);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            returnResult();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0 && resultCode == 0){
            checkPos = data.getIntExtra("checkPos", 0);
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void returnResult(){
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isLogIn", isLogIn);
        bundle.putInt("logPos", logPos);
        bundle.putInt("checkPos", checkPos);
        intent.putExtras(bundle);
        SettingActivity.this.setResult(0, intent);
    }

    private void setUpBtns(final Activity act){
        btnSettingBack = (ImageView) findViewById(R.id.btn_setting_back);
        btnSettingBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnResult();
                act.finish();
            }
        });

        btnLogOn = (LinearLayout) findViewById(R.id.btn_log_on);
        btnLogOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isLogIn){
                    //                startActivity(new Intent(SettingActivity.this, LogInActivity.class));
                    Toast.makeText(act, act.getResources().getString(R.string.mes_log), Toast.LENGTH_SHORT).show();
                }else {
                    switch (logPos){
                        case SINA:
                            logOutSina(act);
                            break;
                        case QQ:
                            logOutQQ(act);
                            break;
                    }
//                    returnResult();
                }
            }
        });

        btnSetColor = (LinearLayout) findViewById(R.id.btn_color_setting);
        btnSetColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(SettingActivity.this, ColorSettingActivity.class), 0);
            }
        });

        btnFeedback = (LinearLayout) findViewById(R.id.btn_feedback);
        btnFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(SettingActivity.this, FeedbackActivity.class));
                FeedbackAgent agent = new FeedbackAgent(act);
                agent.startFeedbackActivity();
            }
        });

        btnAboutUs = (LinearLayout) findViewById(R.id.btn_about_us);
        btnAboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingActivity.this, AboutUsActivity.class));
            }
        });

        btnDataSync = (LinearLayout) findViewById(R.id.btn_data_sync);
        btnDataSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(SettingActivity.this, DataSyncActivity.class));
                Toast.makeText(act, act.getResources().getString(R.string.sorry_wait), Toast.LENGTH_SHORT).show();
            }
        });

        btnUpdate = (LinearLayout) findViewById(R.id.btn_update);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UmengUpdateAgent.forceUpdate(act);
            }
        });

        btnQQ = (ImageView) findViewById(R.id.setting_share_qq);
        btnQQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oauthQQ(act);
            }
        });

        btnSina = (ImageView) findViewById(R.id.setting_share_sina);
        btnSina.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oauthSina(act);
            }
        });

        if(isLogIn){
            switch (logPos){
                case SINA:
                    btnSina.setImageResource(R.drawable.umeng_socialize_sina_on);
                    break;
                case QQ:
                    btnQQ.setImageResource(R.drawable.umeng_socialize_qq_on);
                    break;
            }
        }
    }

    private void setUpTextViews(){
        textLog = (TextView) findViewById(R.id.text_setting_log);
        if(isLogIn){
            switch (logPos){
                case SINA:
                    textLog.setText("退出登录(Sina)");
                    btnSina.setImageResource(R.drawable.umeng_socialize_sina_on);
                    break;
                case QQ:
                    textLog.setText("退出登录(QQ)");
                    btnQQ.setImageResource(R.drawable.umeng_socialize_qq_on);
                    break;
            }
        }else {
            textLog.setText("登录");
        }
        textUser = (TextView) findViewById(R.id.text_setting_user);
    }

    private void oauthSina(final Activity act){
        mController.doOauthVerify(SettingActivity.this, SHARE_MEDIA.SINA, new SocializeListeners.UMAuthListener() {
            @Override
            public void onStart(SHARE_MEDIA share_media) {
            }

            @Override
            public void onComplete(Bundle value, SHARE_MEDIA share_media) {
                if (value != null && !TextUtils.isEmpty(value.getString("uid"))) {
                    Toast.makeText(SettingActivity.this, "Ati授权成功", Toast.LENGTH_SHORT).show();
                    isLogIn = true;
                    logPos = 0;
                    textLog.setText("退出登录(Sina)");
                    btnSina.setImageResource(R.drawable.umeng_socialize_sina_on);
                } else {
                    Toast.makeText(SettingActivity.this, "Ati授权失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(SocializeException e, SHARE_MEDIA share_media) {
            }

            @Override
            public void onCancel(SHARE_MEDIA share_media) {
            }
        });

        mController.getPlatformInfo(SettingActivity.this, SHARE_MEDIA.SINA, new SocializeListeners.UMDataListener() {
            @Override
            public void onStart() {
                Toast.makeText(SettingActivity.this, "获取平台数据开始~", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete(int status, Map<String, Object> info) {
                if (status == 200 && info != null) {
                    StringBuilder sb = new StringBuilder();
                    Set<String> keys = info.keySet();
                    for (String key : keys
                            ) {
                        sb.append(key + "=" + info.get(key).toString() + "\r\n");
                    }
                    Log.d("TestData", sb.toString());
                } else {
                    Log.d("TestData", "发生错误：" + status);
                }
            }
        });

//        returnResult();
    }

    private void logOutSina(final Activity act){
        mController.deleteOauth(act, SHARE_MEDIA.SINA, new SocializeListeners.SocializeClientListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onComplete(int status, SocializeEntity entity) {
                if (status == 200) {
                    Toast.makeText(act, "退出成功", Toast.LENGTH_SHORT).show();
                    isLogIn = false;
                    logPos = SINA;
                    textLog.setText("登录");
                    btnSina.setImageResource(R.drawable.umeng_socialize_sina_off);
                } else {
                    Toast.makeText(act, "退出失败", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void addQQ(Activity act){
        UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(act, "1104803897", "2xog4814oI5TGYb3");
        qqSsoHandler.addToSocialSDK();
    }


    private void oauthQQ(final Activity act){
        mController.doOauthVerify(act, SHARE_MEDIA.QQ, new SocializeListeners.UMAuthListener() {
            @Override
            public void onStart(SHARE_MEDIA platform) {
                Toast.makeText(act, "授权开始", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(SocializeException e, SHARE_MEDIA platform) {
                Toast.makeText(act, "授权错误", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete(Bundle value, SHARE_MEDIA platform) {
                Toast.makeText(act, "授权完成", Toast.LENGTH_SHORT).show();

                //获取相关授权信息
                mController.getPlatformInfo(SettingActivity.this, SHARE_MEDIA.QQ, new SocializeListeners.UMDataListener() {
                    @Override
                    public void onStart() {
                        Toast.makeText(SettingActivity.this, "获取平台数据开始...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete(int status, Map<String, Object> info) {
                        if (status == 200 && info != null) {
                            StringBuilder sb = new StringBuilder();
                            Set<String> keys = info.keySet();
                            for (String key : keys) {
                                sb.append(key + "=" + info.get(key).toString() + "\r\n");
                            }
                            isLogIn = true;
                            logPos = QQ;
                            textLog.setText("退出登录(QQ)");
                            btnQQ.setImageResource(R.drawable.umeng_socialize_qq_on);
                            Log.d("TestData", sb.toString());
                        } else {
                            Log.d("TestData", "发生错误：" + status);
                        }
                    }
                });
            }

            @Override
            public void onCancel(SHARE_MEDIA platform) {
                Toast.makeText(act, "授权取消", Toast.LENGTH_SHORT).show();
            }
        });

//        returnResult();
    }

    private void logOutQQ(final Activity act){
        mController.deleteOauth(act, SHARE_MEDIA.QQ, new SocializeListeners.SocializeClientListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onComplete(int status, SocializeEntity entity) {
                if (status == 200) {
                    Toast.makeText(act, "退出成功", Toast.LENGTH_SHORT).show();
                    isLogIn = false;
                    textLog.setText("登录");
                    btnQQ.setImageResource(R.drawable.umeng_socialize_qq_off);
                } else {
                    Toast.makeText(act, "退出失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //------------------------------------------------------------------------------

}













