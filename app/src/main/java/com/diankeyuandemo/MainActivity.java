package com.diankeyuandemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.serialport.DeviceControl;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.diankeyuandemo.dialog.InvSetDialog;
import com.diankeyuandemo.dialog.LockTagDialog;
import com.diankeyuandemo.dialog.ReadTagDialog;
import com.diankeyuandemo.dialog.SearchTagDialog;
import com.diankeyuandemo.dialog.SetEPCDialog;
import com.diankeyuandemo.dialog.SetModuleDialog;
import com.diankeyuandemo.dialog.SetPasswordDialog;
import com.diankeyuandemo.dialog.WriteTagDialog;
import com.diankeyuandemo.util.PsamUtil;
import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.UHFManager;
import com.speedata.libuhf.bean.SpdReadData;
import com.speedata.libuhf.bean.SpdWriteData;
import com.speedata.libuhf.interfaces.OnSpdReadListener;
import com.speedata.libuhf.interfaces.OnSpdWriteListener;
import com.speedata.libuhf.utils.SharedXmlUtil;
import com.speedata.libutils.CommonUtils;
import com.speedata.libutils.DataConversionUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

public class MainActivity extends Activity implements OnClickListener, OnSpdReadListener, OnSpdWriteListener {
    private static final String[] list = {"Reserved", "EPC", "TID", "USER"};
    private TextView Cur_Tag_Info;
    private TextView Status, Version;
    private Spinner Area_Select;
    private ArrayAdapter<String> adapter;
    private Button Search_Tag;
    private Button Read_Tag;
    private Button Write_Tag;
    private Button Set_Tag;
    private Button Set_Password;
    private Button Set_EPC;
    private Button Lock_Tag;
    private Button btnOpentheDoor;
    private Button btn_inv_set;
    private IUHFService iuhfService;
    private String current_tag_epc = null;
    private Button Speedt;
    private PowerManager pM = null;
    private WakeLock wK = null;
    private int init_progress = 0;
    private String modle;
    private DeviceControl deviceControl;
    private String readHexString;
    private byte[] readDatas;
    private String decodeData;
    private String newEpc;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            iuhfService = UHFManager.getUHFService(MainActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "模块不存在", Toast.LENGTH_SHORT).show();
            return;
        }
//        iuhfService.setOnReadListener(this);
//        iuhfService.setOnWriteListener(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECE_DATA_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
        modle = SharedXmlUtil.getInstance(MainActivity.this).read("modle", "");
        initUI();
        Version.append("-" + modle);
        newWakeLock();
        EventBus.getDefault().register(this);
        Set_Tag.setEnabled(true);
        Search_Tag.setEnabled(true);
        Read_Tag.setEnabled(true);
        Write_Tag.setEnabled(true);
        Set_EPC.setEnabled(true);
        Set_Password.setEnabled(true);
        Lock_Tag.setEnabled(true);
        Area_Select.setEnabled(true);
        if ("r2k".equals(modle)) {
            btn_inv_set.setVisibility(View.VISIBLE);
            btn_inv_set.setEnabled(true);
        }
        try {
            deviceControl = new DeviceControl(DeviceControl.PowerType.MAIN);

        } catch (IOException e) {
            e.printStackTrace();
        }
        //psam 初始化
        PsamUtil.initePsam(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (iuhfService != null) {
                if (openDev()) return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //解码广播
    private static final String RECE_DATA_ACTION = "com.se4500.onDecodeComplete";
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (RECE_DATA_ACTION.equals(intent.getAction())) {
                decodeData = intent.getStringExtra("se4500") + "00";
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (iuhfService != null) {
                iuhfService.CloseDev();
                //断点后选卡操作会失效，需要重新选卡（掩码）
                current_tag_epc = null;
                Cur_Tag_Info.setText("");
//                deviceControl.PowerOffDevice();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @org.greenrobot.eventbus.Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MsgEvent mEvent) {
        String type = mEvent.getType();
        String msg = (String) mEvent.getMsg();
        if (type.equals("set_current_tag_epc")) {
            current_tag_epc = msg;
            Cur_Tag_Info.setText(msg);
            MainActivity.this.Status
                    .setText(R.string.Status_Select_Card_Ok);
        }
        if (type.equals("setPWD_Status")) {
            MainActivity.this.Status
                    .setText(R.string.Status_Write_Card_Ok);
        }
        if (type.equals("lock_Status")) {
            MainActivity.this.Status
                    .setText("设置成功");
        }
        if (type.equals("SetEPC_Status")) {
            MainActivity.this.Status
                    .setText(R.string.Status_Write_Card_Ok);
        }
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

    /**
     * 上电开串口
     *
     * @return
     */
    private boolean openDev() {
        if (iuhfService.OpenDev() != 0) {
            Cur_Tag_Info.setText("Open serialport failed");
            new AlertDialog.Builder(this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_OPEN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    finish();
                }
            }).show();
            return true;
        }
        return false;
    }

    private void initUI() {
        setContentView(R.layout.main);
        Write_Tag = (Button) findViewById(R.id.btn_write);
        Write_Tag.setOnClickListener(this);
        Read_Tag = (Button) findViewById(R.id.btn_read);
        Read_Tag.setOnClickListener(this);
        Search_Tag = (Button) findViewById(R.id.btn_search);
        Search_Tag.setOnClickListener(this);
        Set_Tag = (Button) findViewById(R.id.btn_check);
        Set_Tag.setOnClickListener(this);
        Set_Password = (Button) findViewById(R.id.btn_setpasswd);
        Set_Password.setOnClickListener(this);
        Set_EPC = (Button) findViewById(R.id.btn_setepc);
        Set_EPC.setOnClickListener(this);
        btn_inv_set = (Button) findViewById(R.id.btn_inv_set);
        btn_inv_set.setOnClickListener(this);
        Lock_Tag = (Button) findViewById(R.id.btn_lock);
        Lock_Tag.setOnClickListener(this);
        Speedt = (Button) findViewById(R.id.button_spt);
        Speedt.setOnClickListener(this);
        Cur_Tag_Info = (TextView) findViewById(R.id.textView_epc);
        Cur_Tag_Info.setText("");
        Status = (TextView) findViewById(R.id.textView_status);
        Version = (TextView) findViewById(R.id.textView_version);
        Version.setText(CommonUtils.getAppVersionName(this));
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Area_Select = (Spinner) findViewById(R.id.spinner_area);
        Area_Select.setAdapter(adapter);
        Set_Tag.setEnabled(false);
        Search_Tag.setEnabled(false);
        Read_Tag.setEnabled(false);
        Write_Tag.setEnabled(false);
        Set_EPC.setEnabled(false);
        Set_Password.setEnabled(false);
        Lock_Tag.setEnabled(false);
        Area_Select.setEnabled(false);

        btnOpentheDoor = findViewById(R.id.btn_openthedoor);
        btnOpentheDoor.setOnClickListener(this);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        wK.release();
        unregisterReceiver(broadcastReceiver);
        //注销广播、对象制空
        UHFManager.closeUHFService();
        EventBus.getDefault().unregister(this);
        if (deviceControl != null) {
            try {
                deviceControl.MainPowerOff(121);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub
        if (arg0 == Read_Tag) {
            if (current_tag_epc == null) {
                Status.setText(R.string.Status_No_Card_Select);
                Toast.makeText(this, R.string.Status_No_Card_Select, Toast.LENGTH_SHORT).show();
                return;
            }

            //读卡
            ReadTagDialog readTag = new ReadTagDialog(this, iuhfService
                    , Area_Select.getSelectedItemPosition(), current_tag_epc, modle);
            readTag.setTitle(R.string.Item_Read);
            readTag.show();

        } else if (arg0 == Write_Tag) {
            if (current_tag_epc == null) {
                Status.setText(R.string.Status_No_Card_Select);
                Toast.makeText(this, R.string.Status_No_Card_Select, Toast.LENGTH_SHORT).show();
                return;
            }
            //写卡
            WriteTagDialog writeTag = new WriteTagDialog(this, iuhfService,
                    Area_Select.getSelectedItemPosition()
                    , current_tag_epc, modle);
            writeTag.setTitle(R.string.Item_Write);
            writeTag.show();

        } else if (arg0 == Search_Tag) {

            //盘点选卡
            SearchTagDialog searchTag = new SearchTagDialog(this, iuhfService, modle);
            searchTag.setTitle(R.string.Item_Choose);
            searchTag.show();

        } else if (arg0 == Set_Tag) {
            //设置频率频段
            SetModuleDialog setDialog = new SetModuleDialog(this, iuhfService, modle);
            setDialog.setTitle(R.string.Item_Set_Title);
            setDialog.show();

        } else if (arg0 == Set_Password) {
            if (current_tag_epc == null) {
                Status.setText(R.string.Status_No_Card_Select);
                Toast.makeText(this, R.string.Status_No_Card_Select, Toast.LENGTH_SHORT).show();
                return;
            }
            //设置密码
            SetPasswordDialog setPasswordDialog = new SetPasswordDialog(this
                    , iuhfService, current_tag_epc, modle);
            setPasswordDialog.setTitle(R.string.SetPasswd_Btn);
            setPasswordDialog.show();
        } else if (arg0 == Set_EPC) {
            if (current_tag_epc == null) {
                Status.setText(R.string.Status_No_Card_Select);
                Toast.makeText(this, R.string.Status_No_Card_Select, Toast.LENGTH_SHORT).show();
                return;
            }
            //写EPC
            SetEPCDialog setEPCDialog = new SetEPCDialog(this, iuhfService, current_tag_epc);
            setEPCDialog.setTitle(R.string.SetEPC_Btn);
            setEPCDialog.show();
        } else if (arg0 == Lock_Tag) {
            if (current_tag_epc == null) {
                Status.setText(R.string.Status_No_Card_Select);
                Toast.makeText(this, R.string.Status_No_Card_Select, Toast.LENGTH_SHORT).show();
                return;
            }
            //锁
            LockTagDialog lockTagDialog = new LockTagDialog(this, iuhfService
                    , current_tag_epc, modle);
            lockTagDialog.setTitle(R.string.Lock_Btn);
            lockTagDialog.show();
        } else if (arg0 == btn_inv_set) {
            //盘点内容设置
            InvSetDialog invSetDialog = new InvSetDialog(this, iuhfService);
            invSetDialog.setTitle("Inv Set");
            invSetDialog.show();
        } else if (arg0 == btnOpentheDoor) {


            if (current_tag_epc == null) {
                Status.setText(R.string.Status_No_Card_Select);
                Toast.makeText(this, R.string.Status_No_Card_Select, Toast.LENGTH_SHORT).show();
                return;
            }
            showInputDialog();


//            int readArea = iuhfService.newReadArea(2, 0, 6, "00000000");
//            if (readArea != 0) {
//                Toast.makeText(this, "读卡参数不正确", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            iuhfService.setOnReadListener(new OnSpdReadListener() {
//                @Override
//                public void getReadData(SpdReadData var1) {
//                    byte[] epcData = var1.getEPCData();
//                    if (var1.getStatus() == 0) {
//                        byte[] readData = var1.getReadData();
//                        byte[] bytes = PsamUtil.result(readData);
//                        if (bytes == null) {
//                            Toast.makeText(MainActivity.this, "获取校验数据成功", Toast.LENGTH_SHORT).show();
//                            return;
//                        } else {
//                            int writeArea = iuhfService.newWriteArea(3, 0, 10, "00000000", bytes);
//                            if (writeArea != 0) {
//                                Toast.makeText(MainActivity.this, "写卡参数不正确", Toast.LENGTH_SHORT).show();
//                            }
//                            iuhfService.setOnWriteListener(new OnSpdWriteListener() {
//                                @Override
//                                public void getWriteData(SpdWriteData var1) {
//                                    StringBuilder stringBuilder = new StringBuilder();
//                                    if (var1.getStatus() == 0) {
//                                        //状态判断，已经写卡成功了就不返回错误码了
//                                        handler.sendMessage(handler.obtainMessage(1, "写卡成功"));
////                                        Toast.makeText(MainActivity.this, "写卡成功", Toast.LENGTH_SHORT).show();
//                                    } else {
//                                        Toast.makeText(MainActivity.this, "写卡失败" + var1.getStatus(), Toast.LENGTH_SHORT).show();
//                                        stringBuilder.append("WriteError：" + var1.getStatus() + "\n");
//                                    }
//
//                                }
//                            });
//                        }
//
//                    } else {
//                        Toast.makeText(MainActivity.this, "读取TID失败", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                }
//            });

        }
    }

    private String state = "";

    private void showInputDialog() {
    /*@setView 装入一个EditView
     */
        final EditText editText = new EditText(MainActivity.this);
        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(MainActivity.this);
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
                                        Log.i("ddddd", "写epc成功");
                                        iuhfService.select_card(1, "", false);
                                        SystemClock.sleep(100);
                                        int res = iuhfService.select_card(1, newEpc, true);//重新选卡
                                        if (res == 0) {
                                            Log.i("ddddd", "重新选卡成功");
//                                            lockUhfCard("lock");
//                                if (res == 0) {
//                                    Log.i("ddddd", "s锁卡成功");
                                            byte[] datas = iuhfService.read_area(2, 0, 6, "00000000");
                                            if (datas != null) {
                                                Log.i("ddddd", DataConversionUtils.byteArrayToString(datas));
                                                Log.i("ddddd", "读tid成功");
                                                byte[] bytes = PsamUtil.result(datas);
                                                if (bytes == null) {
                                                    Log.i("ddddd", "psam失败");
                                                    Toast.makeText(MainActivity.this, "获取校验数据失败", Toast.LENGTH_SHORT).show();
                                                    return;
                                                } else {
                                                    Log.i("ddddd", DataConversionUtils.byteArrayToString(bytes) + "dddd" + bytes.length);
                                                    Log.i("ddddd", "psam成功");
                                                    SystemClock.sleep(100);
                                                    res = iuhfService.write_area(3, 0, 10, "00000000", bytes);

                                                    Log.i("ddddd", "res" + res);
                                                    if (res == 0) {
                                                        Log.i("ddddd", "写user成功");
//                                                    Toast.makeText(MainActivity.this, "chengggggg", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                        }

//                                }

                                    }
                                }
                            }
                        }).

                                start();

//                        writeUhfCard(1, 2, 6, DataConversionUtils.hexStringToByteArray(newEpc), "newEpc");

                    }
                }).

                show();

    }

    private void lockUhfCard(String s) {
        int reval = iuhfService.newSetLock(1, 2, "00000000");
        if (reval != 0) {
            handler.sendMessage(handler.obtainMessage(1, "锁卡参数不正确"));
        } else {
            state = s;
            isflag = false;
        }
    }

    private void readUhfCard(int i, int addr, int count, String s) {
        int readArea = iuhfService.newReadArea(i, addr, count, "00000000");
        if (readArea != 0) {
            Toast.makeText(this, "读卡参数不正确", Toast.LENGTH_SHORT).show();
            return;
        } else {
            state = s;
            isflag = false;
        }
    }

    private void writeUhfCard(int i, int addr, int count, byte[] b, String s) {
        int writeArea = iuhfService.newWriteArea(i, addr, count, "00000000", b);
        if (writeArea != 0) {
            Toast.makeText(MainActivity.this, "写卡参数不正确", Toast.LENGTH_SHORT).show();
            return;
        } else {
            state = s;
            isflag = false;
        }

    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Toast.makeText(MainActivity.this, msg.obj + "", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(MainActivity.this, msg.obj + "", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    SystemClock.sleep(100);
                    lockUhfCard("lock");
                    Cur_Tag_Info.setText((String) msg.obj);
                    break;
                case 4:
                    SystemClock.sleep(100);

                    readUhfCard(2, 0, 6, "readTid");
                    break;
                case 5:
                    SystemClock.sleep(100);
                    writeUhfCard(3, 0, 10, (byte[]) msg.obj, "writUser");
//                    writeUhfCard(3, 0, 10, DataConversionUtils.hexStringToByteArray("0000000000000000000000000000000000000000"), "writUser");
                    break;
            }
        }
    };
    private long mkeyTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.ACTION_DOWN:
                if ((System.currentTimeMillis() - mkeyTime) > 2000) {
                    mkeyTime = System.currentTimeMillis();
                    Toast.makeText(MainActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void getReadData(SpdReadData var1) {
        Log.i("ddddd", "tdddddddd" + isflag + state);
        if (isflag) {
            return;
        }
        if (state.equals("readTid")) {
            if (var1.getStatus() == 0) {
                Log.i("ddddd", "读tid成功");
                byte[] readData = var1.getReadData();
                byte[] readDatas = PsamUtil.result(readData);
                if (readDatas == null) {
                    Toast.makeText(MainActivity.this, "获取校验数据失败", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    handler.sendMessage(handler.obtainMessage(5, readDatas));
                }
            } else {
                Log.i("ddddd", "读tid失败");
                handler.sendMessage(handler.obtainMessage(2, "读TID失败"));
            }
        }
    }

    private boolean isflag = false;

    @Override
    public void getWriteData(SpdWriteData var1) {
        Log.i("ddddd", "ttttttttt" + state + isflag);
        if (isflag) {
            return;
        }
//        if (state.equals("newEpc")) {
//            if (var1.getStatus() == 0) {
//                iuhfService.select_card(1, "", false);
//                SystemClock.sleep(100);
//                int res = iuhfService.select_card(1, newEpc, true);//重新选卡
//                Log.i("ddddd", "写epc成功");
//                if (res == 0) {
//                    SystemClock.sleep(100);
//                    handler.sendMessage(handler.obtainMessage(3, newEpc));
//                }
//            } else {
//                Log.i("ddddd", "写epc失败");
//                handler.sendMessage(handler.obtainMessage(2, "写EPC失败" + var1.getStatus()));
//                return;
//            }
//        }
        if (state.equals("lock")) {
            if (var1.getStatus() == 0) {
                isflag = true;
                Log.i("ddddd", "锁卡成功");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (this) {
                            byte[] datas = iuhfService.read_area(2, 0, 6, "00000000");
                            if (datas != null) {
//                                Log.i("ddddd", DataConversionUtils.byteArrayToString(datas));
                                Log.i("ddddd", "读tid成功");
                                byte[] bytes = PsamUtil.result(datas);
                                if (bytes == null) {
                                    Log.i("ddddd", "psam失败");
                                    Toast.makeText(MainActivity.this, "获取校验数据失败", Toast.LENGTH_SHORT).show();
                                    return;
                                } else {
                                    Log.i("ddddd", DataConversionUtils.byteArrayToString(bytes) + "dddd" + bytes.length);
                                    Log.i("ddddd", "psam成功");
                                    SystemClock.sleep(100);
                                    int res = iuhfService.write_area(3, 0, 10, "00000000", bytes);

                                    Log.i("ddddd", "res" + res);
                                    if (res == 0) {
                                        Log.i("ddddd", "写user成功");
//                                                    Toast.makeText(MainActivity.this, "chengggggg", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                Log.i("ddddd", "读tid失败");
                            }
                        }
                    }
                }).start();
//                handler.sendMessage(handler.obtainMessage(4, ""));
            } else {
                Log.i("ddddd", "锁卡失败");
                handler.sendMessage(handler.obtainMessage(2, "锁卡失败" + var1.getStatus()));
                return;
            }
        }

//        if (state.equals("writUser")) {
//            if (var1.getStatus() == 0) {
//                isflag = true;
//                Log.i("ddddd", "写user 成功" + "");
//                handler.sendMessage(handler.obtainMessage(1, "写USER成功" + var1.getStatus()));
//            } else {
//                Log.i("ddddd", "写user 失败" + "");
//                handler.sendMessage(handler.obtainMessage(2, "写USER失败" + var1.getStatus()));
//                return;
//            }
//        }


    }
}
