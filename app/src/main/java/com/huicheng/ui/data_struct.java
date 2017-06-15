package com.huicheng.ui;

/**
 * Created by Administrator on 2017/5/23.
 */

public class data_struct {

    static  final int connected = 0;
    static  final int connecting = 1;
    static  final int dis_connect =2;

     static final int   index_left_light = 0;
    static final int   index_front_light = 1;
    static final int   index_right_light = 2;

    static final byte protol_head = (byte) 0xac;
    static final byte protol_head_2 = (byte) 0x55;

    static final  int mode_index_color_pan = 0;
    static final  int mode_index_breathe = 1;
    static final  int mode_index_colors = 2;
    static final  int mode_index_shake = 3;

    public static final String dev_name = "HC-08";

    //开关标记
    static boolean onoff = false;
    //灯方向选择标记
    static boolean [] light_seclected = {false,false,false};

    //三个灯的颜色标记
    static  int light_color[] = {0,0,0};

    //呼吸
    static boolean is_breathe_mode = false;
    //七彩
    static boolean is_colcr_mode  = false;
    //闪烁
    static boolean is_flash_mode = false;
}
