package com.jfleischer.slideshow.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.jfleischer.slideshow.R;
import com.jfleischer.slideshow.models.Slide;
import com.jfleischer.slideshow.utils.BitmapAndImageHelper;

import java.io.File;

public class SlideImageView extends RelativeLayout implements SlideViewInterface{

    private ImageView imageView;

    public SlideImageView(Context context, AttributeSet attrs) {
        super(context);
    }

    @Override
    public void init() {
        imageView = findViewById(R.id.imageView);
    }

    @Override
    public void setup(Slide slide) {
        File imgFile = new File(slide.getPath());
        Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        int bitmap_height = bitmap.getHeight();
        int bitmap_width = bitmap.getWidth();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screen_height = displayMetrics.heightPixels;
        int screen_width = displayMetrics.widthPixels;

        float parentRatio = screen_width / screen_height;
        float imageRatio = bitmap_width / bitmap_height;
        if( parentRatio > imageRatio) {
            bitmap = BitmapAndImageHelper.getResizedBitmap(bitmap,  screen_height * bitmap_width / bitmap_height, screen_height );
        } else {
            bitmap = BitmapAndImageHelper.getResizedBitmap(bitmap, screen_width, screen_width *  bitmap_height/ bitmap_width );
        }

        imageView.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
    }

    @Override
    public void cleanup() {
        imageView.setImageDrawable(null);
    }
}
