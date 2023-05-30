package com.company.Temp;

import java.util.ArrayList;
import java.util.TreeSet;

public class main {
    public static void main(String[] args) {
        ArrayList<Integer> imageArrayIndexes;
        imageArrayIndexes = new ArrayList<>();
        for (int i = 0; i < 10000; i++){
            imageArrayIndexes.add(i, i);
        }

        while (true){
            ArrayList<Integer> ArrayIndex = new ArrayList<>(imageArrayIndexes);
            TreeSet<Integer> arrIND = new TreeSet<>(ArrayIndex);
            TreeSet<Integer> ss = new TreeSet<>();
            for (int j : ArrayIndex){
                ss.add(j % 5097);
            }
            arrIND.removeAll(ss);
            Integer[] temp2 = arrIND.toArray(new Integer[ss.size()]);
            arrIND.addAll(ss);
            arrIND.clear();
        }

        
    }
}
