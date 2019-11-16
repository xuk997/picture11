package com.example.register1_1;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class register extends Activity {
    String username = null, email = null,password=null,password1=null;
    Button register,cancel;
    private SQLiteDatabase sdb;
    protected void onCreate(Bundle savedInstanceState){
        DatabaseHelper databaseHelper = new DatabaseHelper(this,"test_db",null,1);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        EditText EditText5=findViewById(R.id.editText5);
        EditText EditText6=findViewById(R.id.editText6);
        EditText EditText7=findViewById(R.id.editText7);
        EditText EditText8=findViewById(R.id.editText8);
        register=findViewById(R.id.register);
        cancel=findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(register.this,MainActivity.class)); }
        });
        EditText5.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {username  = s.toString();}
        });
        EditText6.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {email  = s.toString();}
        });
        EditText7.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {password  = s.toString();}
        });
        EditText8.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {password1  = s.toString();}
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( password.equals(password1)&&password!=null&&password1!=null){
                Toast.makeText(getApplicationContext(),username+email+password+password1,Toast .LENGTH_SHORT).show();
                    ContentValues values = new ContentValues();
                    values.put("name",username);
                    values.put("email",email);
                    values.put("password",password);
                    db.insert("user",null,values);
                }
                else{
                    Toast.makeText(getApplicationContext(),"密码不同",Toast .LENGTH_SHORT).show();
                }
            }
        });
    }


}

