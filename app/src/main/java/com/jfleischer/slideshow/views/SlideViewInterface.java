package com.jfleischer.slideshow.views;

import com.jfleischer.slideshow.models.Slide;

public interface SlideViewInterface {

    void init();
    void setup(Slide slide);
    void cleanup();
}
