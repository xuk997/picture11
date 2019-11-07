package com.example.picture;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import java.io.File;
import java.io.FileNotFoundException;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;



public class MainActivity extends AppCompatActivity {

    private Uri imgUri; //记录拍照后的照片文件的地址(临时文件)
    private ImageView imgView;//用于查看照片的view
    private String uploadFileName;
    private byte[] fileBuf;
    private String uploadUrl = "http://39.106.224.238:8000/upload";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo);
        imgView=findViewById(R.id.pupload);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getPrem(View view) {
        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    //得到了用户的允许
                }
                else{
                    //用户拒绝
                }break;
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    Toast.makeText(this, "读相册的操作被拒绝", Toast.LENGTH_LONG).show();
                }break;
        }
    }
    public void photo(View view) throws Exception{

        //删除并创建临时文件，用于保存拍照后的照片
        //android 6以后，写Sdcard是危险权限，需要运行时申请，但此处使用的是"关联目录"，无需！
        File outImg=new File(getExternalCacheDir(),"temp.jpg");
        if(outImg.exists()) outImg.delete();
        outImg.createNewFile();

        //复杂的Uri创建方式
        if(Build.VERSION.SDK_INT>=24)
            //这是Android 7后，更加安全的获取文件uri的方式（需要配合Provider,在Manifest.xml中加以配置）
            imgUri= FileProvider.getUriForFile(this,"com.example.app1.fileprovider",outImg);
        else
            imgUri=Uri.fromFile(outImg);

        //利用actionName和Extra,启动《相机Activity》
        Intent intent=new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imgUri);
        startActivityForResult(intent,1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        switch (requestCode) {
            case 1:
                //此时，相机拍照完毕
                if (resultCode == RESULT_OK) {
                    try {
                        //利用ContentResolver,查询临时文件，并使用BitMapFactory,从输入流中创建BitMap
                        //同样需要配合Provider,在Manifest.xml中加以配置
                        Bitmap map = BitmapFactory.decodeStream(getContentResolver().openInputStream(imgUri));
                        imgView.setImageBitmap(map);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }break;
            case 2:
                handleSelect(data);
                break;
        }
    }




    //按钮点击事件
    public void select(View view) {
        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        //进行sdcard的读写请求
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, 2);
        } else {
            openGallery(); //打开相册，进行选择
        }
    }

    //打开相册,进行照片的选择
    private void openGallery() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, 2);
    }


    //选择后照片的读取工作
    private void handleSelect(Intent intent) {
        Cursor cursor = null;
        Uri uri = intent.getData();
        cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            uploadFileName = cursor.getString(columnIndex);
        }
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            fileBuf=convertToBytes(inputStream);
            Bitmap bitmap = BitmapFactory.decodeByteArray(fileBuf, 0, fileBuf.length);
            imgView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        cursor.close();
    }

    //文件上传的处理
    public void upload(View view) {
        new Thread() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                //上传文件域的请求体部分
                RequestBody formBody = RequestBody
                        .create(fileBuf, MediaType.parse("image/jpeg"));
                //整个上传的请求体部分（普通表单+文件上传域）
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("title", "Square Logo")
                        //filename:avatar,originname:abc.jpg
                        .addFormDataPart("avatar", uploadFileName, formBody)
                        .build();
                Request request = new Request.Builder()
                        .url(uploadUrl)
                        .post(requestBody)
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    Log.i("数据", response.body().string() + "....");
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }.start();
    }

    private byte[] convertToBytes(InputStream inputStream) throws Exception{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        inputStream.close();
        return  out.toByteArray();
    }

}
