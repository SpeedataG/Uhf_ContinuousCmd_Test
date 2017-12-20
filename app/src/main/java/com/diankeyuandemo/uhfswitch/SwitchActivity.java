package com.diankeyuandemo.uhfswitch;

import android.os.Bundle;
import android.os.SystemClock;
import android.serialport.SerialPort;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.diankeyuandemo.R;
import com.speedata.libutils.DataConversionUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class SwitchActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    SerialPort serialPort;
    //    DeviceControl deviceControl;
    private int Fd;
    private List<byte[]> byteList = new ArrayList<>();
    private GridView gridView;
    List<Numuhf> numuhfs = new ArrayList<>();
    private GridAdapter gridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.swich_layout);
        initInfor();
        initDev();

    }

    private void initInfor() {
        gridView = findViewById(R.id.grid_view);
        gridAdapter = new GridAdapter(numuhfs, this);
        gridView.setAdapter(gridAdapter);
        gridView.setOnItemClickListener(this);
        for (int i = 1; i < 50; i++) {
            Numuhf numuhf = new Numuhf(i);
            numuhfs.add(numuhf);
        }
        gridAdapter.notifyDataSetChanged();
        byte bb = 0x30;
        for (int i = 0; i < 5; i++) {
            byte bb1 = 0x30;
            for (int j = 0; j < 10; j++) {
                byte[] switchCmd = {0x03, bb, bb1, 0x04};
                byte[] bbb = {bb, bb1};
                byteList.add(switchCmd);
                bb1++;
                Log.i("byte", "initInfor: " + i + j + "::::" + DataConversionUtils.byteArrayToString(switchCmd));
                Log.i("ss", "initInfor: " + i + j + "::::" + DataConversionUtils.byteArrayToAscii(bbb));
            }
            bb++;
        }
    }

    private void initDev() {

        try {
            serialPort = new SerialPort();
            serialPort.OpenSerial(SerialPort.SERIAL_TTYMT3, 9600);

            Fd = serialPort.getFd();
//            deviceControl = new DeviceControl(DeviceControl.PowerType.MAIN, 121);
//            deviceControl.PowerOnDevice();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Switch(byte[] switchCmd) {
        try {
            serialPort.WriteSerialByte(Fd, switchCmd);
            SystemClock.sleep(100);
            byte[] result = serialPort.ReadSerial(Fd, 512);
            if (result != null) {
                String d = DataConversionUtils.byteArrayToAscii(cutBytes(result, 1, result.length - 2));
                Toast.makeText(this, d, Toast.LENGTH_SHORT).show();
            }
//            Toast.makeText(this, DataConversionUtils.byteArrayToString(result), Toast.LENGTH_SHORT).show();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//        Toast.makeText(SwitchActivity.this, "onclick" + i, Toast.LENGTH_SHORT).show();
        Switch(byteList.get(i));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serialPort != null) {
            serialPort.CloseSerial(Fd);
        }
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
