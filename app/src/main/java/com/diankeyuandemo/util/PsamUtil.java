package com.diankeyuandemo.util;

import android.content.Context;
import android.os.SystemClock;

import com.speedata.libutils.DataConversionUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import speedatacom.a3310libs.PsamManager;
import speedatacom.a3310libs.inf.IPsam;

public class PsamUtil {

    private static IPsam psamIntance;

    public static void initePsam(Context context) {
        psamIntance = PsamManager.getPsamIntance();
        try {
            psamIntance.initDev(context);//初始化设备
            psamIntance.resetDev();//复位
            SystemClock.sleep(2000);
            byte[] data = psamIntance.PsamPower(IPsam.PowerType.Psam1);
            if (data != null) {
            } else {
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private static final String A4CMD = "00A4000002DF02";
    private static final String FACTORY_CODING = "00B0810701";
    private static final String GET_RESULT = "00C0000004";

    public static void uhfGettidAndWrittCard() {

    }

    public static byte[] result(byte[] Tid) {
        byte[] data = null;
        try {
            data = psamIntance.WriteCmd(DataConversionUtils.hexStringToByteArray(A4CMD), IPsam
                    .PowerType.Psam1);
            if (DataConversionUtils.byteArrayToString(data).equals("6108")) {
                data = psamIntance.WriteCmd(DataConversionUtils.hexStringToByteArray(FACTORY_CODING), IPsam
                        .PowerType.Psam1);
                if (DataConversionUtils.byteArrayToString(DataConvertUtil.cutBytes(data, 1, 2)).equals("9000")) {
                    data = DataConvertUtil.cutBytes(data, 0, 1);
                    String Factory_code = DataConversionUtils.byteArrayToString(data);
                    String CMD1 = "801A070118" + DataConversionUtils.byteArrayToString(Tid) + "00000000000000" + Factory_code;
                    data = psamIntance.WriteCmd(DataConversionUtils.hexStringToByteArray(CMD1), IPsam.PowerType.Psam1);
                    if (DataConversionUtils.byteArrayToString(data).equals("9000")) {
                        //80FA0500200000000000000000000000000000000000000000000000+厂商代码（1字节）+
                        // 标签生产日期（年月日时分：yymmddhhmm，例：1711201816）+800000
                        String time = DataConvertUtil.getNowTime();
                        String CMD2 = "80FA0500200000000000000000000000000000000000000000000000" + Factory_code + time + 800000;
                        data = psamIntance.WriteCmd(DataConversionUtils.hexStringToByteArray(CMD2), IPsam.PowerType.Psam1);
                        if (DataConversionUtils.byteArrayToString(data).equals("6104")) {
                            data = psamIntance.WriteCmd(DataConversionUtils.hexStringToByteArray(GET_RESULT), IPsam.PowerType.Psam1);
                            if (DataConversionUtils.byteArrayToString(DataConvertUtil.cutBytes(data, 4, 2)).equals("9000")) {
                                data = DataConvertUtil.cutBytes(data, 0, 4);
                                String result = Factory_code + time + DataConversionUtils.byteArrayToString(data) + "00000000000000000000";
                                data = DataConversionUtils.HexString2Bytes(result);
                            }
                        }
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return data;

    }
}
