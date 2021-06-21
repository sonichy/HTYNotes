package com.hty.notes;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.Date;

public class NoteActivity extends Activity {

    EditText editText;
    InputMethodManager IMM;
    String id, s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        Log.e(Thread.currentThread().getStackTrace()[2] + "", "id = " + id);
        editText = (EditText) findViewById(R.id.editText);

        DBHelper helper = new DBHelper(this);
        Cursor cursor = helper.queryId(id);
        if (cursor.moveToFirst()) {
            s = cursor.getString(cursor.getColumnIndex("note"));
            editText.setText(s);
        }

        IMM = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        //IMM.hideSoftInputFromWindow(editText.getWindowToken(), 0);//无效
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "保存");
        menu.add(0, 1, 1, "退出");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int item_id = item.getItemId();
        switch (item_id) {
            case 0:
                save();
                break;
            case 1:
                IMM.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                finish();
                break;
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            save();
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    void save() {
        DBHelper DBHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase DB = DBHelper.getWritableDatabase();
        Date date = new Date();
        String s1 = editText.getText().toString();
        ContentValues values = new ContentValues();
        values.put("time", date.getTime());
        values.put("note", s1);
        if (id == null) {
            if (!s1.equals(""))
                DB.insert("notes", null, values);
        } else {
            if (!s.equals(editText.getText().toString()))
                DB.update("notes", values, "_id = " + id, null);
        }
        DB.close();
    }

}