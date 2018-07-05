package com.diankeyuandemo;

import android.app.Application;

import com.diankeyuandemo.db.DaoMaster;
import com.diankeyuandemo.db.DaoSession;
import com.diankeyuandemo.db.DbHelper;

import org.greenrobot.greendao.database.Database;

public class App extends Application {
    private DaoSession daoSession;
    private static App sInstance;
    @Override
    public void onCreate() {
        super.onCreate();
        sInstance=this;
        DbHelper helper = new DbHelper(this, "diankeyuan", null);
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();

    }
    public static App getsInstance() {
        return sInstance;
    }
    public DaoSession getDaoSession() {
        return daoSession;
    }
}
