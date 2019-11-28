package com.jfleischer.slideshow.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

// util class for image processing
public class BitmapAndImageHelper {

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        final int width = bm.getWidth();
        final int height = bm.getHeight();
        if(width == newWidth && height == newHeight)
            return bm;
        final float scaleWidth = ((float) newWidth) / width;
        final float scaleHeight = ((float) newHeight) / height;
        final Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
    }
    public static Bitmap toGrayScale(Bitmap bitmap) {
        int width, height;
        height = bitmap.getHeight();
        width = bitmap.getWidth();
        final Bitmap bmpGrayScale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        final Canvas c = new Canvas(bmpGrayScale);
        final Paint paint = new Paint();
        final ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        final ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bitmap, 0, 0, paint);
        return bmpGrayScale;
    }

    // will throw illegal state exception if src is not mutable
    public static void changeColor(final Bitmap src, int colorToReplace, int colorThatWillReplace) {
        final int [] allPixels = new int [ src.getHeight()*src.getWidth()];
        src.getPixels(allPixels, 0, src.getWidth(), 0, 0, src.getWidth(),src.getHeight());
        for(int i =0; i<src.getHeight()*src.getWidth();i++){
            if( allPixels[i] == colorToReplace)
                allPixels[i] = colorThatWillReplace;
        }
        src.setPixels(allPixels, 0, src.getWidth(), 0, 0, src.getWidth(), src.getHeight());
    }

    /*public static int getAverageColor(int resourceId){
        final Bitmap bitmap = BitmapFactory.decodeResource(Navigator.getInstance().getResources(), resourceId);
        return getAverageColor(bitmap);
    }*/
    public static int getAverageColor(Bitmap bitmap){

        bitmap = Bitmap.createScaledBitmap(bitmap, 3, 3, false);
        final int [] allPixels = new int [ bitmap.getHeight()*bitmap.getWidth()];
        bitmap.getPixels(allPixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(),bitmap.getHeight());
        final int numberOfPixels = bitmap.getHeight()*bitmap.getWidth();
        int sumOfRed =0, sumOfBlue =0, sumOfGreen=0;
        for(int i =0; i<numberOfPixels;i++){
            final int pixelColor = allPixels[i];
            sumOfRed += Color.red(pixelColor);
            sumOfGreen +=Color.green(pixelColor);
            sumOfBlue += pixelColor & 0x000000FF;
        }
        return Color.rgb(sumOfRed/numberOfPixels, sumOfGreen/numberOfPixels, sumOfBlue/numberOfPixels);
    }

    public static int getComplementColor(int color){
        return Color.rgb(0xFF-Color.red(color), 0xFF-Color.green(color), 0xFF-Color.blue(color));
    }


    public static int getColorBasedOnStandardDeviation(int colorInt){
        if(calculateStandardDeviation(new double[]{Color.red(colorInt),Color.green(colorInt),Color.blue(colorInt)}) < 8){
            final int averageColor = (Color.red(colorInt)+Color.green(colorInt)+Color.blue(colorInt)) / 3;
            colorInt = ((averageColor<128)?Color.BLACK: Color.WHITE);
        }
        return colorInt;
    }

    private static double calculateStandardDeviation(double[] values){

        double sum = 0;
        for(final double i: values){
            sum += i;
        }
        final double mean = sum / values.length;
        double numerator = 0;
        for(final double j: values){
            numerator += Math.pow((j - mean), 2);
        }
        return Math.sqrt(numerator / values.length);
    }
}
