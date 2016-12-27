package com.example.karavaevitalii.servicebroadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    public static final String FILE_NAME = "image.jpg"
            , BROADCAST = "broadcast";

    private File f;
    private Bitmap bitmap;

    private ImageView iv;
    private TextView tv;

    private BroadcastReceiver actionReceiver;
    private BroadcastReceiver imageReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFields();
        display();
        initReceivers();
        registerReceivers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
    }

    private void initFields() {
        iv = (ImageView) findViewById(R.id.image);
        tv = (TextView) findViewById(R.id.error);
    }

    private void getImage() {
        f = new File(getFilesDir(), FILE_NAME);
        bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
    }

    private void display() {
        getImage();
        if (f.exists()) {
            showImage();
        } else {
            showError();
        }
    }

    private void initReceivers() {
        actionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Intent i = new Intent(context, LoadService.class);
                context.startService(i);
            }
        };

        imageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                showImage();
            }
        };
    }

    private void showImage() {
        getImage();
        if (f.exists()) {
            iv.setImageBitmap(bitmap);
            iv.setVisibility(View.VISIBLE);
            tv.setVisibility(View.GONE);
        }
    }

    private void showError() {
        iv.setVisibility(View.GONE);
        tv.setVisibility(View.VISIBLE);
    }

    private void registerReceivers() {
        registerReceiver(actionReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(imageReceiver, new IntentFilter(BROADCAST));
    }

    private void unregisterReceivers() {
        unregisterReceiver(actionReceiver);
        unregisterReceiver(imageReceiver);
    }
}
