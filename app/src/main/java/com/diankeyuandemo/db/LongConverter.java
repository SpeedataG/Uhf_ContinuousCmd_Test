package com.diankeyuandemo.db;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.greendao.converter.PropertyConverter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LongConverter implements PropertyConverter<List<Long>, String> {


    private final Gson mGson;

    public LongConverter() {
        mGson = new Gson();
    }

    @Override
    public List<Long> convertToEntityProperty(String databaseValue) {
        Type type = new TypeToken<ArrayList<Long>>() {
        }.getType();
        ArrayList<Long> list = mGson.fromJson(databaseValue, type);
        return list;
    }

    @Override
    public String convertToDatabaseValue(List<Long> entityProperty) {
        String dbString = mGson.toJson(entityProperty);
        return dbString;
    }


}

