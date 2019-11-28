package com.jfleischer.slideshow.models;

import java.util.Locale;

public class Slide implements Comparable<Slide>{

    private  String path;
    private int order;
    private SlideType type;

    public enum SlideType {
        Image, Gif, Video , Unknown
    };

    public Slide(String path, int order){
        this.path = path;
        this.order = order;
        type = getSlideType(path);
    }
    public String getPath() { return path; }
    private int getOrder() {
        return order;
    }
    public int getDurationInSeconds() {
        return 0;
    }


    public SlideType getType() {
        return type;
    }

    public boolean isValidFileName(){
        return type != SlideType.Unknown;
    }

    public static boolean isValidFileName(String fileName){
        return getSlideType(fileName) != SlideType.Unknown;
    }

    private static SlideType getSlideType(String path){
        String fileName = path.toLowerCase(Locale.ENGLISH);
        if (fileName.endsWith(".gif")) {
            return SlideType.Gif;

            ////TODO put back video content
        /*} else if (fileName.endsWith("mp4") || fileName.endsWith("avi") || fileName.endsWith("3gp")) {
            return SlideType.Video;
        }*/} else if (fileName.endsWith("png") || fileName.endsWith("jpg") || fileName.endsWith("jpeg")) {
            return SlideType.Image;
        } else {
            return SlideType.Unknown;
        }
    }

    @Override
    public int compareTo(Slide another) {
        return this.getOrder() - another.getOrder(); // increasing order
    }
}
