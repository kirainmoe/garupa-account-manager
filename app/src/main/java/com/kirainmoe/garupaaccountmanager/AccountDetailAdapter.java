package com.kirainmoe.garupaaccountmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class AccountDetailAdapter extends BaseAdapter {
    private final int resId;
    private ArrayList<AccountDetail> accountList;
    private Context parentContext;

    public AccountDetailAdapter(Context context, int id, ArrayList<AccountDetail> list) {
        resId = id;
        accountList = list;
        parentContext = context;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(this.parentContext).inflate(this.resId, parent, false);
        AccountDetail item = this.accountList.get(position);
        TextView nameView = (TextView) view.findViewById(R.id.account_name);
        TextView descView = (TextView) view.findViewById(R.id.account_description);
        nameView.setText(item.name);
        descView.setText(item.description);

        if (item.md5.equals(AppUtils.getCurrentAccount(false))) {
            view.setBackgroundColor(0xffff5252);
            nameView.setTextColor(0xffffffff);
            descView.setTextColor(0xffffffff);
        }

        return view;
    }

    @Override
    public int getCount() {
        return this.accountList.size();
    }

    public void redraw() {
        notifyDataSetChanged();
    }
}
