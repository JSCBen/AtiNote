package com.tju.twist.atinote;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

public class ColorSettingActivity extends AppCompatActivity {
    private CheckBox checkBox0, checkBox1, checkBox2;
    private CheckBox[] checkBoxes;
    public static int checkPos = 0;

    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_setting);
        initColorPos(this);

        setUpCheckBoxes();
        setUpBtns(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_color_setting, menu);
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
        if(keyCode == KeyEvent.KEYCODE_BACK){
            retureResults();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void retureResults(){
        Intent intent = new Intent();
        intent.putExtra("checkPos", checkPos);
        ColorSettingActivity.this.setResult(0, intent);
    }

    private void setUpCheckBoxes(){
        checkBox0 = (CheckBox) findViewById(R.id.checkbox0);
        checkBox1 = (CheckBox) findViewById(R.id.checkbox1);
        checkBox2 = (CheckBox) findViewById(R.id.checkbox2);

        switch (checkPos){
            case 0:
                checkBox0.setChecked(true);
                break;
            case 1:
                checkBox1.setChecked(true);
                break;
            case 2:
                checkBox2.setChecked(true);
                break;
            default:
                checkBox0.setChecked(true);
        }

        checkBoxes = new CheckBox[]{checkBox0, checkBox1, checkBox2};
        for (CheckBox box: checkBoxes
             ) {
            box.setOnCheckedChangeListener(new ColorCheckListener());
        }
    }

    private void setUpBtns(final Activity act){
        btnBack = (ImageView) findViewById(R.id.btn_colorSetting_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retureResults();
                act.finish();

            }
        });
    }

    private class ColorCheckListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int id = buttonView.getId();
            if(isChecked){
                switch (id){
                    case R.id.checkbox0:
                        checkPos = 0;
                        break;
                    case R.id.checkbox1:
                        checkPos = 1;
                        break;
                    case R.id.checkbox2:
                        checkPos = 2;
                        break;
                    default:
                        checkPos = 0;
                }
            }
            int i = 0;
            for (CheckBox box: checkBoxes
                 ) {
                if(i != checkPos){
                    box.setChecked(false);
                }
                i++;
            }
            setColorSetting(ColorSettingActivity.this, checkPos);
        }
    }

    public int getColorStringPos(){
        return checkPos;
    }

    private void setColorSetting(Activity act, int colorPos){
        SharedPreferences sp = act.getSharedPreferences("setting", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("colorPos", colorPos);
        editor.commit();
    }

    private void initColorPos(Activity act){
        SharedPreferences sp = act.getSharedPreferences("setting", MODE_PRIVATE);
        checkPos = sp.getInt("colorPos", 0);
    }

}
