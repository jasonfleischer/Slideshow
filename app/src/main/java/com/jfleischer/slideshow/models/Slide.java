package com.jfleischer.slideshow.models;

import java.util.Locale;

public class Slide implements Comparable<Slide>{

    private int order = 0;
    private String image;

    private int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getDurationInSeconds() {
        return 0;
    }

    public static boolean isValidFileName(String fileName){
        fileName = fileName.toLowerCase(Locale.ENGLISH);
        return fileName.endsWith("png") || fileName.endsWith("jpg") || fileName.endsWith("jpeg") ||
               fileName.endsWith("gif") || fileName.endsWith("mp4") || fileName.endsWith("avi") ||
               fileName.endsWith("3gp");
    }

    @Override
    public int compareTo(Slide another) {
        return this.getOrder() - another.getOrder(); // increasing order
    }
}
