package com.diankeyuandemo.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.diankeyuandemo.R;
import com.diankeyuandemo.db.DataDb;

public class ManagerDialog {
    private static EditText fNum, fname;
    private static DBUitl dbUitl = new DBUitl();

    public static void showCustomizeDialog(final Context context) {
        AlertDialog.Builder customizeDialog =
                new AlertDialog.Builder(context);

        final View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_change_layout, null);
        fNum = dialogView.findViewById(R.id.ed_change_num);
        fname = dialogView.findViewById(R.id.ed_change_name);
        customizeDialog.setTitle("");
        customizeDialog.setView(dialogView);

        customizeDialog.setPositiveButton("确定添加",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        changDatas(context);
                        Intent intent = new Intent();
                        intent.setAction("com.update");
                        context.sendBroadcast(intent);
                    }
                });
        customizeDialog.show();

    }

    public static void changDatas(Context context) {
        String pnum = String.valueOf(fNum.getText());
        String fnames = String.valueOf(fname.getText());
        if (!pnum.equals("") && !fnames.equals("")) {
            DataDb dataDb = new DataDb(fnames, pnum);
            dbUitl.insertDtata(dataDb);
            Toast.makeText(context, "添加成功！", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "信息不能为空！", Toast.LENGTH_SHORT).show();
        }
    }
}
