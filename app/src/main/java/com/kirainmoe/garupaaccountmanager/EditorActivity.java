package com.kirainmoe.garupaaccountmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.File;
import java.io.FileOutputStream;


public class EditorActivity extends AppCompatActivity {

    private AccountDetail target = null;
    public static EditorActivity instance;

    public EditorActivity() {
        EditorActivity.instance = this;
    }

    public static EditorActivity getInstance() {
        return EditorActivity.instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        int accountIndex = intent.getIntExtra("com.kirainmoe.garupaaccountmanager.index", -1);
        if (accountIndex < 0) {
            return;
        }

        final AccountDetail target = AppUtils.accounts.get(accountIndex);

        this.target = target;

        final EditText name = (EditText) findViewById(R.id.name_edittext),
                description = (EditText) findViewById(R.id.description_edittext),
                inheritID = (EditText) findViewById(R.id.inheritID_edittext),
                inheritPassword = (EditText) findViewById(R.id.inheritPassword_edittext);

        name.setText(target.name);
        description.setText(target.description);
        inheritID.setText(target.inheritID);
        inheritPassword.setText(target.inheritPassword);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sName = name.getText().toString(),
                        sDesc = description.getText().toString(),
                        sInheritId = inheritID.getText().toString(),
                        sInheritPassword = inheritPassword.getText().toString();
                target.name = sName;
                target.description = sDesc;
                target.inheritID = sInheritId;
                target.inheritPassword = sInheritPassword;

                String json = target.toJSON();
                File targetFile = new File(AppUtils.appDataPath + "/" + target.md5 + "/account.json");

                try {
                    FileOutputStream fout = new FileOutputStream(targetFile);
                    fout.write(json.getBytes());
                    fout.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                MainActivity.getInstance().renderAccountList();
                Snackbar.make(MainActivity.getInstance().getView(), "账号信息已更新~", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                setResult(0);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                EditText nameEdit = (EditText) findViewById(R.id.name_edittext);
                String name = nameEdit.getText().toString();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.sure_to_delete)
                        .setMessage("此操作不可逆，删除后账号数据将无法找回，请谨慎操作。确定要删除 " + name + "吗？")
                        .setPositiveButton("取消", null)
                        .setNegativeButton("确认删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditorActivity editor = EditorActivity.getInstance();
                                String targetPath = AppUtils.appDataPath + "/" + editor.target.md5;
                                File t = new File(targetPath);
                                boolean res = AppUtils.deleteFile(t);
                                if (res) {
                                    MainActivity.getInstance().renderAccountList();
                                    Snackbar.make(MainActivity.getInstance().getView(), "已删除", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                    setResult(0);
                                    finish();
                                }
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
        }
        return false;
    }

}
