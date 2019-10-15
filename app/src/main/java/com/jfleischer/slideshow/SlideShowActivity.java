package com.jfleischer.slideshow;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.jfleischer.slideshow.models.FileActivityMode;
import com.jfleischer.slideshow.models.Slide;
import com.jfleischer.slideshow.models.SlideShow;
import com.jfleischer.slideshow.utils.FileManager;
import com.jfleischer.slideshow.views.SlideShowView;

import java.io.File;
import java.util.ArrayList;

public class SlideShowActivity extends Activity {

    public static final String STORAGE_IMG_DIR = "slideShow";
    private File SLIDE_DIR;
    private SlideShowView slideShowView;
    private static final String TAG = SlideShowActivity.class.getSimpleName();
    private static SlideShowActivity instance;
    public static SlideShowActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        instance = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hideNavBar();
        SLIDE_DIR = new File(getFilesDir(), STORAGE_IMG_DIR);
        slideShowView = findViewById(R.id.slide_show_view);
        if (!SLIDE_DIR.exists() || SLIDE_DIR.list().length == 0) {
            if (SLIDE_DIR.mkdir())
                FileManager.copyAssetsTo(SLIDE_DIR);
            else
                Log.e(TAG, "Unable to make slide Directory");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final String[] fileList = SLIDE_DIR.list();
        if (fileList.length == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.prompt_no_slides));
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final Intent intent = new Intent(SlideShowActivity.this, FileActivity.class);
                    intent.putExtra("mode", FileActivityMode.Add.ordinal());
                    startActivity(intent);
                }
            });
            builder.setNegativeButton(R.string.add_defaults, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                    File destinationDirectory = new File(getFilesDir(), SlideShowActivity.STORAGE_IMG_DIR);
                    FileManager.copyAssetsTo(destinationDirectory);
                    init_slideshow(SLIDE_DIR.list());
                }
            });
            builder.show();
        } else {
            init_slideshow(fileList);
        }
    }

    private void init_slideshow(String[] fileList){
        SlideShow slideShow = new SlideShow();
        ArrayList<Slide> slides = new ArrayList<Slide>();
        for (int i = 0; i < fileList.length; i++) {
            String fileName = SLIDE_DIR.getAbsolutePath() + "/" + fileList[i];
            Slide slide = new Slide();
            slide.setImage(fileName);
            slide.setOrder(i);
            slides.add(slide);
        }
        slideShow.setSlides(slides);
        slideShow.sort();
        slideShowView.init(slideShow);
    }

    @Override
    protected void onPause() {
        super.onPause();
        slideShowView.onDestroy();
    }

    public void hideNavBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }
}
