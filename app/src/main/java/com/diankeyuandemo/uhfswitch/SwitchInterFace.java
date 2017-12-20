package com.diankeyuandemo.uhfswitch;

public interface SwitchInterFace {
    /**
     * 初始化
     */
    void initDev();

    /**
     * 切换uhf分支器路数
     */
    int switchUhf(int i);
    void switchUhf();

    /**
     * 关闭设备
     */
    void closeDev();

}
