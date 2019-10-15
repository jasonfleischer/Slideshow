package com.jfleischer.slideshow.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class GifView extends SurfaceView {

    private static final int BUFFER = 16 * 1024;
    private Movie movie;
    private long movieStart = 0;
    private boolean isGif;

    public GifView(Context context, AttributeSet attrs) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void setGifImage(String path) {
        try {
            File file = new File(path);
            adjustScreenSize(file);
            invalidate();

            InputStream is = null;
            is = new BufferedInputStream(new FileInputStream(file), BUFFER);
            is.mark(BUFFER);
            movie = null;
            movie = Movie.decodeStream(is);
            is.close();
            setBackgroundColor(Color.BLACK); //NEED THIS LINE 

            isGif = true;
        } catch (Exception e) {
            isGif = false;
            Log.e(getClass().getName(), "gif error");
            e.printStackTrace();
        }
    }

    public void reset() {
        isGif = false;
        setOnTouchListener(null);
        setAlpha(1.0f);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isGif) {
            canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR); // clear display
        } else {
            long now = android.os.SystemClock.uptimeMillis();
            if (movieStart == 0)
                movieStart = now;
            int relTime = (int) ((now - movieStart) % movie.duration());
            movie.setTime(relTime);
            movie.draw(canvas, 0, 0);
            invalidate();
        }
    }

    private void adjustScreenSize(File file) { // gets width and height from bytes then adjusts layout
        try {
            int[] width = new int[2];
            int[] height = new int[2];

            InputStream is = new FileInputStream(file);
            int counter = 0;
            int value = 0;
            while ((value = is.read()) != -1) {
                if (counter < 6) {
                    counter++;
                    continue;
                }
                if (counter == 6) width[0] = value;
                if (counter == 7) width[1] = value;
                if (counter == 8) height[0] = value;
                if (counter == 9) height[1] = value;
                counter++;
                if (counter > 10) break;
            }
            is.close();
            int w = width[0] + width[1] * 256;
            int h = height[0] + height[1] * 256;
            setLayoutParams(new RelativeLayout.LayoutParams(w, h));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}