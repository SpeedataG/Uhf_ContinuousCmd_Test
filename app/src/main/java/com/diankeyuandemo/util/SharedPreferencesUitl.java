package com.diankeyuandemo.util;

import android.annotation.SuppressLint;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * ----------Dragon be here!----------/
 * 　　　┏┓　　　┏┓
 * 　　┏┛┻━━━┛┻┓
 * 　　┃　　　　　　　┃
 * 　　┃　　　━　　　┃
 * 　　┃　┳┛　┗┳　┃
 * 　　┃　　　　　　　┃
 * 　　┃　　　┻　　　┃
 * 　　┃　　　　　　　┃
 * 　　┗━┓　　　┏━┛
 * 　　　　┃　　　┃神兽保佑
 * 　　　　┃　　　┃代码无BUG！
 * 　　　　┃　　　┗━━━┓
 * 　　　　┃　　　　　　　┣┓
 * 　　　　┃　　　　　　　┏┛
 * 　　　　┗┓┓┏━┳┓┏┛
 * 　　　　　┃┫┫　┃┫┫
 * 　　　　　┗┻┛　┗┻┛
 * ━━━━━━神兽出没━━━━━━
 *
 * @author :孙天伟 in  2017/9/27   13:28.
 *         联系方式:QQ:420401567
 *         功能描述:  SharedPreferences  用户偏好 存储数据
 */

public class SharedPreferencesUitl {
    private android.content.SharedPreferences sharedPreferences;
    private android.content.SharedPreferences.Editor editor;
    private static SharedPreferencesUitl preferencesUitl = null;


    @SuppressLint("WrongConstant")
    public SharedPreferencesUitl(Context context, String key) {
        sharedPreferences = context.getSharedPreferences(key, Context.MODE_APPEND);
        editor = sharedPreferences.edit();
    }

    public static SharedPreferencesUitl getInstance(Context context, String filename) {
        if (preferencesUitl == null) {
            preferencesUitl = new SharedPreferencesUitl(context, filename);
        }
        return preferencesUitl;
    }

    public boolean saveArrayL(String key, List<Long> list) {
        editor.putInt(key, list.size());

        for (int i = 0; i < list.size(); i++) {
            editor.remove(key + i);

            editor.putLong(key + i, list.get(i));
        }
        return editor.commit();
    }

    public void loadArrayL(String key, List<Long> list) {

        list.clear();
        int size = sharedPreferences.getInt(key, 0);
        if (size == 0) {
            list.add(0l);
        } else {
            for (int i = 0; i < size; i++) {
                list.add(sharedPreferences.getLong(key + i, 0l));
            }
        }
    }

    public boolean saveArrayS(String key, List<String> list) {
        editor.putInt(key, list.size());

        for (int i = 0; i < list.size(); i++) {
            editor.remove(key + i);

            editor.putString(key + i, list.get(i));
        }
        return editor.commit();
    }


    public List<String> loadArrayS(String key) {
        List<String> list = new ArrayList<>();
        int size = sharedPreferences.getInt(key, 0);
        for (int i = 0; i < size; i++) {
            list.add(sharedPreferences.getString(key + i, null));
        }
        return list;
    }

    public void write(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }


    public void write(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    public void write(String key, long value) {
        editor.putLong(key, value);
        editor.commit();
    }

    public void write(String key, float value) {
        editor.putFloat(key, value);
        editor.commit();
    }

    public void write(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public boolean read(String key, boolean defValue) {
        return sharedPreferences.getBoolean(key, defValue);
    }

    public long read(String key, long defValue) {
        return sharedPreferences.getLong(key, defValue);
    }

    public String read(String key, String defValue) {
        String string = defValue;
        try {
            string = sharedPreferences.getString(key, defValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return string;
    }

    public int read(String key, int defValue) {
        return sharedPreferences.getInt(key, defValue);
    }

    public float read(String key, float defValue) {
        return sharedPreferences.getFloat(key, defValue);
    }

    public void delete(String key) {
        editor.remove(key);
        editor.commit();
    }

    /**
     * 保存List
     *
     * @param tag
     * @param datalist
     */
    public <T> void setDataList(String tag, List<T> datalist) {
        if (null == datalist || datalist.size() <= 0)
            return;
        Gson gson = new Gson();
        //转换成json数据，再保存
        String strJson = gson.toJson(datalist);
        editor.clear();
        editor.putString(tag, strJson);
        editor.commit();

    }

    /**
     * 获取List
     *
     * @param tag
     * @return
     */
    public <T> List<T> getDataList(String tag) {
        List<T> datalist = new ArrayList<T>();
        String strJson = sharedPreferences.getString(tag, null);
        if (null == strJson) {
            return datalist;
        }
        Gson gson = new Gson();
        datalist = gson.fromJson(strJson, new TypeToken<List<T>>() {
        }.getType());
        return datalist;
    }
}
