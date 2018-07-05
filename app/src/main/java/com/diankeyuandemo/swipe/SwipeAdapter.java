package com.diankeyuandemo.swipe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.diankeyuandemo.R;

import java.util.List;

/**
 * Created by Horrarndoo on 2017/3/17.
 */

public class SwipeAdapter extends BaseAdapter implements SwipeLayout.OnSwipeStateChangeListener {
    private Context mContext;
    private List<ManagerDatas> list;
    private MyClickListener myClickListener;
    private SwipeLayoutManager swipeLayoutManager;

    public SwipeAdapter(Context mContext, List<ManagerDatas> list) {
        super();
        this.mContext = mContext;
        myClickListener = new MyClickListener();
        this.list = list;
        swipeLayoutManager = SwipeLayoutManager.getInstance();
    }

    public void setList(List<ManagerDatas> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_layout_managet, null);
        }
        ViewHolder holder = ViewHolder.getHolder(convertView);
        holder.fName.setText(list.get(position).getfNname());
        holder.fNum.setText(list.get(position).getfNum());
        holder.dataNum.setText(list.get(position).getNum() + "");
        holder.sv_layout.setOnSwipeStateChangeListener(this);
        holder.sv_layout.setTag(position);
        holder.tv_change.setOnClickListener(myClickListener);
        holder.tv_change.setTag(position);
        holder.tv_delete.setOnClickListener(myClickListener);
        holder.tv_delete.setTag(position);

        holder.sv_layout.getContentView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeLayoutManager.closeUnCloseSwipeLayout();
                swipeLayoutManager.closeUnCloseSwipeLayout(false);
                if (onSwipeControlListener != null) {
                    onSwipeControlListener.onItemClick(position);
                }
            }
        });

        return convertView;
    }

    static class ViewHolder {
        TextView fName, fNum, dataNum, tv_change, tv_delete;
        SwipeLayout sv_layout;

        public ViewHolder(View convertView) {
            fName = convertView.findViewById(R.id.f_name);
            fNum = convertView.findViewById(R.id.f_num);
            dataNum = convertView.findViewById(R.id.tv_num);
            tv_change = (TextView) convertView.findViewById(R.id.tv_change);
            tv_delete = (TextView) convertView.findViewById(R.id.tv_delete);
            sv_layout = convertView.findViewById(R.id.sv_layout);


        }

        public static ViewHolder getHolder(View convertView) {
            ViewHolder holder = (ViewHolder) convertView.getTag();
            if (holder == null) {
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }
            return holder;
        }
    }

    class MyClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Integer position = (Integer) v.getTag();
            switch (v.getId()) {
                case R.id.tv_change:
                    //ToastUtils.showToast("position : " + position + " overhead is clicked.");
                    swipeLayoutManager.closeUnCloseSwipeLayout(false);
                    if (onSwipeControlListener != null) {
                        onSwipeControlListener.onChangen(position);
                    }
                    break;
                case R.id.tv_delete:
                    //ToastUtils.showToast("position : " + position + " delete is clicked.");
                    swipeLayoutManager.closeUnCloseSwipeLayout(false);
                    if (onSwipeControlListener != null) {
                        onSwipeControlListener.onDelete(position);
                    }
                    break;
                case R.id.sv_layout:


                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onOpen(SwipeLayout swipeLayout) {
        //ToastUtils.showToast(swipeLayout.getTag() + "onOpen.");
    }

    @Override
    public void onClose(SwipeLayout swipeLayout) {
        //ToastUtils.showToast(swipeLayout.getTag() + "onClose.");
    }

    @Override
    public void onStartOpen(SwipeLayout swipeLayout) {
        //            ToastUtils.showToast("onStartOpen.");
    }

    @Override
    public void onStartClose(SwipeLayout swipeLayout) {
        //            ToastUtils.showToast("onStartClose.");
    }

    private OnSwipeControlListener onSwipeControlListener;

    public void setOnSwipeControlListener(OnSwipeControlListener onSwipeControlListener) {
        this.onSwipeControlListener = onSwipeControlListener;
    }

    /**
     * overhead 和 delete点击事件接口
     */
    public interface OnSwipeControlListener {
        void onChangen(int position);

        void onDelete(int position);

        void onItemClick(int position);
    }
}
