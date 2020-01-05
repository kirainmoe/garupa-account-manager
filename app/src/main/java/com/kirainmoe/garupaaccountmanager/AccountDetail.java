package com.kirainmoe.garupaaccountmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;

import org.json.JSONException;
import org.json.JSONStringer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Date;

public class AccountDetail {
    public String name;                 // 账户的名称
    public String md5;                  // 账号文件的 MD5
    public String description;          // 账号文件的说明
    public String inheritID;            // 引继码的ID
    public String inheritPassword;      // 引继码的密码
    public String timestamp;            // 创建的时间戳

    public AccountDetail(String name, String md5, String inheritID, String inheritPassword, String description) {
        this.name = name;
        this.md5 = md5;
        this.description = description;
        this.inheritID = inheritID;
        this.inheritPassword = inheritPassword;
        this.timestamp = (new Date()).toString();
    }

    public String toJSON() {
        JSONStringer stringer = new JSONStringer();
        try {
            stringer.object()
                    .key("name")
                    .value(this.name)
                    .key("description")
                    .value(this.description)
                    .key("md5")
                    .value(this.md5)
                    .key("inheritID")
                    .value(this.inheritID)
                    .key("inheritPassword")
                    .value(this.inheritPassword)
                    .key("created")
                    .value(this.timestamp)
                    .endObject();

            String result = stringer.toString();
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void applyCurrentFile() {
        // 判断是否备份当前账号
        boolean isBackedup = AppUtils.checkCurrentAccountAdded("");
        if (!isBackedup) {
            AppUtils.createFromCurrentAccountFile("Untitled", "应用其它账号时，为防止原账号丢失自动创建的备份");
            MainActivity.getInstance().renderAccountList();

            final AccountDetail ref = this;

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.getInstance());
            builder.setTitle(R.string.current_account_backed_up)
                    .setMessage(R.string.current_account_backed_up_description)
                    .setPositiveButton(R.string.fine, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ref.doApply();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            this.doApply();
        }
    }

    protected void doApply() {
        // 应用当前账号
        File bandoriAccountFile = new File(AppUtils.garupaAccountPath);
        boolean delResult = bandoriAccountFile.delete();
        File targetFile = new File(AppUtils.appDataPath + "/" + this.md5 + "/" + AppUtils.accountFileName);
        try {
            FileInputStream fin = new FileInputStream(targetFile);
            FileOutputStream fout = new FileOutputStream(bandoriAccountFile);
            FileChannel fcin = fin.getChannel(), fcout = fout.getChannel();
            fcin.transferTo(0, fcin.size(), fcout);

            fin.close();
            fout.close();
            fcin.close();
            fcout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 写入配置文件
        AppUtils.updateCurrentAccount(this.md5);

        // 返回结果
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.getInstance());
        builder.setTitle(R.string.success)
                .setMessage("成功应用了账号: " + this.name)
                .setPositiveButton(R.string.fine, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}