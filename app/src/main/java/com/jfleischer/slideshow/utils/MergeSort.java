package com.jfleischer.slideshow.utils;

public class MergeSort {
    @SuppressWarnings("rawtypes")
    public static void mergeSort(Comparable[] a) {
        Comparable[] tmpArray = new Comparable[a.length];
        mergeSort(a, tmpArray, 0, a.length - 1);
    }

    /**
     * Internal method that makes recursive calls.
     *
     * @param a        an array of Comparable items.
     * @param tmpArray an array to place the merged result.
     * @param left     the left-most index of the sub array.
     * @param right    the right-most index of the sub array.
     */
    @SuppressWarnings("rawtypes")
    private static void mergeSort(Comparable[] a, Comparable[] tmpArray,
                                  int left, int right) {
        if (left < right) {
            int center = (left + right) / 2;
            mergeSort(a, tmpArray, left, center);
            mergeSort(a, tmpArray, center + 1, right);
            merge(a, tmpArray, left, center + 1, right);
        }
    }

    /**
     * Internal method that merges two sorted halves of a sub array.
     *
     * @param a        an array of Comparable items.
     * @param tmpArray an array to place the merged result.
     * @param leftPos  the left-most index of the sub array.
     * @param rightPos the index of the start of the second half.
     * @param rightEnd the right-most index of the sub array.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void merge(Comparable[] a, Comparable[] tmpArray,
                              int leftPos, int rightPos, int rightEnd) {
        int leftEnd = rightPos - 1;
        int tmpPos = leftPos;
        int numElements = rightEnd - leftPos + 1;

        // Main loop
        while (leftPos <= leftEnd && rightPos <= rightEnd)
            if (a[leftPos].compareTo(a[rightPos]) <= 0)
                tmpArray[tmpPos++] = a[leftPos++];
            else
                tmpArray[tmpPos++] = a[rightPos++];

        while (leftPos <= leftEnd)    // Copy rest of first half
            tmpArray[tmpPos++] = a[leftPos++];

        while (rightPos <= rightEnd)  // Copy rest of right half
            tmpArray[tmpPos++] = a[rightPos++];

        // Copy tmpArray back
        for (int i = 0; i < numElements; i++, rightEnd--)
            a[rightEnd] = tmpArray[rightEnd];
    }
}
