package com.jfleischer.slideshow.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import com.jfleischer.slideshow.SlideShowActivity;
import com.jfleischer.slideshow.models.Slide;

import android.content.res.AssetManager;
import android.util.Log;


public class FileManager {

    private static String TAG = FileManager.class.getSimpleName();

    public static void copyFile(File src, File dst){
        try{
            final FileInputStream fis = new FileInputStream(src);
            final FileOutputStream fos = new FileOutputStream(dst);
            try {
                final FileChannel inChannel = fis.getChannel();
                final FileChannel outChannel = fos.getChannel();
                inChannel.transferTo(0, inChannel.size(), outChannel);
            } finally {
                fis.close();
                fos.close();
            }
        }catch(final IOException ex){
            ex.printStackTrace();
            Log.e(TAG, "Unable to copy file " + src.getPath() + " to " + dst.getPath());
        }
    }


    public static void copyAssetsTo(File destinationDir){
        //File destinationDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), STORAGE_IMG_DIR);
        AssetManager mgr = SlideShowActivity.getInstance().getResources().getAssets();
        try {
            String[] list = mgr.list("");
            if (list != null)
                for (String fileName: list){
                    File file = new File(fileName);
                    if (!file.isDirectory()) {
                        InputStream in = null;
                        OutputStream out = null;
                        try {
                            in = mgr.open(fileName);
                            File outFile = new File(destinationDir, fileName);
                            out = new FileOutputStream(outFile);
                            byte[] buffer = new byte[1024];
                            int read;
                            while ((read = in.read(buffer)) != -1) {
                                out.write(buffer, 0, read);
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Failed to copy asset file: " + fileName, e);
                        } finally {
                            if (in != null) {
                                try { in.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (out != null) {
                                try { out.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
        } catch (IOException e) {
            Log.e(TAG, "Assets error");
        }
    }
}
