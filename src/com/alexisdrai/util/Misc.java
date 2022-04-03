package com.alexisdrai.util;

import java.util.Objects;

public class Misc
{

    public static String toRed(String orig)
    {
        return "\u001b[31m" + Objects.requireNonNull(orig) + "\u001b[0m";
    }

    public static String toYellow(String orig)
    {
        return "\u001b[33m" + Objects.requireNonNull(orig) + "\u001b[0m";
    }

    public static String toOriginalColor(String orig)
    {
        return "\u001b[0m" + Objects.requireNonNull(orig);
    }

    public static int maxFromArray(int[] arr, boolean returnIdx)
    {
        if (arr == null || arr.length == 0)
        {
            throw new IllegalArgumentException("non-existant or empty array");
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
        return returnIdx ? idx : max;
    }
}
