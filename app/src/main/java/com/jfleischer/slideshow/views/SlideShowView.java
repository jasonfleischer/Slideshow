package com.jfleischer.slideshow.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.jfleischer.slideshow.AddSlideActivity;
import com.jfleischer.slideshow.R;
import com.jfleischer.slideshow.RemoveSlideActivity;
import com.jfleischer.slideshow.SlideShowActivity;
import com.jfleischer.slideshow.SlideShowOptionsActivity;
import com.jfleischer.slideshow.models.Slide;
import com.jfleischer.slideshow.models.SlideShow;
import com.jfleischer.slideshow.utils.SharedPreferencesUtil;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SlideShowView extends RelativeLayout {


    private final static String TAG = SlideShowView.class.getSimpleName();

    private SlideShow slideShow;
    private List<Slide> slides;
    private RelativeLayout controls;
    private RelativeLayout[] slideContAry;
    private SlideImageView[] imgContAry;
    private SlideGifView[] gifContAry;
    private SlideVideoView[] vidContAry;


    private ImageView leftArrow, rightArrow, settings;
    private Timer timer;
    private TimerTask rotateSlideTimerTask, resumeSlideShowTimerTask;
    private int currentSlideIndex;
    private int previousSlideIndex;
    private boolean controllable, transitioning, controlsShown = false;

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
            return;
        }
        //videoQueue = new LinkedList<RelativeLayout>();
        transitioning = false;
        controllable = SharedPreferencesUtil.get_slideshow_controllable();
        currentSlideIndex = 1;




        controls = findViewById(R.id.slide_show_controls);


        final RelativeLayout frontSlideContainer = findViewById(R.id.front_slide_container);
        final RelativeLayout backSlideContainer = findViewById(R.id.back_slide_container);
        slideContAry = new RelativeLayout[]{backSlideContainer, frontSlideContainer};

        final SlideImageView frontImgCont = findViewById(R.id.front_imageViewCont);
        final SlideImageView backImgCont = findViewById(R.id.back_imageViewCont);
        imgContAry = new SlideImageView[]{backImgCont, frontImgCont};

        final SlideGifView frontGif = findViewById(R.id.front_gifViewCont);
        final SlideGifView backGif = findViewById(R.id.back_gifViewCont);
        gifContAry = new SlideGifView[]{backGif, frontGif};

        final SlideVideoView frontVideoContainer = findViewById(R.id.front_video_container);
        final SlideVideoView backVideoContainer = findViewById(R.id.back_video_container);
        vidContAry = new SlideVideoView[]{backVideoContainer, frontVideoContainer};

        for(SlideImageView img: imgContAry){
            img.init();
        }
        for(SlideGifView gif: gifContAry){
            gif.init();
        }
        for(SlideVideoView video: vidContAry){
            video.init();
        }


        leftArrow = findViewById(R.id.slide_show_left_arrow);
        rightArrow = findViewById(R.id.slide_show_right_arrow);
        settings = findViewById(R.id.slide_show_settings);
        settings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence[] options = new CharSequence[]{
                        getContext().getString(R.string.action_options),
                        getContext().getString(R.string.action_add),
                        getContext().getString(R.string.action_remove)};
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Choose an action");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(which==0){
                            getContext().startActivity(new Intent(getContext(), SlideShowOptionsActivity.class));
                        }else if(which==1){
                            getContext().startActivity(new Intent(getContext(), AddSlideActivity.class));
                        }else if(which==2){
                            getContext().startActivity(new Intent(getContext(), RemoveSlideActivity.class));
                        }
                    }
                });
                builder.show();

            }
        });

        setSlideBackground(slides.get(0), 1);
        setSlideBackground(slides.get(0), 0);
        slideContAry[1].setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    onSlideTouch(slides.get(0));
                    return true;
                }
                performClick();
                view.performClick();
                return performClick();
            }
        });

        frontSlideContainer.setAlpha(1.0f);
        if (!slideS.isTransparent()) {
            setBackgroundColor(Color.BLACK);
        }

        ProgressBar p = findViewById(R.id.slide_show_progress_bar);
        p.setVisibility(GONE);

        if (slides.size() == 1) {
            Log.i(TAG, "Slides are of size one");
        } else {
            if (slides.get(0).getType() == Slide.SlideType.Video) {
                vidContAry[0].startVideo(slides.size()<2);
            } else {
                if (slides.get(0).getDurationInSeconds() > 0) {
                    setupTimer(slides.get(0).getDurationInSeconds() * 1000);
                } else {
                    setupTimer(SharedPreferencesUtil.get_slide_duration());
                }
            }
        }
    }

    private void onSlideTouch(Slide slide) {
        //if(!touched){ // prevent duplicate touches

        controlsShown = true;
        settings.setVisibility(View.VISIBLE);
        resumeSlideShowTimerTask = new ResumeSlideShowTimerTask();

        if (controllable) {
            if(slides.size()>1) {
                leftArrow.setVisibility(View.VISIBLE);
                rightArrow.setVisibility(View.VISIBLE);
            }
            rightArrow.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //stopVideoView();
                    nextScreen(true);
                    resetResumeSlideShowTimerTask();
                }
            });
            leftArrow.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //stopVideoView();
                    nextScreen(false);
                    resetResumeSlideShowTimerTask();
                }
            });
            //if (isVideoFile(slides.get(0).getImage())) startVideo();

        }

        resetResumeSlideShowTimerTask();
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
        int fadeTimeInMS = SharedPreferencesUtil.get_fade_duration();

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

                setSlideBackground(slides.get(currentSlideIndex), isFront);
                if (slides.get(previousSlideIndex).getType() != Slide.SlideType.Video) {
                    timer = null;
                    if (slides.get(previousSlideIndex).getDurationInSeconds() > 0) {
                        setupTimer(slides.get(previousSlideIndex).getDurationInSeconds() * 1000);
                    } else {
                        setupTimer(SharedPreferencesUtil.get_slide_duration());
                    }
                } else {
                    //startVideo();
                    vidContAry[isFront].startVideo(slides.size()<2);
                }
                transitioning = false;
            }

            @Override
            public void onAnimationStart(Animation animation) {
                setSlideBackground(slides.get(previousSlideIndex), notIsFront);
                slideContAry[notIsFront].setAlpha(1.0f);
                slideContAry[notIsFront].bringToFront();
                controls.bringToFront();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private void setSlideBackground(Slide slide, int isFront) {
        final String path = slide.getPath();
        if(new File(path).exists()){
            switch (slide.getType()){
                case Image:
                    gifContAry[isFront].cleanup();
                    vidContAry[isFront].cleanup();
                    imgContAry[isFront].setup(slide);
                    imgContAry[isFront].bringToFront();
                    break;
                case Gif:
                    imgContAry[isFront].cleanup();
                    vidContAry[isFront].cleanup();
                    gifContAry[isFront].setup(slide);
                    gifContAry[isFront].bringToFront();
                    break;
                case Video:
                    imgContAry[isFront].cleanup();
                    gifContAry[isFront].cleanup();
                    vidContAry[isFront].setup(slide);
                    vidContAry[isFront].bringToFront();
                    break;
                case Unknown:
                    imgContAry[isFront].cleanup();
                    gifContAry[isFront].cleanup();
                    vidContAry[isFront].cleanup();
                    break;
            }
        }else{
            imgContAry[isFront].cleanup();
            gifContAry[isFront].cleanup();
            vidContAry[isFront].cleanup();
            Log.e(TAG, "file does not exist");
            //File imgFile = new File(path);
            //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_not_found);
            //imgAry[isFront].setBackground(new BitmapDrawable(getResources(), bitmap));
        }
    }


    public void onDestroy() {
        controlsShown = false;
        destroyTimers();

        SlideShowActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                leftArrow.setVisibility(View.GONE);
                rightArrow.setVisibility(View.GONE);
                settings.setVisibility(View.GONE);
                SlideShowActivity.getInstance().hideNavBar();
            }
        });

        for(SlideImageView img: imgContAry){
            img.cleanup();
        }
        for(SlideGifView gif: gifContAry){
            gif.cleanup();
        }
        for(SlideVideoView video: vidContAry){
            video.cleanup();
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
