package com.diankeyuandemo.util;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;


import com.speedata.libutils.DataConversionUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import speedatacom.a3310libs.PsamManager;
import speedatacom.a3310libs.inf.IPsam;


public class PsamUtil {

    private static IPsam psamIntance;
    private static Context mContext;

    /**
     * 初始化psam 模块
     * @param context 上下文对象
     */
    public static void initePsam(Context context) {
        mContext = context;
        psamIntance = PsamManager.getPsamIntance();
        try {
            psamIntance.initDev(context);//初始化设备
            psamIntance.resetDev();//复位
            SystemClock.sleep(2000);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private static final String A4CMD = "00A4000002DF02";
    private static final String FACTORY_CODING = "00B0810701";//返回厂商代码
    private static final String GET_RESULT = "00C0000004";
    private static final String cmd1 = "801A07011000000000E2801105200071199E6E08C7";
    private static final String cmd2 = "80FA0500200000000000000000000000000000000000000000000000011806011528800000";

    /**
     * 写卡 操作
     * @param Tid  uhf标签TID
     * @return
     */
    public static byte[] WriteResult(byte[] Tid) {
        byte[] data = null;
        try {
            data = psamIntance.PsamPower(IPsam.PowerType.Psam1);
            if (data != null) {

                data = psamIntance.WriteCmd(DataConversionUtils.hexStringToByteArray(A4CMD), IPsam
                        .PowerType.Psam1);
                Log.i("tw", "写卡1: " + A4CMD);
                Log.i("tw", "写卡1返回: " + DataConversionUtils.byteArrayToString(data));
                if (data != null) {
                    if (DataConversionUtils.byteArrayToString(data).equals("6108")) {
                        data = psamIntance.WriteCmd(DataConversionUtils.hexStringToByteArray(FACTORY_CODING), IPsam
                                .PowerType.Psam1);
                        Log.i("tw", "写卡2: " + FACTORY_CODING);
                        Log.i("tw", "写卡2返回: " + DataConversionUtils.byteArrayToString(data));
                        if (data != null && DataConversionUtils.byteArrayToString(DataConvertUtil.cutBytes(data, 1, 2)).equals("9000")) {
                            data = DataConvertUtil.cutBytes(data, 0, 1);
                            String Factory_code = DataConversionUtils.byteArrayToString(data);//获取到厂商代码
                            Log.i("tw", "写卡tid " + DataConversionUtils.byteArrayToString(Tid));
                            //生成校验数据
                            String CMD1 = "801A07011000000000" + DataConversionUtils.byteArrayToString(Tid).toUpperCase();
                            Log.i("tw", "写卡3: " + CMD1);
                            data = psamIntance.WriteCmd(DataConversionUtils.hexStringToByteArray(CMD1), IPsam.PowerType.Psam1);
                            Log.i("tw", "写卡3返回: " + DataConversionUtils.byteArrayToString(data));

                            if (data != null && DataConversionUtils.byteArrayToString(data).equals("9000")) {
                                //80FA0500200000000000000000000000000000000000000000000000+厂商代码（1字节）+
                                // 标签生产日期（年月日时分：yymmddhhmm，例：1711201816）+800000
                                String time = DataConvertUtil.getNowTime();
                                String CMD2 = "80FA0500200000000000000000000000000000000000000000000000" + Factory_code + time + 800000;
                                Log.i("tw", "写卡4: " + CMD2);
                                data = psamIntance.WriteCmd(DataConversionUtils.hexStringToByteArray(CMD2), IPsam.PowerType.Psam1);
                                Log.i("tw", "写卡4返回: " + DataConversionUtils.byteArrayToString(data));

                                if (data != null && DataConversionUtils.byteArrayToString(data).equals("6104")) {

                                    Log.i("tw", "写卡5: " + GET_RESULT);
                                    data = psamIntance.WriteCmd(DataConversionUtils.hexStringToByteArray(GET_RESULT), IPsam.PowerType.Psam1);
                                    Log.i("tw", "写卡结果返回: " + DataConversionUtils.byteArrayToString(data));
                                    if (data != null && DataConversionUtils.byteArrayToString(DataConvertUtil.cutBytes(data, 4, 2)).equals("9000")) {
                                        data = DataConvertUtil.cutBytes(data, 0, 4);
                                        String result = Factory_code + time + DataConversionUtils.byteArrayToString(data) + "00000000000000000000";
                                        data = DataConversionUtils.HexString2Bytes(result);
                                        Log.i("tw", "写卡结果: " + result);
                                    } else {
                                        return data;
                                    }
                                } else {
                                    return data;
                                }
                            } else {
                                return data;
                            }
                        } else {
                            return data;
                        }
                    }
                }
            } else {

            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return data;

    }

    /**
     * 验证操作
     * @param num   厂商编号
     * @param tid   uhf标签TID
     * @param time  时间
     * @return
     */
    public static byte[] checkOutResult(String num, byte[] tid, String time) {
        byte[] data = null;
        try {
            data = psamIntance.PsamPower(IPsam.PowerType.Psam2);
            if (data != null) {
                data = psamIntance.WriteCmd(DataConversionUtils.hexStringToByteArray(A4CMD), IPsam
                        .PowerType.Psam2);
                Log.i("tw", "校验1: " + A4CMD);
                Log.i("tw", "校验1返回: " + DataConversionUtils.byteArrayToString(data));

                if (data != null && DataConversionUtils.byteArrayToString(data).equals("6108")) {
                    String CMD1 = "801A07011800000000" + DataConversionUtils.byteArrayToString(tid).toUpperCase() + "00000000000000" + num;
                    Log.i("tw", "校验2: " + CMD1);
                    data = psamIntance.WriteCmd(DataConversionUtils.hexStringToByteArray(CMD1), IPsam.PowerType.Psam2);
                    Log.i("tw", "校验2返回: " + DataConversionUtils.byteArrayToString(data));
                    if (data != null && DataConversionUtils.byteArrayToString(data).equals("9000")) {
                        //80FA0500200000000000000000000000000000000000000000000000+厂商代码（1字节）+
                        // 标签生产日期（年月日时分：yymmddhhmm，例：1711201816）+800000
//                    String time = DataConvertUtil.getNowTime();
                        String CMD2 = "80FA0500200000000000000000000000000000000000000000000000" + num + time + "800000";
                        Log.i("tw", "校验3: " + CMD2);
                        data = psamIntance.WriteCmd(DataConversionUtils.hexStringToByteArray(CMD2), IPsam.PowerType.Psam2);
                        Log.i("tw", "校验3返回: " + DataConversionUtils.byteArrayToString(data));
                        if (data != null && DataConversionUtils.byteArrayToString(data).equals("6104")) {
                            Log.i("tw", "校验4: " + GET_RESULT);
                            data = psamIntance.WriteCmd(DataConversionUtils.hexStringToByteArray(GET_RESULT), IPsam.PowerType.Psam2);
                            Log.i("tw", "校验结果返回: " + DataConversionUtils.byteArrayToString(data));
                            if (data != null && DataConversionUtils.byteArrayToString(DataConvertUtil.cutBytes(data, 4, 2)).equals("9000")) {
                                data = DataConvertUtil.cutBytes(data, 0, 4);
//                            String result = num + time + DataConversionUtils.byteArrayToString(data) + "00000000000000000000";
//                            data = DataConversionUtils.HexString2Bytes(result);
                                Log.i("tw", "校验结果: " + DataConversionUtils.byteArrayToString(data));
                            } else {
                                return data;
                            }
                        } else {
                            return data;
                        }
                    } else {
                        return data;
                    }
                } else {
                    return data;
                }
            } else {
                return data;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 释放
     */
    private void closePsam() {
        try {
            psamIntance.releaseDev();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
