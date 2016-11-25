package com.rcumis.vigilance.vicpl.network;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.webkit.URLUtil;

import com.rcumis.vigilance.vicpl.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by 'Nagarjuna' on 25/11/16.
 */


public class DownloadService extends IntentService {
    private int lastUpdate =0;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;

    private File storageDirectory;
    String downloadUrl = "";
    private File outputFile;
    private ResultReceiver receiver;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public DownloadService() {
        super("RCUMIS_DOWNLOAD_SERVICE");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags,startId);
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        downloadUrl = intent.getStringExtra("download_url");
        receiver = intent.getParcelableExtra("receiver");
        Log.i("url", downloadUrl);
        receiver.send(0, null);
        mNotificationManager = (NotificationManager)  getSystemService(NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(
                "RCUMIS")
                .setContentText("Download in progress")
                .setSmallIcon(R.mipmap.ic_launcher).setContentInfo("0%");

        mBuilder.setOngoing(true);
        // ResultReceiver receiver = (ResultReceiver) intent.getParcelableExtra("receiver");
        try {
            URL url = new URL(downloadUrl);
            URLConnection connection = url.openConnection();
            connection.connect();
            // this will be useful so that you can show a typical 0-100% progress bar
            int fileLength = connection.getContentLength();
            storageDirectory = new File(
                    Environment.getExternalStorageDirectory(), "RCUMIS");
            // download the file
            InputStream input = new BufferedInputStream(url.openStream());
            outputFile = new File(storageDirectory.getPath(),
                    URLUtil.guessFileName(downloadUrl,null,null));
            OutputStream output = new FileOutputStream(outputFile);
            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;

                progressChange((int)(total * 100) / fileLength);
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
            receiver.send(2, null);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    void progressChange(int progress){


        if (lastUpdate != progress) {
            lastUpdate = progress;
            // not.contentView.setProgressBar(R.id.status_progress,
            // 100,Integer.valueOf(progress[0]), false);
            // inform the progress bar of updates in progress
            // mNotificationManager.notify(42, not);
            if (progress < 100) {
                mBuilder.setProgress(100, Integer.valueOf(progress),
                        false).setContentInfo(progress+"%");
                mNotificationManager.notify(12, mBuilder.build());

            } else {
                mBuilder.setContentText("Download complete")
                        // Removes the progress bar
                        .setProgress(0, 0, false).setOngoing(false).setContentInfo("");
                Bundle bundle = new Bundle();
                bundle.putString("path", outputFile.getAbsolutePath());
                receiver.send(1, bundle);
                mNotificationManager.notify(12, mBuilder.build());

            }

        }

    }
}
