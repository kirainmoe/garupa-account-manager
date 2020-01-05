package com.kirainmoe.garupaaccountmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;

import org.json.JSONStringer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener  {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    public String garupaAccountPath = Environment.getExternalStorageDirectory().getPath()
            +  "/Android/data/jp.co.craftegg.band/files/EhNfQ7brV3f3cCIcW9O4OaZxwC0V6UH1";
    public String appDataPath = Environment.getExternalStorageDirectory().getPath()
            + "/Android/data/com.kirainmoe.garupaaccountmanager";
    public String accountFileName = "EhNfQ7brV3f3cCIcW9O4OaZxwC0V6UH1";

    public static MainActivity instance;
    public View view;

    public MainActivity() {
        MainActivity.instance = this;
    }

    public static MainActivity getInstance() {
        return MainActivity.instance;
    }

    /**
     * Android 6.0+ 动态申请存储权限
     * @param activity Activity
     */
    public boolean verifyStoragePermissions(Activity activity) {
        try {
            // 检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 找不到账号文件时的提示信息
     */
    public void alertFileNotFound() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_error_file_not_found)
                .setMessage(R.string.app_error_file_not_found_description)
                .setPositiveButton(R.string.fine, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * 检查 garupa 是否已安装，并查找账号文件
     * @return boolean 如果因为各种原因无法读取账号文件，返回 false
     */
    public boolean checkGarupaInstalled() {
        try {
            FileInputStream fin = new FileInputStream(this.garupaAccountPath);
            int length = fin.available();
            fin.close();

            if (length > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean initAppDataPath() {
        // 检查账号文件是否存在
        boolean hasAccountFile = this.checkGarupaInstalled();

        if (!hasAccountFile) {
            this.alertFileNotFound();
        }

        File dataDir = new File(this.appDataPath);
        // 存在数据文件夹即视为已初始化 App
        if (dataDir.exists()) {
            return true;
        } else {
            try {
                /* 创建文件夹 */
                boolean res;
                res = dataDir.mkdir();
                if (!res) {
                    return false;
                }

                /* 自动备份当前账号，并生成信息 */
                AppUtils.createFromCurrentAccountFile("默认账号", "首次运行时自动备份的账号");

                /* 创建 App config.json 文件 */
                String currentAccountMd5 = AppUtils.calcFileMd5(new File(AppUtils.garupaAccountPath));
                JSONStringer stringer = new JSONStringer();
                try {
                    stringer.object()
                            .key("currentAccount")
                            .value(currentAccountMd5)
                            .endObject();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String configJson = stringer.toString();
                File configFile = new File(AppUtils.appConfigPath);
                FileOutputStream fout = new FileOutputStream(configFile);
                fout.write(configJson.getBytes());
                fout.close();

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.app_init_success)
                        .setMessage(R.string.app_init_description)
                        .setPositiveButton(R.string.fine, null);
                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public void renderAccountList() {
        AppUtils.accounts = AppUtils.getValidAccountList();
        AppUtils.getCurrentAccount(true);
        final ArrayList<AccountDetail> accounts = AppUtils.accounts;

        AccountDetailAdapter adapter = new AccountDetailAdapter(
                this,  R.layout.account_list_item, accounts
        );

        ListView listView = (ListView) findViewById(R.id.accounts_list_view);
        listView.setAdapter(adapter);

        // 点击 ListView 的 Item，应用该文件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AccountDetail targetAccount = accounts.get(position);
                targetAccount.applyCurrentFile();
            }
        });

        // 长按 ListView 的 Item，修改账号文件信息
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.getInstance(), EditorActivity.class);
                intent.putExtra("com.kirainmoe.garupaaccountmanager.index", position);
                startActivityForResult(intent, 0);
                return true;
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 设置状态栏颜色
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorStatusbar));

        this.view = getWindow().getDecorView();

        boolean hasPermission = this.verifyStoragePermissions(this);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        } else {
            this.initAppDataPath();
            this.renderAccountList();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity ref = MainActivity.getInstance();
                ref.onClickCreatePopUpMenu(view);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.initAppDataPath();
                    this.renderAccountList();
                } else {
                    final MainActivity ref = this;
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.app_request_permission)
                            .setMessage(R.string.app_request_permission_info)
                            .setPositiveButton(R.string.fine, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(ref, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                return;
            }
        }
    }

    public void onClickCreatePopUpMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v, Gravity.START);
        popup.inflate(R.menu.menu_add);
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.about)
                    .setMessage(R.string.about_info)
                    .setPositiveButton(R.string.fine, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        if (id == R.id.action_run_garupa) {
            AppUtils.runGarupa();
        }

        if (id == R.id.action_new_account) {
            boolean isBackedup = AppUtils.checkCurrentAccountAdded("");
            if (!isBackedup) {
                AppUtils.createFromCurrentAccountFile("Untitled", "创建新账号时，为防止原账号丢失自动创建的备份");
            }

            File accountFile = new File(AppUtils.garupaAccountPath);
            try {
                accountFile.delete();

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.success)
                        .setMessage(R.string.delete_success)
                        .setPositiveButton(R.string.fine, null);
                AlertDialog dialog = builder.create();
                dialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_current) {
            boolean isCurrentExist = AppUtils.checkCurrentAccountAdded("");
            if (isCurrentExist) {
                Snackbar.make(view, "当前账号已存在~", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return true;
            }
            String title = "Untitled", description = "于" + (new Date()).toString() + "导入的账号文件。";
            AppUtils.createFromCurrentAccountFile(title, description);
            MainActivity.getInstance().renderAccountList();
            Snackbar.make(view, "导入成功~", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        if (id == R.id.action_add_custom) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType(AppUtils.accountFileName + "/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, 1);
            return true;
        }

        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                Uri uri = data.getData();
                String filePath = uri.getPath().replace("/external_files", Environment.getExternalStorageDirectory().getPath());
                Log.v("path", filePath);
                File target = new File(filePath);

                if (!target.exists()) {
                    Snackbar.make(view, "导入时发生了未知错误：文件意外丢失 :(", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }

                String md5 = AppUtils.calcFileMd5(target);

                boolean isCurrentExist = AppUtils.checkCurrentAccountAdded(md5);
                if (isCurrentExist) {
                    Snackbar.make(view, "当前账号已存在~", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

                String time = (new Date()).toString();
                AppUtils.createFromSomewhere(filePath, "Untitled", "于 " + time + "从" + filePath + "导入的账号文件。");
                this.renderAccountList();
                Snackbar.make(view, "导入成功~", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

    public View getView() {
        return this.view;
    }
}
