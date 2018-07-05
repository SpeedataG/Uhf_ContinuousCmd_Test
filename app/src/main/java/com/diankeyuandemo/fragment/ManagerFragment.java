package com.diankeyuandemo.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;

import com.diankeyuandemo.R;
import com.diankeyuandemo.db.DataDb;
import com.diankeyuandemo.swipe.ManagerDatas;
import com.diankeyuandemo.swipe.SwipeAdapter;
import com.diankeyuandemo.swipe.SwipeLayoutManager;
import com.diankeyuandemo.util.DBUitl;
import com.diankeyuandemo.util.DialogChange;
import com.diankeyuandemo.util.InsertDialog;

import java.util.ArrayList;
import java.util.List;

public class ManagerFragment extends android.support.v4.app.Fragment implements SwipeAdapter.OnSwipeControlListener, View.OnClickListener {
    private SwipeAdapter swipeAdapter;
    private SwipeLayoutManager swipeLayoutManager;
    private ListView listMnager;
    private List<ManagerDatas> managerList = new ArrayList<>();
    private DBUitl dbUitl;
    private Button btnInsert;
    private ManagerDatas managerDatas;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_mananger, container, false);
        listMnager = v.findViewById(R.id.list_manager);
        btnInsert = v.findViewById(R.id.btn_insert);
        btnInsert.setOnClickListener(this);
        dbUitl = new DBUitl();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.update");
        getActivity().registerReceiver(broadcastReceiver, intentFilter);
        initSwipView();
        managerList.clear();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        initInfo();
//        swipeAdapter.notifyDataSetChanged();
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.update".equals(intent.getAction())) {
                initInfo();
            }
        }
    };

    private void initInfo() {
        List<DataDb> list = dbUitl.queryAll();
        managerList.clear();
        for (int i = 0; i < list.size(); i++) {
            managerDatas = new ManagerDatas(i + 1, list.get(i).getFactorNum(), list.get(i).getFactorNname());
            managerList.add(managerDatas);
            swipeAdapter.notifyDataSetChanged();
        }
    }

    private void initSwipView() {
        swipeLayoutManager = SwipeLayoutManager.getInstance();
        swipeAdapter = new SwipeAdapter(getActivity(), managerList);

        listMnager.setAdapter(swipeAdapter);
        listMnager.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                swipeLayoutManager.closeUnCloseSwipeLayout();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        swipeAdapter.setOnSwipeControlListener(this);
    }

    @Override
    public void onChangen(int position) {
        DialogChange.showCustomizeDialog(getActivity(), managerList.get(position).getfNum());
    }

    @Override
    public void onDelete(int position) {
        dbUitl.delete(managerList.get(position).getfNum());
        managerList.remove(position);
        swipeAdapter.notifyDataSetChanged();

    }

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onClick(View view) {
        InsertDialog.showCustomizeDialog(getActivity());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(broadcastReceiver);
    }
}
