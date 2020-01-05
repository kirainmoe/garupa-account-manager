package com.kirainmoe.garupaaccountmanager;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import android.os.Environment;

import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.ArrayList;

public class AppUtils {

    public static String garupaAccountPath = Environment.getExternalStorageDirectory().getPath()
            +  "/Android/data/jp.co.craftegg.band/files/EhNfQ7brV3f3cCIcW9O4OaZxwC0V6UH1";
    public static String appDataPath = Environment.getExternalStorageDirectory().getPath()
            + "/Android/data/com.kirainmoe.garupaaccountmanager";
    public static String appConfigPath = appDataPath + "/config.json";
    public static String accountFileName = "EhNfQ7brV3f3cCIcW9O4OaZxwC0V6UH1";
    public static ArrayList<AccountDetail> accounts;
    public static String currentAccount;

    /**
     * 计算文件的 MD5 校验值
     * @param file 待计算的文件
     * @return 返回计算后的结果
     */
    public static String calcFileMd5(File file) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            FileInputStream fin = new FileInputStream(file);
            int len = 0;
            byte[] buffer = new byte[1024 * 100];

            while ((len = fin.read(buffer)) > 0) {
                md.update(buffer, 0, len);
            }

            BigInteger bigInt = new BigInteger(1, md.digest());
            String md5 = bigInt.toString(16);
            while (md5.length() < 32) {
                md5 = "0" + md5;
            }

            fin.close();
            return md5;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 备份当前账号相关信息
     * @return void
     */
    public static void createFromCurrentAccountFile(String title, String description) {
        File accountFile = new File(AppUtils.garupaAccountPath);
        String md5 = AppUtils.calcFileMd5(accountFile);
        String currentAccountPath = AppUtils.appDataPath + "/" + md5;
        File currentAccountDir = new File(currentAccountPath);
        if (!currentAccountDir.exists()) {
            boolean res = currentAccountDir.mkdir();
            try {
                FileInputStream fin = new FileInputStream(accountFile);
                FileOutputStream fout = new FileOutputStream(currentAccountDir + "/" + AppUtils.accountFileName);
                FileChannel fcin = fin.getChannel(), fcout = fout.getChannel();
                fcin.transferTo(0, fcin.size(), fcout);

                fin.close();
                fout.close();
                fcin.close();
                fcout.close();

                // 写入 JSON 信息
                AccountDetail cur = new AccountDetail(title, md5, "", "", description);
                String json = cur.toJSON();
                FileOutputStream jsonfout = new FileOutputStream(new File(currentAccountDir + "/account.json"));
                jsonfout.write(json.getBytes());
                jsonfout.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从其他位置备份账号相关信息
     * @return void
     */
    public static void createFromSomewhere(String path, String title, String description) {
        File accountFile = new File(path);
        String md5 = AppUtils.calcFileMd5(accountFile);
        String currentAccountPath = AppUtils.appDataPath + "/" + md5;
        File currentAccountDir = new File(currentAccountPath);
        if (!currentAccountDir.exists()) {
            boolean res = currentAccountDir.mkdir();
            try {
                FileInputStream fin = new FileInputStream(accountFile);
                FileOutputStream fout = new FileOutputStream(currentAccountDir + "/" + AppUtils.accountFileName);
                FileChannel fcin = fin.getChannel(), fcout = fout.getChannel();
                fcin.transferTo(0, fcin.size(), fcout);

                fin.close();
                fout.close();
                fcin.close();
                fcout.close();

                // 写入 JSON 信息
                AccountDetail cur = new AccountDetail(title, md5, "", "", description);
                String json = cur.toJSON();
                FileOutputStream jsonfout = new FileOutputStream(new File(currentAccountDir + "/account.json"));
                jsonfout.write(json.getBytes());
                jsonfout.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 读取程序数据文件夹，获取账号列表
     * @return 返回读取的有效结果
     */
    public static ArrayList<AccountDetail> getValidAccountList() {
        File folder = new File(AppUtils.appDataPath);
        File[] accounts = folder.listFiles();
        ArrayList<AccountDetail> res = new ArrayList<AccountDetail>();

        for (File item : accounts) {
            String accountPath = item.toString() + "/" + AppUtils.accountFileName,
                    jsonPath = item.toString() + "/account.json";
            File af = new File(accountPath), jf = new File(jsonPath);
            if (af.exists() && jf.exists()) {
                // 读取 JSON 文件
                Long jsonLength = jf.length();
                byte[] jsonContent = new byte[jsonLength.intValue()];
                try {
                    FileInputStream fin = new FileInputStream(jf);
                    fin.read(jsonContent);
                    fin.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 将 Byte 转换为 String
                String result = "";
                try {
                    result = new String(jsonContent, "UTF-8");

                    // 解析 JSON
                    try {
                        JSONObject obj = new JSONObject(result);
                        AccountDetail cur = new AccountDetail(obj.getString("name"),
                                obj.getString("md5"),
                                obj.getString("inheritID"),
                                obj.getString("inheritPassword"),
                                obj.getString("description"));
                        res.add(cur);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return res;
    }

    /**
     * 检查当前账号是否备份，若没有备份，则自动备份避免丢失
     * @return boolean
     */
    public static boolean checkCurrentAccountAdded(String specified) {
        File account = new File(AppUtils.garupaAccountPath);
        String md5 = specified.equals("") ? AppUtils.calcFileMd5(account) : specified;
        String accountPath = AppUtils.appDataPath + "/" + md5;
        File accountBackup = new File(accountPath);
        File accountBackupFile = new File(accountPath + "/" + accountFileName);

        if (!accountBackup.exists() || !accountBackup.isDirectory() || !md5.equals(calcFileMd5(accountBackupFile))) {
            return false;
        }
        return true;
    }

    /**
     * 在配置文件中更新当前账号信息
     * @param md5
     */
    public static void updateCurrentAccount(String md5) {
        File config = new File(AppUtils.appConfigPath);
        Long jsonLength = config.length();
        byte[] jsonConfig = new byte[jsonLength.intValue()];

        try {
            FileInputStream fin = new FileInputStream(config);
            fin.read(jsonConfig);
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String jsonContent = "";
        try {
            jsonContent = new String (jsonConfig, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // parse json
        try {
            JSONObject obj = new JSONObject(jsonContent);
            JSONStringer stringer = new JSONStringer();
            stringer.object()
                    .key("current")
                    .value(md5)
                    .endObject();
            String newConfig = stringer.toString();

            AppUtils.currentAccount = md5;

            // write
            FileOutputStream fout = new FileOutputStream(config);
            fout.write(newConfig.getBytes());
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        MainActivity.getInstance().renderAccountList();
    }

    /**
     * 在配置文件中获取当前账号信息
     * @param forceUpdate 是否强制更新账号信息
     */
    public static String getCurrentAccount(boolean forceUpdate) {
        if (!forceUpdate) {
            return AppUtils.currentAccount;
        }

        File config = new File(AppUtils.appConfigPath);
        Long jsonLength = config.length();
        byte[] jsonConfig = new byte[jsonLength.intValue()];

        try {
            FileInputStream fin = new FileInputStream(config);
            fin.read(jsonConfig);
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String jsonContent = "";
        try {
            jsonContent = new String (jsonConfig, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // parse json
        try {
            JSONObject obj = new JSONObject(jsonContent);
            String current = obj.getString("current");
            AppUtils.currentAccount = current;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return AppUtils.currentAccount;
    }

    public static void runGarupa() {
        PackageManager pm = MainActivity.getInstance().getPackageManager();
        if (AppUtils.checkPackageInstalled("jp.co.craftegg.band")) {
            Intent intent = pm.getLaunchIntentForPackage("jp.co.craftegg.band");
            MainActivity.getInstance().startActivity(intent);
        }
    }

    public static boolean deleteFile(File dirFile) {
        if (!dirFile.exists()) {
            return false;
        }

        if (dirFile.isFile()) {
            return dirFile.delete();
        } else {
            for (File file : dirFile.listFiles()) {
                deleteFile(file);
            }
        }
        return dirFile.delete();
    }

    public static boolean checkPackageInstalled (String packageName) {
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = MainActivity.getInstance().getPackageManager().getPackageInfo(packageName, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pkgInfo != null;
    }

}
