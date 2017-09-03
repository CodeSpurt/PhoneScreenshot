package com.codespurt.phonescreenshot;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button takeScreenshot;

    private final int PERMISSION_STORAGE = 1002;

    private boolean isExternalStorageAvailable = false;
    private String directoryPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        takeScreenshot = (Button) findViewById(R.id.btn_take_screenshot);
    }

    @Override
    protected void onResume() {
        super.onResume();
        takeScreenshot.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_take_screenshot:
                takeScreenshot();
                break;
        }
    }

    private void takeScreenshot() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // request permission
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
            return;
        }

        try {
            // for activity
            View v = getWindow().getDecorView().getRootView();

            // for fragment
            // View v = getActivity().getWindow().getDecorView().getRootView();

            Bitmap screenshot = null;
            v.setDrawingCacheEnabled(true);
            screenshot = Bitmap.createBitmap(v.getDrawingCache());
            v.setDrawingCacheEnabled(false);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            screenshot.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

            createDirectoryInStorage();

            // write file to directory
            File imageFile = null;
            if (isExternalStorageAvailable) {
                imageFile = new File(directoryPath, getFileName() + ".jpg");
                if (imageFile != null) {
                    FileOutputStream fo;
                    try {
                        imageFile.createNewFile();
                        fo = new FileOutputStream(imageFile);
                        fo.write(bytes.toByteArray());
                        fo.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            openScreenshot(imageFile);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takeScreenshot();
                } else {
                    Toast.makeText(this, "Permission required", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    private void createDirectoryInStorage() {
        boolean sdCardAvailability = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (sdCardAvailability) {
            String directoryName = getPackageName();
            directoryPath = Environment.getExternalStorageDirectory().getPath().toString() + "/Android/data/" + directoryName + "/Images";

            // storage
            File f = new File(directoryPath);
            if (!f.exists()) {
                if (!f.mkdirs()) {
                    isExternalStorageAvailable = false;
                }
            }
            isExternalStorageAvailable = true;
        } else {
            isExternalStorageAvailable = false;
        }
    }

    private String getFileName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
        String currentTimeStamp = dateFormat.format(new Date());
        return currentTimeStamp;
    }

    @Override
    protected void onPause() {
        super.onPause();
        takeScreenshot.setOnClickListener(null);
    }
}
