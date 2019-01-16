package com.tju.twist.utils;

import com.tju.twist.atinote.R;

/**
 * Created by Twist on 2015/9/20.
 */
public class ColorPicker {
    private static final int[] colorStrings0 = new int[]{R.color.color0,
            R.color.color1, R.color.color2,
            R.color.color3, R.color.color4};
    private static final int[] colorStrings1 = new int[]{R.color.color00,
            R.color.color01, R.color.color02,
            R.color.color03, R.color.color04};
    private static final int[] colorStrings2 = new int[]{R.color.color10,
            R.color.color11, R.color.color12,
            R.color.color13, R.color.color14};
    private static final int[][] colors = new int[][]{colorStrings0, colorStrings1, colorStrings2};
    public static int colorStringPos = 0;

    public int getColor(int stringPos, int itemPos){
        return colors[stringPos][itemPos];
    }

    public void setColorStringPos(int i){
        colorStringPos = i;
    }
}
