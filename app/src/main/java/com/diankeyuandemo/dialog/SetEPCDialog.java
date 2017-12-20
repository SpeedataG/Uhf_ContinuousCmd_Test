package com.diankeyuandemo.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.diankeyuandemo.R;
import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.bean.SpdWriteData;
import com.speedata.libuhf.interfaces.OnSpdWriteListener;
import com.speedata.libuhf.utils.StringUtils;

/**
 * Created by 张明_ on 2016/12/28.
 */

public class SetEPCDialog extends Dialog implements
        View.OnClickListener {

    private Button Ok;
    private Button Cancle;
    private TextView EPC;
    private TextView Status;
    private EditText passwd;
    private EditText newepc;
    private EditText newepclength;
    private IUHFService iuhfService;
    private String current_tag_epc;
    private boolean isSuccess = false;
    private Context mContext;

    public SetEPCDialog(Context context, IUHFService iuhfService, String current_tag_epc) {
        super(context);
        // TODO Auto-generated constructor stub
        this.iuhfService = iuhfService;
        this.current_tag_epc = current_tag_epc;
        this.mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setepc);

        Ok = (Button) findViewById(R.id.btn_epc_ok);
        Ok.setOnClickListener(this);
        Cancle = (Button) findViewById(R.id.btn_epc_cancle);
        Cancle.setOnClickListener(this);

        EPC = (TextView) findViewById(R.id.textView_epc_epc);
        EPC.setText(current_tag_epc);
        Status = (TextView) findViewById(R.id.textView_epc_status);

        passwd = (EditText) findViewById(R.id.editText_epc_passwd);
        newepc = (EditText) findViewById(R.id.editText_epc_newepc);
        newepclength = (EditText) findViewById(R.id.editText_epc_epclength);

        iuhfService.setOnWriteListener(new OnSpdWriteListener() {
            @Override
            public void getWriteData(SpdWriteData var1) {
                StringBuilder stringBuilder = new StringBuilder();
                byte[] epcData = var1.getEPCData();
                String hexString = StringUtils.byteToHexString(epcData, var1.getEPCLen());
                if (!TextUtils.isEmpty(hexString)) {
                    stringBuilder.append("EPC：" + hexString + "\n");
                }
                if (var1.getStatus() == 0) {
                    //状态判断，已经写卡成功了就不返回错误码了
                    isSuccess = true;
                    stringBuilder.append("WriteSuccess" + "\n");
                    handler.sendMessage(handler.obtainMessage(1, stringBuilder));
                } else {
                    stringBuilder.append("WriteError：" + var1.getStatus() + "\n");
                }
                if (!isSuccess) {
                    handler.sendMessage(handler.obtainMessage(1, stringBuilder));
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == Ok) {
            final String password = passwd.getText().toString().replace(" ", "");
            final String epc_str = newepc.getText().toString().replace(" ", "");
            String count_str = newepclength.getText().toString();
            if (TextUtils.isEmpty(password) || TextUtils.isEmpty(epc_str) || TextUtils.isEmpty(count_str)) {
                Toast.makeText(mContext, "参数不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            final byte[] write = StringUtils.stringToByte(epc_str);
            final int epcl;
            try {
                epcl = Integer.parseInt(count_str, 10);
            } catch (NumberFormatException e) {
                return;
            }
            Status.setText("正在写卡中....");
            isSuccess = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int writeArea = iuhfService.newWriteArea(1, 2, epcl, password, write);
                    if (writeArea != 0) {
                        handler.sendMessage(handler.obtainMessage(1,"参数不正确"));
                    }
                }
            }).start();

        } else if (v == Cancle) {
            dismiss();
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                if (msg.what == 1) {
                    Status.setText(msg.obj + "");
                }
            }
        }
    };
}
