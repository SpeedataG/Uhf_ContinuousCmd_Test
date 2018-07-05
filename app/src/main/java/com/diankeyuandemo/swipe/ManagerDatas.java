package com.diankeyuandemo.swipe;

public class ManagerDatas {

    private int num;
    private String fNum;
    private String fNname;

    public ManagerDatas(int num, String fNum, String fNname) {
        this.num = num;
        this.fNum = fNum;
        this.fNname = fNname;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getfNum() {
        return fNum;
    }

    public void setfNum(String fNum) {
        this.fNum = fNum;
    }

    public String getfNname() {
        return fNname;
    }

    public void setfNname(String fNname) {
        this.fNname = fNname;
    }
}
