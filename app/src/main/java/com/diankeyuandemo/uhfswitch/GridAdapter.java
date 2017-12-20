package com.diankeyuandemo.uhfswitch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.diankeyuandemo.R;

import java.util.List;

public class GridAdapter extends BaseAdapter {
    private Context context;

    private List<Numuhf> gridNums;

    public GridAdapter(List<Numuhf> gridNums, Context context) {
        super();
        this.gridNums = gridNums;
        this.context = context;
    }

    @Override
    public int getCount() {

        if (null != gridNums) {
            return gridNums.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {

        return gridNums.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(this.context).inflate(R.layout.item_gridview_layout, null);
        }
        ViewHolder viewHolder = ViewHolder.getHolder(convertView);
        viewHolder.num.setText(gridNums.get(position).getNumId() + "路");
        if (position == 48) {
            viewHolder.num.setText("关闭所有");
        }
        return convertView;
    }

    static class ViewHolder {
        public TextView num;

        public ViewHolder(View convertView) {
            num = convertView.findViewById(R.id.item_grid);
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

}
