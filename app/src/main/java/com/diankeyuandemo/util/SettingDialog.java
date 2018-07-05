package com.diankeyuandemo.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.diankeyuandemo.R;

public class SettingDialog {

    private static EditText oldPwd, newPwd;
    private static SharedPreferencesUitl preferencesUitl = null;

    public static void showCustomizeDialog(final Context context) {
        preferencesUitl = SharedPreferencesUitl.getInstance(context, "dainkeyuan");
        AlertDialog.Builder customizeDialog =
                new AlertDialog.Builder(context);

        final View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.setting_dialog, null);
        newPwd = dialogView.findViewById(R.id.ed_new_pwd);
        oldPwd = dialogView.findViewById(R.id.ed_old_pwd);
        customizeDialog.setTitle("修改密码");
        customizeDialog.setView(dialogView);

        customizeDialog.setPositiveButton("确定",

                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (preferencesUitl.read("pwd", "").equals("")) {
                            if ("admin".equals(oldPwd.getText().toString())) {
                                preferencesUitl.write("pwd", newPwd.getText().toString());
                                Toast.makeText(context, "密码修改成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "旧密码输入错误", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.i("tws", "onClick: " + preferencesUitl.read("pwd", ""));
                            if (preferencesUitl.read("pwd", "").equals(oldPwd.getText().toString())) {
                                preferencesUitl.write("pwd", newPwd.getText().toString());
                                Toast.makeText(context, "密码修改成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "旧密码输入错误", Toast.LENGTH_SHORT).show();
                            }
                        }


                    }
                });
        customizeDialog.show();

    }


}
