package com.diankeyuandemo.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.diankeyuandemo.MsgEvent;
import com.diankeyuandemo.R;
import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.bean.SpdInventoryData;
import com.speedata.libuhf.interfaces.OnSpdInventoryListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 张明_ on 2016/12/28.
 */

public class SearchTagDialog extends Dialog implements
        View.OnClickListener, AdapterView.OnItemClickListener {

    private Button Cancle;
    private Button Action;
    private TextView Status;
    private ListView EpcList;
    private boolean inSearch = false;
    private List<EpcDataBase> firm = new ArrayList<EpcDataBase>();
    private ArrayAdapter<EpcDataBase> adapter;
    private Activity cont;
    private SoundPool soundPool;
    private int soundId;
    private long scant = 0;
    private CheckBox cbb;
    private IUHFService iuhfService;
    private String model;

    public SearchTagDialog(Activity context, IUHFService iuhfService, String model) {
        super(context);
        cont = context;
        this.iuhfService = iuhfService;
        this.model = model;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setreader);

        Cancle = (Button) findViewById(R.id.btn_search_cancle);
        Cancle.setOnClickListener(this);
        Action = (Button) findViewById(R.id.btn_search_action);
        Action.setOnClickListener(this);

        cbb = (CheckBox) findViewById(R.id.checkBox_beep);

        Status = (TextView) findViewById(R.id.textView_search_status);
        EpcList = (ListView) findViewById(R.id.listView_search_epclist);
        EpcList.setOnItemClickListener(this);

        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        if (soundPool == null) {
            Log.e("as3992", "Open sound failed");
        }
        soundId = soundPool.load("/system/media/audio/ui/VideoRecord.ogg", 0);
        Log.w("as3992_6C", "id is " + soundId);


        //新的Listener回调参考代码

        adapter = new ArrayAdapter<EpcDataBase>(
                cont, android.R.layout.simple_list_item_1, firm);
        EpcList.setAdapter(adapter);

        iuhfService.setOnInventoryListener(new OnSpdInventoryListener() {
            @Override
            public void getInventoryData(SpdInventoryData var1) {
                handler.sendMessage(handler.obtainMessage(1, var1));
            }
        });
    }

    //新的Listener回调参考代码
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    scant++;
                    if (!cbb.isChecked()) {
                        if (scant % 10 == 0) {
                            soundPool.play(soundId, 1, 1, 0, 0, 1);
                        }
                    }
                    SpdInventoryData var1 = (SpdInventoryData) msg.obj;
                    int j;
                    for (j = 0; j < firm.size(); j++) {
                        if (var1.epc.equals(firm.get(j).epc)) {
                            firm.get(j).valid++;
                            firm.get(j).setRssi(var1.rssi);
                            break;
                        }
                    }
                    if (j == firm.size()) {
                        firm.add(new EpcDataBase(var1.epc, 1,
                                var1.rssi, var1.tid));
                        if (cbb.isChecked()) {
                            soundPool.play(soundId, 1, 1, 0, 0, 1);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    Status.setText("Total: " + firm.size());
                    break;
            }

        }
    };

    @Override
    protected void onStop() {
        Log.w("stop", "im stopping");
        if (inSearch) {
            iuhfService.newInventoryStop();
            inSearch = false;
        }
        soundPool.release();
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        if (v == Cancle) {
            soundPool.release();
            dismiss();
        } else if (v == Action) {
            if (inSearch) {
                inSearch = false;
                this.setCancelable(true);
                iuhfService.newInventoryStop();

                Action.setText(R.string.Start_Search_Btn);
                Cancle.setEnabled(true);
            } else {
                inSearch = true;
                this.setCancelable(false);
                scant = 0;
                iuhfService.select_card(1, "", false);
                iuhfService.newInventoryStart();
                Action.setText(R.string.Stop_Search_Btn);
                Cancle.setEnabled(false);
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        for (int i = 0; i < 48; i++) {
//                            int ii = switchManage.switchUhf(i);
//                            if (ii == 0) {
//                                cont.runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        Action.setText(R.string.Start_Search_Btn);
//                                    }
//                                });
//                                SystemClock.sleep(3);
//                            } else {
//                                return;
//                            }
//                        }
//                    }
//                }).start();

//                iuhfService.select_card(1, "", false);
//                iuhfService.newInventoryStart();

            }
        }
    }

    class EpcDataBase {
        String epc;
        int valid;
        String rssi;
        String tid_user;

        public EpcDataBase(String e, int v, String rssi, String tid_user) {
            // TODO Auto-generated constructor stub
            epc = e;
            valid = v;
            this.rssi = rssi;
            this.tid_user = tid_user;
        }

        public String getRssi() {
            return rssi;
        }

        public void setRssi(String rssi) {
            this.rssi = rssi;
        }

        @Override
        public String toString() {
            if (TextUtils.isEmpty(tid_user)) {
                return "EPC:" + epc + "\n"
                        + "(" + "COUNT:" + valid + ")" + " RSSI:" + rssi + "\n";
            } else {
                return "EPC:" + epc + "\n"
                        + "T/U:" + tid_user + "\n"
                        + "(" + "COUNT:" + valid + ")" + " RSSI:" + rssi + "\n";
            }
        }

    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                            long arg3) {
        // TODO Auto-generated method stub
        if (inSearch) {
            return;
        }

        String epcStr = firm.get(arg2).epc;
        int res = iuhfService.select_card(1, epcStr, true);
        if (res == 0) {
            EventBus.getDefault().post(new MsgEvent("set_current_tag_epc", epcStr));
            dismiss();
        } else {
            Status.setText(R.string.Status_Select_Card_Faild);
        }
    }
}
