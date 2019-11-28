package com.jfleischer.slideshow.views;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.jfleischer.slideshow.R;
import com.jfleischer.slideshow.models.Slide;

import java.net.URI;

public class SlideVideoView extends RelativeLayout implements SlideViewInterface{

    final private String TAG = SlideShowView.class.getSimpleName();
    private VideoView videoView;
    //private ImageView image;
    private boolean loop = false;
    Uri uri;



    public SlideVideoView(Context context, AttributeSet attrs) {
        super(context);
        //setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }


    @Override
    public void init() {
        videoView = findViewById(R.id.video_view);
        //image = findViewById(R.id.film_image);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.e(TAG, "video end");
                //if (!controlsShown) {
                if (loop) { // restart video
                    mp.seekTo(0);
                    mp.start();
                    return;
                }
                stopVideoView();
                //setupTimer(0);
                //}

                stopVideoView();
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() { // gets rid of pop-up
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e(TAG, "video error");
                stopVideoView();
                //setupTimer(0);
                return true;
            }
        });
    }

    @Override
    public void setup(Slide slide) {
        //image.setVisibility(View.VISIBLE);

        //videoView.setVideoPath(slide.getPath());

         uri = Uri.parse(slide.getPath());


    }


    public void startVideo(boolean repeat) {
        Log.i(TAG, "start video: " + videoView.getDuration() );
        loop = repeat;
        //image.setVisibility(View.INVISIBLE);
        videoView.setVisibility(View.VISIBLE);
        videoView.setVideoURI(uri);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.e(TAG, "video end");
                //if (!controlsShown) {
                if (loop) { // restart video
                    mp.seekTo(0);
                    mp.start();
                    return;
                }
                stopVideoView();
                //setupTimer(0);
                //}

                stopVideoView();
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() { // gets rid of pop-up
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e(TAG, "video error");
                stopVideoView();
                //setupTimer(0);
                return true;
            }
        });
        videoView.start();
    }

    @Override
    public void cleanup() {
        stopVideoView();
    }

    private void stopVideoView() {
        //image.setVisibility(View.INVISIBLE);
        //videoView.setVisibility(View.INVISIBLE);
        //videoView.setBackgroundColor(Color.TRANSPARENT);
        videoView.stopPlayback();
    }
}
