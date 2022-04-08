package com.alexisdrai.util;

public class Misc
{
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static int maxIdxFromArray(int[] arr)
    {
        if (arr == null || arr.length == 0)
        {
            throw new IllegalArgumentException("non-existent or empty array");
        }
        int idx = 0;
        int max = arr[idx];
        for (int i = 1; i < arr.length; i++)
        {
            if (arr[i] > max)
            {
                idx = i;
                max = arr[idx];
            }
        }
        return idx;
    }
}
