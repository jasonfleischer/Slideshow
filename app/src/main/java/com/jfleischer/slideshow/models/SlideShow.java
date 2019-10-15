package com.jfleischer.slideshow.models;

import com.jfleischer.slideshow.SlideShowConfig;
import com.jfleischer.slideshow.utils.MergeSort;

import java.util.List;

public class SlideShow {
    private List<Slide> slides;
    private boolean isControllable = SlideShowConfig.CONTROLLABLE;
    private boolean isFullscreen = true;
    private boolean isTransparent = false;
    private int controllableTimeoutInSeconds = 15;

    public List<Slide> getSlides() {
        return slides;
    }

    public void setSlides(List<Slide> slides) {
        this.slides = slides;
    }

    public boolean isControllable() {
        return isControllable;
    }

    public boolean isFullscreen() {
        return isFullscreen;
    }

    public boolean isTransparent() {
        return isTransparent;
    }

    public int getControllableTimeoutInSeconds() {
        if (controllableTimeoutInSeconds < 10)
            return 15;
        return controllableTimeoutInSeconds;
    }

    public void sort() {
        final int size = slides.size();
        MergeSort.mergeSort(slides.toArray(new Slide[size]));
    }

}
