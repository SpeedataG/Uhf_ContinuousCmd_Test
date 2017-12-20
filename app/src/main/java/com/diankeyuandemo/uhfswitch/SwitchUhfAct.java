package com.diankeyuandemo.uhfswitch;

import android.app.Activity;
import android.os.Bundle;
import android.serialport.DeviceControl;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.diankeyuandemo.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SwitchUhfAct extends Activity implements AdapterView.OnItemClickListener {
    DeviceControl deviceControl;
    private GridView gridView;
    private GridAdapter gridAdapter;
    List<Numuhf> numuhfs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);
        initInfor();
        initDev();

    }

    private void initInfor() {
        gridView = findViewById(R.id.grid_view2);
        gridAdapter = new GridAdapter(numuhfs, this);
        gridView.setAdapter(gridAdapter);
        gridView.setOnItemClickListener(this);
        for (int i = 1; i < 9; i++) {
            Numuhf numuhf = new Numuhf(i);
            numuhfs.add(numuhf);
        }
        gridAdapter.notifyDataSetChanged();
    }

    private void initDev() {
        try {
            deviceControl = new DeviceControl(DeviceControl.PowerType.MAIN);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Switch(int witchCmd) {
        switch (witchCmd) {
            case 2:
                try {
                    deviceControl.MainPowerOff(121);
                    deviceControl.setDir(66,1,DeviceControl.POWER_MAIN);
                    deviceControl.MainPowerOn(66);
                    deviceControl.MainPowerOff(67);
                    deviceControl.MainPowerOff(68);
                    deviceControl.MainPowerOn(121);
                    Toast.makeText(this, "切换3路", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 0:
                try {
                    deviceControl.MainPowerOff(121);
                    deviceControl.setDir(67,1,DeviceControl.POWER_MAIN);
                    deviceControl.MainPowerOn(67);
                    deviceControl.MainPowerOff(66);
                    deviceControl.MainPowerOff(68);
                    deviceControl.MainPowerOn(121);
                    Toast.makeText(this, "切换1路", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 7:
                try {
                    deviceControl.MainPowerOff(121);
                    deviceControl.setDir(68,1,DeviceControl.POWER_MAIN);
                    deviceControl.MainPowerOn(68);
                    deviceControl.MainPowerOff(67);
                    deviceControl.MainPowerOff(66);
                    deviceControl.MainPowerOn(121);
                    Toast.makeText(this, "切换8路", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 5:
                try {
                    deviceControl.MainPowerOff(121);
                    deviceControl.setDir(66,1,DeviceControl.POWER_MAIN);
                    deviceControl.setDir(67,1,DeviceControl.POWER_MAIN);
                    deviceControl.setDir(68,1,DeviceControl.POWER_MAIN);
                    deviceControl.MainPowerOn(66);
                    deviceControl.MainPowerOn(67);
                    deviceControl.MainPowerOn(68);
                    deviceControl.MainPowerOn(121);
                    Toast.makeText(this, "切换6路", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 1:
                try {
                    deviceControl.MainPowerOff(121);
                    deviceControl.setDir(66,1,DeviceControl.POWER_MAIN);
                    deviceControl.setDir(67,1,DeviceControl.POWER_MAIN);
                    deviceControl.MainPowerOn(66);
                    deviceControl.MainPowerOn(67);
                    deviceControl.MainPowerOff(68);
                    deviceControl.MainPowerOn(121);
                    Toast.makeText(this, "切换2路", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 6:
                try {
                    deviceControl.MainPowerOff(121);
                    deviceControl.setDir(66,1,DeviceControl.POWER_MAIN);
                    deviceControl.setDir(68,1,DeviceControl.POWER_MAIN);
                    deviceControl.MainPowerOn(66);
                    deviceControl.MainPowerOn(68);
                    deviceControl.MainPowerOff(67);
                    deviceControl.MainPowerOn(121);
                    Toast.makeText(this, "切换7路", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 4:
                try {
                    deviceControl.MainPowerOff(121);
                    deviceControl.setDir(67,1,DeviceControl.POWER_MAIN);
                    deviceControl.setDir(68,1,DeviceControl.POWER_MAIN);
                    deviceControl.MainPowerOn(67);
                    deviceControl.MainPowerOn(68);
                    deviceControl.MainPowerOff(66);
                    deviceControl.MainPowerOn(121);
                    Toast.makeText(this, "切换5路", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 3:
                try {
                    deviceControl.MainPowerOff(121);
                    deviceControl.MainPowerOff(66);
                    deviceControl.MainPowerOff(67);
                    deviceControl.MainPowerOff(68);
                    deviceControl.MainPowerOn(121);
                    Toast.makeText(this, "切换4路", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Switch(i);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 截取数组
     *
     * @param bytes  被截取数组
     * @param start  被截取数组开始截取位置
     * @param length 新数组的长度
     * @return 新数组
     */
    public static byte[] cutBytes(byte[] bytes, int start, int length) {
        byte[] res = new byte[length];
        System.arraycopy(bytes, start, res, 0, length);
        return res;
    }
}
