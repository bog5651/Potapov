package com.company;

import java.util.ArrayList;

public class Utils {
    public static String printArray(String prefix, ArrayList<Integer> list) {
        StringBuilder builder = new StringBuilder();
        builder.append(prefix).append(" { ");
        for (Integer o : list) {
            builder.append(o).append(" ");
        }
        builder.append("}");
        return builder.toString();
    }
}
