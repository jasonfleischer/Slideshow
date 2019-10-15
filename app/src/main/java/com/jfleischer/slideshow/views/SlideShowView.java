package com.jfleischer.slideshow.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.jfleischer.slideshow.FileActivity;
import com.jfleischer.slideshow.R;
import com.jfleischer.slideshow.SlideShowActivity;
import com.jfleischer.slideshow.SlideShowConfig;
import com.jfleischer.slideshow.models.FileActivityMode;
import com.jfleischer.slideshow.models.Slide;
import com.jfleischer.slideshow.models.SlideShow;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SlideShowView extends RelativeLayout {

    private final static int SLIDE_DURATION_MS = SlideShowConfig.SLIDE_DURATION_MS;
    private final static String TAG = SlideShowView.class.getSimpleName();
    private final OnErrorListener videoViewOnErrorListener = new OnErrorListener() { // gets rid of pop-up
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.e(TAG, "video error");
            stopVideoView();
            setupTimer(0);
            return true;
        }
    };
    //private final static String db = CouchController.CONTENT;
    private final OnCompletionListener videoViewCompleteListener = new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (!controlsShown) {
                if (slides.size() == 1) { // restart video
                    mp.seekTo(0);
                    mp.start();
                    return;
                }
                stopVideoView();
                setupTimer(0);
            }
        }
    };
    private int fadeTimeInMS = SlideShowConfig.FADE_IN_TIME_MS;
    private SlideShow slideShow;
    private List<Slide> slides;
    private RelativeLayout[] slideContAry;
    private FrameLayout[] imgContAry;
    private ImageView[] imgAry;
    private GifView[] gifAry;
    private VideoView[] videoAry;
    private RelativeLayout[] vidContAry;
    private ImageView leftArrow, rightArrow, settings;
    private Timer timer;
    private TimerTask rotateSlideTimerTask, resumeSlideShowTimerTask;
    private int currentSlideIndex;
    private int previousSlideIndex;
    private LinkedList<RelativeLayout> videoQueue;
    private boolean touched, controllable, transitioning, controlsShown = false;
    private String slideName, slideAction; // Used for metrics

    public SlideShowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(SlideShow slideS) {
        setKeepScreenOn(true);
        slideShow = slideS;
        if (slideShow == null) {
            Log.e(TAG, "slideShow null");
            return;
        }
        slides = slideShow.getSlides();
        if (slides.isEmpty()) {
            Log.e(TAG, "slides are of size zero");
            imgAry[0].setBackground(getResources().getDrawable(R.drawable.img_not_found));
            return;
        }
        videoQueue = new LinkedList<RelativeLayout>();
        touched = false;
        transitioning = false;
        controllable = slideShow.isControllable();
        currentSlideIndex = 1;


        final RelativeLayout frontSlideContainer = findViewById(R.id.front_slide_container);
        final RelativeLayout backSlideContainer = findViewById(R.id.back_slide_container);
        slideContAry = new RelativeLayout[]{backSlideContainer, frontSlideContainer};

        final FrameLayout frontImgCont = findViewById(R.id.front_imageViewCont);
        final FrameLayout backImgCont = findViewById(R.id.back_imageViewCont);
        imgContAry = new FrameLayout[]{backImgCont, frontImgCont};

        final ImageView frontImage = findViewById(R.id.front_imageView);
        final ImageView backImage = findViewById(R.id.back_imageView);
        imgAry = new ImageView[]{backImage, frontImage};

        final GifView frontGif = findViewById(R.id.front_gif);
        final GifView backGif = findViewById(R.id.back_gif);
        gifAry = new GifView[]{backGif, frontGif};

        final RelativeLayout frontVideoContainer = findViewById(R.id.front_video_container);
        final RelativeLayout backVideoContainer = findViewById(R.id.back_video_container);
        vidContAry = new RelativeLayout[]{backVideoContainer, frontVideoContainer};

        final VideoView frontVideo = findViewById(R.id.front_slide_video);
        final VideoView backVideo = findViewById(R.id.back_slide_video);
        videoAry = new VideoView[]{backVideo, frontVideo};

        leftArrow = findViewById(R.id.slide_show_left_arrow);
        rightArrow = findViewById(R.id.slide_show_right_arrow);
        settings = findViewById(R.id.slide_show_settings);
        settings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence[] colors = new CharSequence[]{"Add slides", "Remove slides"};
                final Context context = SlideShowActivity.getInstance();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Choose an action");
                builder.setItems(colors, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // the user clicked on colors[which]
                        if(which==0){
                            final Intent intent = new Intent(SlideShowActivity.getInstance(),FileActivity.class);
                            intent.putExtra("mode", FileActivityMode.Add.ordinal());
                            context.startActivity(intent);
                        }else if(which==1){
                            final Intent intent = new Intent(SlideShowActivity.getInstance(),FileActivity.class);
                            intent.putExtra("mode", FileActivityMode.Delete.ordinal());
                            context.startActivity(intent);
                        }
                        //context.finish();
                    }
                });
                builder.show();

            }
        });

        frontVideo.setOnCompletionListener(videoViewCompleteListener);
        backVideo.setOnCompletionListener(videoViewCompleteListener);
        frontVideo.setOnErrorListener(videoViewOnErrorListener);
        backVideo.setOnErrorListener(videoViewOnErrorListener);

        setSlideBackground(slides.get(0), 1);
        slideContAry[1].setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    onSlideTouch(slides.get(0));
                    return true;
                }
                return false;
            }
        });

        frontSlideContainer.setAlpha(1.0f);
        if (!slideS.isTransparent()) {
            setBackgroundColor(Color.BLACK);
        }
        /*if(!slideShow.isFullscreen()){
            LayoutParams lp = (LayoutParams)this.getLayoutParams();
            lp.setMargins(0, (int)getResources().getDimension(R.dimen.header_height), (int)getResources().getDimension(R.dimen.advertisement_panel_width), 0);
            setLayoutParams(lp);
        }*/

        if (slides.size() == 1) {
            Log.i(TAG, "Slides are of size one");
            if (isVideoFile(slides.get(0).getImage())) {
                startVideo();
            }
            return;
        }

        if (isVideoFile(slides.get(0).getImage())) {
            startVideo();
        } else {
            if (slides.get(0).getDurationInSeconds() > 0) {
                setupTimer(slides.get(0).getDurationInSeconds() * 1000);
            } else {
                setupTimer(SLIDE_DURATION_MS);
            }
        }
    }

    private void onSlideTouch(Slide slide) {
        //if(!touched){ // prevent duplicate touches
        if (controllable && !controlsShown) {
            controlsShown = true;
            touched = true;
            resumeSlideShowTimerTask = new ResumeSlideShowTimerTask();
            fadeTimeInMS = 800;
            if(slides.size()>1) {
                leftArrow.setVisibility(View.VISIBLE);
                rightArrow.setVisibility(View.VISIBLE);
            }
            settings.setVisibility(View.VISIBLE);
            rightArrow.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopVideoView();
                    nextScreen(true);
                    resetResumeSlideShowTimerTask();
                }
            });
            leftArrow.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopVideoView();
                    nextScreen(false);
                    resetResumeSlideShowTimerTask();
                }
            });
            if (isVideoFile(slides.get(0).getImage())) startVideo();
            resetResumeSlideShowTimerTask();
        }
            /*}else{
                touched = true;
                //slideAction = slide.getAction();
                slideName = slide.getName();
            }*/
        //}
    }

    // TIMERS
    private void setupTimer(int delay) {
        if (controlsShown)
            return;
        if (timer != null) {
            timer.cancel();
            timer.purge();
            rotateSlideTimerTask.cancel();
        }
        timer = new Timer();
        rotateSlideTimerTask = new RotateSlideTimerTask();
        timer.schedule(rotateSlideTimerTask, delay);
    }

    private void resetResumeSlideShowTimerTask() {
        if(rotateSlideTimerTask!=null)rotateSlideTimerTask.cancel();
        if(resumeSlideShowTimerTask!=null)resumeSlideShowTimerTask.cancel();
        resumeSlideShowTimerTask = new ResumeSlideShowTimerTask();
        new Timer().schedule(resumeSlideShowTimerTask, slideShow.getControllableTimeoutInSeconds() * 1000);
    }

    private void destroyTimers() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            rotateSlideTimerTask.cancel();
            rotateSlideTimerTask = null;
        }
        if (resumeSlideShowTimerTask != null) {
            resumeSlideShowTimerTask.cancel();
            resumeSlideShowTimerTask = null;
        }
        timer = null;
    }

    private int getNextSlideIndex() {
        int result = currentSlideIndex + 1;
        if (result >= slides.size()) {
            result = 0;
        }
        return result;
    }

    private int getPreviousSlideIndex() {
        int result = currentSlideIndex - 1;
        if (result == -1) result = slides.size() - 1;
        return result;
    }

    private void nextScreen(boolean forward) {

        if (transitioning || slides.size() < 2)
            return;
        transitioning = true;

        if (forward) {
            previousSlideIndex = currentSlideIndex;
            currentSlideIndex = getNextSlideIndex();
        } else {
            currentSlideIndex = getPreviousSlideIndex();
            previousSlideIndex = getPreviousSlideIndex();
        }

        // dissolveToNext
        final int isFront = (slideContAry[1].getAlpha() == 0 ? 0 : 1);
        final int notIsFront = (isFront == 1 ? 0 : 1);

        final Animation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(fadeTimeInMS);
        animation.setInterpolator(new DecelerateInterpolator());
        slideContAry[isFront].startAnimation(animation);

        final Animation animationFadeIn = new AlphaAnimation(0.0f, 1.0f);
        animationFadeIn.setDuration(fadeTimeInMS);
        animationFadeIn.setInterpolator(new DecelerateInterpolator());
        slideContAry[notIsFront].startAnimation(animationFadeIn);
        slideContAry[notIsFront].setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    onSlideTouch(slides.get(previousSlideIndex));
                    return true;
                }
                return false;
            }
        });
        slideContAry[isFront].setOnTouchListener(null);

        animationFadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                slideContAry[isFront].setAlpha(0.0f);
                // clearViews(isFront);
                setSlideBackground(slides.get(currentSlideIndex), isFront);
                if (!isVideoFile(slides.get(previousSlideIndex).getImage())) {
                    timer = null;
                    if (slides.get(previousSlideIndex).getDurationInSeconds() > 0) {
                        setupTimer(slides.get(previousSlideIndex).getDurationInSeconds() * 1000);
                    } else {
                        setupTimer(SLIDE_DURATION_MS);
                    }
                } else {
                    startVideo();
                }
                transitioning = false;
            }

            @Override
            public void onAnimationStart(Animation animation) {
                setSlideBackground(slides.get(previousSlideIndex), notIsFront);
                slideContAry[notIsFront].setAlpha(1.0f);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private void setSlideBackground(Slide slide, int isFront) {

        final String path = slide.getImage();
        if(new File(path).exists()){
            if (slide.getImage().endsWith(".gif")) { // gif
                hideImg(isFront);
                gifAry[isFront].setGifImage(path);
            } else if (isVideoFile(slide.getImage())) { // video
                hideImg(isFront);
                gifAry[isFront].reset();
                setupVideoView(path, isFront);

            } else { // bitmap
                gifAry[isFront].reset();
                File imgFile = new File(path);
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imgAry[isFront].setBackground(new BitmapDrawable(getResources(), bitmap));
            }
        }else{
            gifAry[isFront].reset();
            File imgFile = new File(path);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_not_found);

            imgAry[isFront].setBackground(new BitmapDrawable(getResources(), bitmap));
        }
    }

    private void hideImg(int isFront) {
        imgContAry[isFront].setBackgroundColor(Color.TRANSPARENT);
        imgAry[isFront].setBackground(null);
    }

    //VIDEO
    private boolean isVideoFile(String fileName) {
        return fileName.endsWith(".mp4") || fileName.endsWith(".3gp") || fileName.endsWith(".avi");
    }

    private void setupVideoView(String path, int isFront) {
        destroyTimers();
        final RelativeLayout videoContainer = vidContAry[isFront];
        final VideoView videoView = videoAry[isFront];
        videoQueue.add(videoContainer);
        videoView.setVideoPath(path);
    }

    private void startVideo() {
        final VideoView vv = (VideoView) videoQueue.getFirst().getChildAt(0);
        //videoQueue.getFirst().setBackgroundColor(Color.BLACK);
        vv.setVisibility(View.VISIBLE);
        vv.start();
        vv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                vv.start();
            }
        });
    }

    private void stopVideoView() {
        if (videoQueue == null || videoQueue.isEmpty())
            return;
        final VideoView vv = (VideoView) videoQueue.getFirst().getChildAt(0);
        vv.setVisibility(View.INVISIBLE);
        videoQueue.getFirst().setBackgroundColor(Color.TRANSPARENT);
        vv.stopPlayback();
        videoQueue.remove();
    }

    // Used for metrics
    public String getAction() {
        return slideAction;
    }

    public String getName() {
        return slideName;
    }

    public void onDestroy() {
        controlsShown = false;
        destroyTimers();
        if (videoQueue == null || videoQueue.isEmpty())
            return;
        stopVideoView();
        SlideShowActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                leftArrow.setVisibility(View.GONE);
                rightArrow.setVisibility(View.GONE);
                settings.setVisibility(View.GONE);
                SlideShowActivity.getInstance().hideNavBar();
            }
        });

        for(ImageView img: imgAry){
            img.setBackground(null);
        }
        setKeepScreenOn(false);
    }

    private class RotateSlideTimerTask extends TimerTask {
        @Override
        public void run() {
            SlideShowActivity.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "RotateSlideTimerTask");
                    nextScreen(true);
                }
            });
        }
    }

    private class ResumeSlideShowTimerTask extends TimerTask {
        @Override
        public void run() {
            fadeTimeInMS = 1500;
            controlsShown = false;
            setupTimer(0);
            SlideShowActivity.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    leftArrow.setVisibility(View.GONE);
                    rightArrow.setVisibility(View.GONE);
                    settings.setVisibility(View.GONE);
                    SlideShowActivity.getInstance().hideNavBar();
                }
            });

        }
    }
}
