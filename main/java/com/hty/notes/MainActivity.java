package com.hty.notes;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    static File dir;
    EditText editText_search;
    ImageButton imageButton_clear;
    GridView gridView;
    SimpleCursorAdapter adapter;
    InputMethodManager IMM;
    SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    SimpleDateFormat SDF_MMddHHmm = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
    SimpleDateFormat SDF_HHmm = new SimpleDateFormat("HH:mm", Locale.getDefault());
    int position1 = 0;
    private static String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };
    private static int REQUEST_PERMISSION_CODE = 1;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "HTYNotes";
        dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.e(Thread.currentThread().getStackTrace()[2] + "", "checkSelfPermission: " + checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE));
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMISSIONS, REQUEST_PERMISSION_CODE);
            }
        }

        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        IMM = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        editText_search = (EditText) findViewById(R.id.editText_search);
        editText_search.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("")) {
                    imageButton_clear.setVisibility(View.GONE);
                } else {
                    imageButton_clear.setVisibility(View.VISIBLE);
                }
                search(s.toString());
            }
        });
        imageButton_clear = (ImageButton) findViewById(R.id.imageButton_clear);
        imageButton_clear.setVisibility(View.GONE);
        gridView = (GridView) findViewById(R.id.gridView);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int columns = 1;
        Configuration configuration = getResources().getConfiguration();
        int orientation = configuration.orientation;
        if (orientation == configuration.ORIENTATION_LANDSCAPE) {
            columns =  Integer.parseInt(sharedPreferences.getString("landscape_columns", "1"));
        } else if (orientation == configuration.ORIENTATION_PORTRAIT) {
            columns =  Integer.parseInt(sharedPreferences.getString("portrait_columns", "1"));
        }
        gridView.setNumColumns(columns);
        search("");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int columns = 1;
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            columns =  Integer.parseInt(sharedPreferences.getString("landscape_columns", "1"));
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            columns =  Integer.parseInt(sharedPreferences.getString("portrait_columns", "1"));
        }
        gridView.setNumColumns(columns);
    }

    @Override
    protected void onResume() {
        super.onResume();
        search(editText_search.getText().toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "添加");
        menu.add(0, 1, 1, "设置");
        menu.add(0, 3, 3, "关于");
        menu.add(0, 4, 4, "更新日志");
        menu.add(0, 5, 5, "退出");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int item_id = item.getItemId();
        switch (item_id) {
            case 0:
                startActivity(new Intent(MainActivity.this, NoteActivity.class));
                break;
            case 1:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case 3:
                new AlertDialog.Builder(this).setIcon(R.mipmap.ic_launcher).setTitle("便签 V1.1")
                        .setMessage("一个简单的便签。\n作者：海天鹰\nQQ：84429027")
                        .setPositiveButton("确定", null).show();
                break;
            case 4:
                new AlertDialog.Builder(this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("更新日志")
                        .setMessage("V1.1 (2021-06)\nGridView代替ListView，可设置横屏列数和竖屏列数。\n日期格式增加今天、昨天。\n\nV1.0 (2021-05)\n便签列表，编辑便签。")
                        .setPositiveButton("确定", null).show();
                break;
            case 5:
                finish();
                break;
        }
        return true;
    }

    void search(String s) {
        DBHelper helper = new DBHelper(this);
        Cursor cursor1 = helper.query(s);
        int count = cursor1.getCount();
        setTitle("便签" + count);
        String[] from = { "_id", "time", "note" };
        int[] to = { R.id.textView_id, R.id.textView_time, R.id.textView_note };
        adapter = new SimpleCursorAdapter(this, R.layout.item_note, cursor1, from, to, 0);
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder(){
            public boolean setViewValue(View view, Cursor cursor, int columnIndex){
                //Log.e(Thread.currentThread().getStackTrace()[2] + "", view.toString() + columnIndex);
                if (view.getId() == R.id.textView_time) {
                    long t = cursor.getLong(columnIndex);
                    Date date_now = new Date();
                    Date date_h0m0s0 = new Date(date_now.getYear(), date_now.getMonth(), date_now.getDate(), 0, 0, 0);
                    Date date_yesterday = new Date(date_h0m0s0.getTime() - 24*60*60*1000);
                    Date date_m1d1 = new Date(date_now.getYear(), 1, 1);
                    Date date = new Date(t);
                    long t_now = date_now.getTime() - t;
                    long t_h0m0s0 = date_now.getTime() - date_h0m0s0.getTime();
                    long t_yesterday = date_now.getTime() - date_yesterday.getTime();
                    long t_m1d1 = date_now.getTime() - date_m1d1.getTime();
                    if (t_now <= t_h0m0s0)
                        ((TextView)view).setText("今天 " + SDF_HHmm.format(date));
                    else if (t_now <= t_yesterday)
                        ((TextView)view).setText("昨天 " + SDF_HHmm.format(date));
                    else if (t_now <= t_m1d1)
                        ((TextView)view).setText(SDF_MMddHHmm.format(date));
                    else
                        ((TextView)view).setText(SDF.format(date));
                    return true;
                }
                return false;
            }
        });
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                IMM.hideSoftInputFromWindow(editText_search.getWindowToken(), 0);
                String sid = ((TextView) view.findViewById(R.id.textView_id)).getText().toString();
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                intent.putExtra("id", sid);
                startActivity(intent);
            }
        });

        gridView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
                //AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                //String title = ((TextView) info.targetView.findViewById(R.id.title)).getText().toString();
                //menu.setHeaderTitle(title);
                menu.add(0, 0, 0, "复制");
                menu.add(0, 1, 1, "分享");
                menu.add(0, 2, 2, "删除");
            }
        });
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final String sid = ((TextView) menuInfo.targetView.findViewById(R.id.textView_id)).getText().toString();
        //String stime = ((TextView) menuInfo.targetView.findViewById(R.id.textView_time)).getText().toString();
        String note = ((TextView) menuInfo.targetView.findViewById(R.id.textView_note)).getText().toString();
        position1 = menuInfo.position;
        switch (item.getItemId()) {
            case 0:
                ClipboardManager CM = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                CM.setPrimaryClip(ClipData.newPlainText("text", note));
                Toast.makeText(getApplicationContext(), "内容已复制", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, note);
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent, "分享到"));
                break;
            case 2:
                new AlertDialog.Builder(MainActivity.this)
                        .setIcon(android.R.drawable.stat_sys_warning)
                        .setTitle("删除操作")
                        .setMessage("此步骤不可还原，确定删除" + sid + "？\n" + note)
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,	int which) {
                                DBHelper DBHelper = new DBHelper(getApplicationContext());
                                SQLiteDatabase DB = DBHelper.getWritableDatabase();
                                DB.delete("notes", "_id = ?", new String[] { sid });
                                search("");
                                gridView.setSelection(position1);
                            }
                        })
                        .setNegativeButton("否", null)
                        .show();
                break;
        }
        return true;
    }

    public void clear(View view){
        editText_search.setText("");
    }

}