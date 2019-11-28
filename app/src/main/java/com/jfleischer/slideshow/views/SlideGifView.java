package com.jfleischer.slideshow.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

import com.jfleischer.slideshow.R;
import com.jfleischer.slideshow.models.Slide;

public class SlideGifView extends RelativeLayout implements SlideViewInterface {

    private GifView gifView;

    public SlideGifView(Context context, AttributeSet attrs) {
        super(context);
    }

    @Override
    public void init() {
        gifView = findViewById(R.id.gif);
    }

    @Override
    public void setup(Slide slide) {
        gifView.setGifImage(slide.getPath());
    }

    @Override
    public void cleanup() {

        gifView.reset();
    }
}
