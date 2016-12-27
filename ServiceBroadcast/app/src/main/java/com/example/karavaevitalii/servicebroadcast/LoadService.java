package com.example.karavaevitalii.servicebroadcast;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public final class LoadService extends Service implements Runnable {

    private static final String URL =
            "https://img3.goodfon.ru/original/1024x1024/b/10/siyanie-dzhek-nikolson-dver.jpg";
    private File f;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        f = new File(getFilesDir(), MainActivity.FILE_NAME);
        new Thread(this).start();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void run() {
        InputStream is = null;
        FileOutputStream os = null;

        try {
            is = new BufferedInputStream(new URL(URL).openStream());
            os = new FileOutputStream(f);

            int read;
            byte[] buffer = new byte[1024];
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }

            sendBroadcast(new Intent(MainActivity.BROADCAST));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
