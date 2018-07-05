package com.diankeyuandemo.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.diankeyuandemo.MsgEvent;
import com.diankeyuandemo.R;
import com.diankeyuandemo.db.DataDb;
import com.diankeyuandemo.fragment.HomeFragment;
import com.diankeyuandemo.fragment.ManagerFragment;
import com.diankeyuandemo.fragment.SearchFragment;
import com.diankeyuandemo.util.DBUitl;
import com.diankeyuandemo.util.DataConvertUtil;
import com.diankeyuandemo.util.PsamUtil;
import com.diankeyuandemo.util.SettingDialog;
import com.diankeyuandemo.util.SharedPreferencesUitl;
import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.UHFManager;
import com.speedata.libutils.DataConversionUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;

public class CheckOutActivity extends FragmentActivity implements View.OnClickListener {
    private IUHFService iuhfService;
    private int init_progress = 0;
    private PowerManager pM = null;
    private PowerManager.WakeLock wK = null;
    //解码广播
    private static final String RECE_DATA_ACTION = "com.se4500.onDecodeComplete";
    private String mEventMsg;
    private String newEpc;
    private SharedPreferencesUitl preferencesUitl;
    /**
     * 电科院UHF+PSAM
     */
    private TextView mTitleName;
    /**
     * 写卡
     */
    private Button mBtnWriteCard;
    /**
     * 验证
     */
    private Button mBtnCheckout;
    /**
     * 管理员
     */
    private Button mBtnManage;
    /**
     * 设置
     */
    private Button mBtnSeting;
    private TextView mTvMsg;
    private ManagerFragment managerFragment;
    private SearchFragment searchFragment;
    private HomeFragment homeFragment;
    private DBUitl dbUitl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏 第一种方法
        setContentView(R.layout.main_layout);
        initView();
        try {
            iuhfService = UHFManager.getUHFService(CheckOutActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(CheckOutActivity.this, "模块不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECE_DATA_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
        newWakeLock();
        EventBus.getDefault().register(this);
        PsamUtil.initePsam(CheckOutActivity.this);
        changeFragment(homeFragment);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (RECE_DATA_ACTION.equals(intent.getAction())) {
                String decodeData = intent.getStringExtra("se4500") + "00";
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mEventMsg = null;
        try {
            if (iuhfService != null) {
                if (openDev()) return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (iuhfService != null) {
                iuhfService.CloseDev();
                //断点后选卡操作会失效，需要重新选卡（掩码）

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wK.release();
        unregisterReceiver(broadcastReceiver);
        //注销广播、对象制空
        UHFManager.closeUHFService();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 上电开串口
     *
     * @return
     */
    private boolean openDev() {
        if (iuhfService.OpenDev() != 0) {
            Toast.makeText(this, "Open serialport failed", Toast.LENGTH_SHORT).show();
            new AlertDialog.Builder(this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_OPEN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).show();
            return true;
        }
        return false;
    }

    private void initFragment() {
        managerFragment = new ManagerFragment();
        searchFragment = new SearchFragment();
        homeFragment = new HomeFragment();
    }

    /**
     * 切换的Fragment
     *
     * @param f
     */
    public void changeFragment(Fragment f) {
        //关闭

        getSupportFragmentManager().beginTransaction().
                replace(R.id.fragment, f).commit();

    }

    private void newWakeLock() {
        init_progress++;
        pM = (PowerManager) getSystemService(POWER_SERVICE);
        if (pM != null) {
            wK = pM.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE, "lock3992");
            if (wK != null) {
                wK.acquire();
                init_progress++;
            }
        }
        if (init_progress == 1) {
            Log.w("3992_6C", "wake lock init failed");
        }
    }

    private void initView() {
        dbUitl = new DBUitl();
        preferencesUitl = SharedPreferencesUitl.getInstance(this, "dainkeyuan");
        mTitleName = findViewById(R.id.title_name);
        mTvMsg = findViewById(R.id.tv_msg);
        mBtnWriteCard = findViewById(R.id.btn_write_card);
        mBtnWriteCard.setOnClickListener(this);
        mBtnCheckout = findViewById(R.id.btn_checkout);
        mBtnCheckout.setOnClickListener(this);
        mBtnManage = findViewById(R.id.btn_manage);
        mBtnManage.setOnClickListener(this);
        mBtnSeting = findViewById(R.id.btn_seting);
        mBtnSeting.setOnClickListener(this);
        initFragment();
        mTitleName.setOnClickListener(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MsgEvent mEvent) {
        String type = mEvent.getType();
        if (type.equals("set_current_tag_epc")) {
            mEventMsg = (String) mEvent.getMsg();
            Toast.makeText(this, R.string.Status_Select_Card_Ok, Toast.LENGTH_SHORT).show();
            CheckOutActivity.this.mTvMsg
                    .setText("EPC:" + mEventMsg);
        }
        if (type.equals("setPWD_Status")) {
            Toast.makeText(this, R.string.Status_Write_Card_Ok, Toast.LENGTH_SHORT).show();

        }
        if (type.equals("lock_Status")) {
            Toast.makeText(this, "设置成功", Toast.LENGTH_SHORT).show();
        }
        if (type.equals("SetEPC_Status")) {
            Toast.makeText(this, R.string.Status_Write_Card_Ok, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.btn_write_card:
                mTitleName.setText("标签发行认证写卡");
                if (mEventMsg == null) {
                    mTvMsg.setText("请先选卡后再执行写卡操作");
                    changeFragment(searchFragment);
                    return;
                }
                showInputDialog();
                break;
            case R.id.btn_checkout://验证
                mTitleName.setText("标签发行认证验证");
                if (mEventMsg == null) {
                    mTvMsg.setText("请先选卡后再执行验证操作");
                    changeFragment(searchFragment);
                    return;
                }

                checkOut();
                break;
            case R.id.btn_manage:
                mTitleName.setText("标签发行认证管理员");
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment);
                if (f instanceof ManagerFragment) {
                    return;
                } else {
                    ManageDialog();
                    mTvMsg.setText("请执行添加/修改/删除操作");
                }
                break;
            case R.id.btn_seting:
                mTitleName.setText("标签发行认证设置");
                SettingDialog.showCustomizeDialog(this);
                break;
        }
    }

    /**
     * 验证操作
     */
    private void checkOut() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
//                            byte[] epcDatas = iuhfService.read_area(1, 0, 10, "00000000");
//                            if (epcDatas != null) {
//                            handler.handleMessage(handler.obtainMessage(3, "epc成功！！!"));
//                            Log.i("tw", "epc成功！！!");
                    byte[] tidDatas = iuhfService.read_area(2, 0, 6, "00000000");//读取tid
                    if (tidDatas != null) {
                        Log.i("tw", "校验: tid::" + DataConversionUtils.byteArrayToString(tidDatas));
//                                Log.i("tw", "读tid成功！！!");
                        SystemClock.sleep(1000);
                        byte[] userDatas = iuhfService.read_area(3, 0, 10, "00000000");//读取user区
                        if (userDatas != null) {
                            Log.i("tw", "校验: user::" + DataConversionUtils.byteArrayToString(userDatas));
//                                    Log.i("tw", "读user成功！！!");
                            byte[] num = DataConvertUtil.cutBytes(userDatas, 0, 1);//获取厂商代码
                            DataDb dataDb = dbUitl.queryDbBody(DataConversionUtils.byteArrayToString(num));
                            if (dataDb != null) {
                                handler.sendMessage(handler.obtainMessage(5, dataDb.getFactorNname()));
                            }
                            Log.i("tw", "校验:厂商num::" + DataConversionUtils.byteArrayToString(num));
                            byte[] time = DataConvertUtil.cutBytes(userDatas, 1, 5);
                            Log.i("tw", "校验: time::" + DataConversionUtils.byteArrayToString(time));
                            byte[] checkOutDatas1 = DataConvertUtil.cutBytes(userDatas, 6, 4);
                            Log.i("tw", "user:校验值::" + DataConversionUtils.byteArrayToString(checkOutDatas1));
                            byte[] checkOutDatas2 = PsamUtil.checkOutResult(DataConversionUtils.byteArrayToString(num)
                                    , tidDatas, DataConversionUtils.byteArrayToString(time));
//                                    Log.i("tw", "校验: 校验值::" + DataConversionUtils.byteArrayToString(checkOutDatas2));
                            if (checkOutDatas2 == null) {
                                handler.sendMessage(handler.obtainMessage(1, "校验失败！！！"));
                                return;
                            }
                            if (Arrays.equals(checkOutDatas1, checkOutDatas2)) {
                                handler.sendMessage(handler.obtainMessage(1, "验证成功！！！"));
                            } else {
                                Log.i("tw", "run:校验失败 ");
                                handler.sendMessage(handler.obtainMessage(1, "校验失败！！！"));
                            }
                        }
                    }

                }
//                        }

            }
        }).start();

    }

    private void ManageDialog() {
        final EditText newPwd = new EditText(CheckOutActivity.this);
        newPwd.setHint("输入管理员密码");
        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(CheckOutActivity.this);
        inputDialog.setTitle("管理员密码").setView(newPwd);
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (preferencesUitl.read("pwd", "").equals("")) {
                            if (newPwd.getText().toString().equals("admin")) {
                                changeFragment(managerFragment);
                            } else {
                                Toast.makeText(CheckOutActivity.this, "管理员密码错误！", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (preferencesUitl.read("pwd", "").equals(newPwd.getText().toString())) {
                                changeFragment(managerFragment);
                            } else {
                                Toast.makeText(CheckOutActivity.this, "管理员密码错误！", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }).

                show();
    }

    /**
     * 写卡操作
     */
    private void showInputDialog() {
    /*@setView 装入一个EditView
     */
        final EditText editText = new EditText(CheckOutActivity.this);
        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(CheckOutActivity.this);
        inputDialog.setTitle("输入/扫描EPC").setView(editText);
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        newEpc = editText.getText().toString() + "00";
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                synchronized (this) {
                                    int sta = iuhfService.write_area(1, 2, 6,
                                            "00000000", DataConversionUtils.hexStringToByteArray(newEpc));
                                    if (sta == 0) {
                                        Log.i("zm", "写epc成功");
                                        iuhfService.select_card(1, "", false);
                                        SystemClock.sleep(100);
                                        int res = iuhfService.select_card(1, newEpc, true);//重新选卡
                                        if (res == 0) {
                                            Log.i("zm", "重新选卡成功");
                                            handler.sendMessage(handler.obtainMessage(3, "重新选卡成功"));

                                            SystemClock.sleep(500);
//                                            int setlock = 0;
                                            int setlock = iuhfService
                                                    .setlock(1, 2, "00000000");
//                                            lockUhfCard("lock");
                                            if (setlock == 0) {
                                                Log.i("zm", "s锁卡成功");
                                                SystemClock.sleep(500);
                                                byte[] datas = iuhfService.read_area(2, 0, 6, "00000000");
                                                if (datas != null) {
                                                    Log.i("zm", DataConversionUtils.byteArrayToString(datas));
                                                    Log.i("zm", "读tid成功");
                                                    byte[] bytes = PsamUtil.WriteResult(datas);
                                                    if (bytes == null) {
                                                        Log.i("zm", "psam失败");
                                                        Toast.makeText(CheckOutActivity.this, "获取校验数据失败", Toast.LENGTH_SHORT).show();
                                                        return;
                                                    } else {
                                                        Log.i("zm", DataConversionUtils.byteArrayToString(bytes) + "dddd" + bytes.length);
                                                        Log.i("zm", "psam成功");
                                                        SystemClock.sleep(500);
                                                        res = iuhfService.write_area(3, 0, 10, "00000000", bytes);
                                                        Log.i("zm", "res" + res);
                                                        if (res == 0) {
                                                            Log.i("zm", "写user成功");
                                                            handler.sendMessage(handler.obtainMessage(1, "写USER成功!!!"));
//                                                    Toast.makeText(CheckOutActivity.this, "chengggggg", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            handler.sendMessage(handler.obtainMessage(1, "写USER失败!!!"));
                                                        }
                                                    }
                                                }
                                            }else {
                                                handler.sendMessage(handler.obtainMessage(1, "锁卡失败请重新操作！！！"));
                                            }
                                        }
                                    }
                                }
                            }
                        }).start();
                    }
                }).show();

    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Toast.makeText(CheckOutActivity.this, msg.obj + "", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(CheckOutActivity.this, msg.obj + "", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(CheckOutActivity.this, msg.obj + "", Toast.LENGTH_SHORT).show();
                    mTvMsg.setText("EPC：" + newEpc);
                    break;
                case 4:
                    Toast.makeText(CheckOutActivity.this, msg.obj + "", Toast.LENGTH_SHORT).show();
                    break;
                case 5:
                    mTvMsg.append("\n厂商名称：" + (String) msg.obj);
                    break;
            }
        }
    };

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            /* 当前Activity  fra 是哪个 */
            Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment);
            if (f instanceof HomeFragment) {
                exit();
                return true;
            } else {
                mEventMsg = null;
                changeFragment(homeFragment);
                mTitleName.setText("标签发行认证");
                mTvMsg.setText("请执行相关操作");
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private long mExitTime = 0;

    /**
     * 双击back退出
     */
    public void exit() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Toast.makeText(CheckOutActivity.this, "再按一次退出标签发行认证", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
        }
    }

    /**
     * 获取当前应用程序的版本号
     */
    public String getVersion() {
        PackageManager pm = getPackageManager();
        try {
            PackageInfo packinfo = pm.getPackageInfo(getPackageName(), 0);
            return packinfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "版本号错误";
        }
    }
}
