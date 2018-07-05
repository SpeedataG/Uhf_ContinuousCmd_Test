package com.diankeyuandemo.swipe;


/**
 * Created by Horrarndoo on 2017/3/17.
 * SwipeLayout公共接口
 */

public interface SwipeLayoutInterface {

    SwipeLayout.SwipeState getCurrentState();

    void open();

    void close();
}
