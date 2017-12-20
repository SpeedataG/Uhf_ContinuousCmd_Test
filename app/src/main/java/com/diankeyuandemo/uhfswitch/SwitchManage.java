package com.diankeyuandemo.uhfswitch;

import android.os.SystemClock;
import android.serialport.DeviceControl;
import android.serialport.SerialPort;
import android.util.Log;
import android.widget.GridView;

import com.speedata.libutils.DataConversionUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class SwitchManage implements SwitchInterFace {
    private SerialPort serialPort;
    private int Fd;
    private List<byte[]> byteList = new ArrayList<>();
    private GridView gridView;
    private List<Numuhf> numuhfs = new ArrayList<>();
    private GridAdapter gridAdapter;
    private static SwitchManage switchManage = null;
    private DeviceControl deviceControl;

    public SwitchManage() {
    }

    public static SwitchManage getInstance() {
        synchronized (SwitchManage.class) {
            if (switchManage == null) {
                switchManage = new SwitchManage();
            }
            return switchManage;
        }

    }

    @Override
    public void initDev() {
        try {
            serialPort = new SerialPort();
            serialPort.OpenSerial(SerialPort.SERIAL_TTYMT3, 9600);
            Fd = serialPort.getFd();
            deviceControl = new DeviceControl(DeviceControl.PowerType.MAIN, 121);
            deviceControl.PowerOnDevice();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @Override
    public int switchUhf(int i) {
        try {
            byte[] ss = byteList.get(i);
            serialPort.WriteSerialByte(Fd, byteList.get(i));
//            SystemClock.sleep(3);
            byte[] result = serialPort.ReadSerial(Fd, 512);
            if (result != null) {
                String d = DataConversionUtils.byteArrayToAscii(cutBytes(result, 1, result.length - 2));
                if ("Switch OK.".equals(d)) {
                    return 0; //正确切换

                } else if ("Switch OK. ".equals(d)) {
                    return 1;//关闭成功

                } else if ("Parameter Value Error. ".equals(d)) {
                    return 2;//参数值错误

                } else if ("Parameter Length Error. ".equals(d)) {
                    return 3;//参数长度错误

                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return 4;
    }

    @Override
    public void switchUhf() {
        for (int i = 0; i < 48; i++) {
            try {
                serialPort.WriteSerialByte(Fd, byteList.get(i));
                SystemClock.sleep(10);
                byte[] result = serialPort.ReadSerial(Fd, 512);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void closeDev() {
        if (serialPort != null) {
            serialPort.CloseSerial(Fd);
        }
        if (deviceControl != null) {
            try {
                deviceControl.PowerOffDevice();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
