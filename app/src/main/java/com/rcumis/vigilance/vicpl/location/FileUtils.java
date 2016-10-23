package com.rcumis.vigilance.vicpl.location;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Nagarjuna on 23/10/16.
 */

public class FileUtils {

    private static  final String TAG = FileUtils.class.getSimpleName();

    public static void deleteFile(String absolutePath) {
        try {
            File file = new File(absolutePath);
            if(file != null && file.exists()) {
                boolean result = file.delete();
                if(result) Log.d(TAG, "File [" + absolutePath + "] Deleted");
            }
        } catch (Exception ex){ Log.e(TAG, ex.getMessage());}
    }

    private static File getAppMediaDirectory() {
        File externalStorageDirectory = Environment.getExternalStorageDirectory();

        File appMediaDir = new File(externalStorageDirectory, "VICPL");

        if(!appMediaDir.exists()) {
            appMediaDir.mkdir();
        }

        return appMediaDir;
    }

    public static File geLogsDirectory() {

        File appMediaDir = getAppMediaDirectory();

        File photosDir = new File(appMediaDir, "Logs");
        if(!photosDir.exists()) {
            photosDir.mkdir();
        }

        return photosDir;
    }



}
